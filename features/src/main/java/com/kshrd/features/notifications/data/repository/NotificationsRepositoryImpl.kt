package com.kshrd.features.notifications.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.UnreadCountSource
import com.kshrd.features.notifications.data.source.InMemoryNotificationsDataSource
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.domain.repository.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Also exposes the unread count to `:core`, so every bell icon can show its dot. */
@Singleton
class NotificationsRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryNotificationsDataSource,
    ) : NotificationsRepository,
        UnreadCountSource {
        override fun observeNotifications(): Flow<DataState<List<AppNotification>>> = source.items.map { DataState.Success(it) }

        override val unreadCount: Flow<Int> = source.items.map { list -> list.count { it.unread } }

        override suspend fun markRead(id: String) = source.markRead(id)

        override suspend fun markAllRead() = source.markAllRead()
    }
