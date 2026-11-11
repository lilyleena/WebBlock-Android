package com.kshrd.features.notifications.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.features.notifications.domain.model.AppNotification

enum class NotificationTab { All, Unread }

@Immutable
data class NotificationsUiState(
    val notifications: UiState<List<AppNotification>> = DataState.Loading,
    val tab: NotificationTab = NotificationTab.All,
) {
    /** The list for the selected tab. Filtering lives here, not in the composable. */
    val visible: UiState<List<AppNotification>>
        get() =
            when (val all = notifications) {
                is DataState.Success ->
                    DataState.Success(if (tab == NotificationTab.Unread) all.data.filter { it.unread } else all.data)
                else -> all
            }

    val unreadCount: Int
        get() = (notifications as? DataState.Success)?.data?.count { it.unread } ?: 0

    val hasUnread: Boolean get() = unreadCount > 0
}

sealed interface NotificationsEvent {
    data class TabSelected(val tab: NotificationTab) : NotificationsEvent

    data class ItemClicked(val id: String) : NotificationsEvent

    data object MarkAllReadClicked : NotificationsEvent

    data object BackClicked : NotificationsEvent
}

sealed interface NotificationsEffect {
    data object Back : NotificationsEffect

    data class OpenDetail(val id: String) : NotificationsEffect
}
