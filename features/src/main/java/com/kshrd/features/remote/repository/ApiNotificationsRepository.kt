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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The bell from Notification v2 (docs/API-MAPPING.md 2.3). Marking read updates the screen first; a failed PATCH is
 * ignored (the next load shows the server's state). Also gives `:core` the unread count for every bell icon.
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

        override suspend fun markRead(id: String) {
            items.update { list -> list?.map { if (it.id == id) it.copy(unread = false) else it } }
            optional { notificationApi.markRead(id) }
        }

        override suspend fun markAllRead() {
            items.update { list -> list?.map { it.copy(unread = false) } }
            optional { notificationApi.markAllRead() }
        }

        private suspend fun fetch(): List<AppNotification> =
            notificationApi.notifications().items.map { dto ->
                val time = parseApiTime(dto.createdAt)?.let(format::relative).orEmpty()
                // The website's logo isn't part of the bell; the initials tile stands in.
                dto.toAppNotification(timeText = time, logoUrl = null)
            }
    }
