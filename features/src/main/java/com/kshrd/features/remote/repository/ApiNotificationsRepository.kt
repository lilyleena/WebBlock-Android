package com.kshrd.features.remote.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.UnreadCountSource
import com.kshrd.features.remote.mapper.MonitorFormat
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.domain.repository.NotificationsRepository
import com.kshrd.features.remote.api.NotificationApi
import com.kshrd.features.remote.mapper.parseApiTime
import com.kshrd.features.remote.mapper.toAppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The bell from Notification v2 (docs/API-MAPPING.md 2.3). Marking read updates the screen first, and a list reloaded
 * before the PATCH lands keeps the item read. A PATCH that doesn't land (it fails, or the screen is left and the call is
 * cancelled) is not retried: the item stays read in the list already on screen, but the next load shows the server's
 * state (unread again), and opening it then sends the PATCH again. Also gives `:core` the unread count for every bell
 * icon.
 */
@Singleton
class ApiNotificationsRepository
    @Inject
    constructor(
        private val notificationApi: NotificationApi,
        private val format: MonitorFormat,
    ) : NotificationsRepository,
        UnreadCountSource {
        /** `null` until the first load. */
        private val items = MutableStateFlow<List<AppNotification>?>(null)

        override fun observeNotifications(): Flow<DataState<List<AppNotification>>> =
            flow {
                when (val loaded = loadState { fetch() }) {
                    is DataState.Success -> {
                        items.value = loaded.data
                        emitAll(items.filterNotNull().map { DataState.Success(it) })
                    }
                    else -> emit(loaded)
                }
            }

        override val unreadCount: Flow<Int> =
            flow {
                if (items.value == null) optional { items.value = fetch() }
                emitAll(items.map { list -> list.orEmpty().count { it.unread } })
            }

        /** Ids with a PATCH on its way: the list and the detail screen both mark the opened item. */
        private val marking = ConcurrentHashMap.newKeySet<String>()

        /**
         * Ids the owner marked read here that the server may not show as read yet, each with the number of requests on
         * their way that cover it (a single PATCH and a mark-all can overlap, and so can two mark-alls). A list loaded
         * before they land (the detail screen reloads while one is on its way) would otherwise show them unread again.
         * A request that doesn't land (fails or is cancelled) releases only its own claim, so the id stays read while
         * another request still covers it; an id is dropped once the server sends it as read. Only the call that
         * actually sends the PATCH claims an id: a second open of an item already read on screen sends nothing.
         */
        private val readLocally = ConcurrentHashMap<String, Int>()

        private fun claim(id: String) {
            readLocally.merge(id, 1, Int::plus)
        }

        private fun release(id: String) {
            readLocally.computeIfPresent(id) { _, claims -> if (claims > 1) claims - 1 else null }
        }

        /** Sends the PATCH only for an item that is unread (or not loaded yet), and only once at a time. */
        override suspend fun markRead(id: String) {
            val wasUnread = items.value?.firstOrNull { it.id == id }?.unread ?: true
            items.update { list -> list?.map { if (it.id == id) it.copy(unread = false) else it } }
            if (!wasUnread || !marking.add(id)) return
            claim(id)
            var landed = false
            try {
                landed = optional { notificationApi.markRead(id) } != null
            } finally {
                if (!landed) release(id)
                marking.remove(id)
            }
        }

        override suspend fun markAllRead() {
            val ids = items.value.orEmpty().map { it.id }
            ids.forEach(::claim)
            items.update { list -> list?.map { it.copy(unread = false) } }
            var landed = false
            try {
                landed = optional { notificationApi.markAllRead() } != null
            } finally {
                if (!landed) ids.forEach(::release)
            }
        }

        private suspend fun fetch(): List<AppNotification> =
            notificationApi.notifications().items.map { dto ->
                val time = parseApiTime(dto.createdAt)?.let(format::relative).orEmpty()
                if (dto.read) readLocally.remove(dto.id)
                // The website's logo isn't part of the bell; the initials tile stands in.
                val item = dto.toAppNotification(timeText = time, logoUrl = null)
                if (readLocally.containsKey(dto.id)) item.copy(unread = false) else item
            }
    }
