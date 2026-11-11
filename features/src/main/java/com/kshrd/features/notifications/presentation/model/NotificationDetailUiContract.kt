package com.kshrd.features.notifications.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.features.notifications.domain.model.AppNotification

@Immutable
data class NotificationDetailUiState(
    val notificationId: String,
    val notifications: UiState<List<AppNotification>> = DataState.Loading,
) {
    /** The notification being shown, or null while loading or if it no longer exists. */
    val notification: AppNotification?
        get() = (notifications as? DataState.Success)?.data?.firstOrNull { it.id == notificationId }
}

sealed interface NotificationDetailEvent {
    data object BackClicked : NotificationDetailEvent
}

sealed interface NotificationDetailEffect {
    data object Back : NotificationDetailEffect
}
