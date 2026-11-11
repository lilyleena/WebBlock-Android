package com.kshrd.features.website.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.features.website.domain.model.Website

@Immutable
data class MyWebsitesUiState(
    val ownerName: String = "",
    val ownerEmail: String = "",
    val websites: UiState<List<Website>> = DataState.Loading,
    val isRefreshing: Boolean = false,
    val unreadCount: Int = 0,
    val showLogoutDialog: Boolean = false,
) {
    /** "Sovannarith Soeun" -> "SS", as on the iOS avatar. */
    val ownerInitials: String
        get() =
            ownerName
                .split(' ')
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "?" }

    /** "Sovannarith Soeun" -> "Sovannarith", for the welcome card. */
    val ownerFirstName: String
        get() = ownerName.trim().substringBefore(' ')
}

sealed interface MyWebsitesEvent {
    data object RetryClicked : MyWebsitesEvent

    data object RefreshRequested : MyWebsitesEvent

    data class WebsiteClicked(val websiteId: String) : MyWebsitesEvent

    data object BellClicked : MyWebsitesEvent

    data object ReplayIntroClicked : MyWebsitesEvent

    data object LogoutClicked : MyWebsitesEvent

    data object LogoutConfirmed : MyWebsitesEvent

    data object LogoutDismissed : MyWebsitesEvent
}

sealed interface MyWebsitesEffect {
    data object OpenPortal : MyWebsitesEffect

    data object OpenNotifications : MyWebsitesEffect

    data object ReplayIntro : MyWebsitesEffect

    data object SignedOut : MyWebsitesEffect
}
