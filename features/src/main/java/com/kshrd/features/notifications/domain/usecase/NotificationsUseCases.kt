package com.kshrd.features.notifications.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.domain.repository.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotificationsUseCase
    @Inject
    constructor(
        private val repository: NotificationsRepository,
    ) {
        operator fun invoke(): Flow<DataState<List<AppNotification>>> = repository.observeNotifications().asUseCaseFlow()
    }

class MarkNotificationReadUseCase
    @Inject
    constructor(
        private val repository: NotificationsRepository,
    ) {
        suspend operator fun invoke(id: String) = repository.markRead(id)
    }

class MarkAllNotificationsReadUseCase
    @Inject
    constructor(
        private val repository: NotificationsRepository,
    ) {
        suspend operator fun invoke() = repository.markAllRead()
    }
