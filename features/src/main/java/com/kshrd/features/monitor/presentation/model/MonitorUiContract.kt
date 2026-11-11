package com.kshrd.features.monitor.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.core.session.PortalChrome
import com.kshrd.features.monitor.domain.model.MonitorData

/** The segmented picker at the top: "Overview" and "Activity" (the activity log + recent deploys). */
enum class MonitorTab { Overview, Logs }

@Immutable
data class MonitorUiState(
    val chrome: PortalChrome = PortalChrome(),
    val ownerName: String = "",
    val monitor: UiState<MonitorData> = DataState.Loading,
    val tab: MonitorTab = MonitorTab.Overview,
)

sealed interface MonitorEvent {
    data class TabSelected(val tab: MonitorTab) : MonitorEvent

    data object RetryClicked : MonitorEvent

    data object VisitSiteClicked : MonitorEvent

    data object BellClicked : MonitorEvent

    data class WebsiteSwitched(val websiteId: String) : MonitorEvent

    data object BackClicked : MonitorEvent
}

sealed interface MonitorEffect {
    data class OpenUrl(val url: String) : MonitorEffect

    data object OpenNotifications : MonitorEffect

    data object BackToWebsites : MonitorEffect
}
