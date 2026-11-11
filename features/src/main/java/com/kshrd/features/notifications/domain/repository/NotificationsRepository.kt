package com.kshrd.features.notifications.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.notifications.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    fun observeNotifications(): Flow<DataState<List<AppNotification>>>

    suspend fun markRead(id: String)

    suspend fun markAllRead()
}
