package com.kshrd.features.monitor.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.core.session.PortalChrome
import com.kshrd.core.session.WorkspaceState
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.card.SegmentedTabs
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.nav.dockContentPadding
import com.kshrd.coreui.components.text.Section
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.business.presentation.components.PortalTitle
import com.kshrd.features.business.presentation.components.PortalTopBar
import com.kshrd.features.business.presentation.components.SiteHeaderCard
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.presentation.components.ActivityLog
import com.kshrd.features.monitor.presentation.components.DeployActivityCard
import com.kshrd.features.monitor.presentation.components.HealthTiles
import com.kshrd.features.monitor.presentation.components.PlanUsageCard
import com.kshrd.features.monitor.presentation.components.RecentDeploysCard
import com.kshrd.features.monitor.presentation.components.StatusCard
import com.kshrd.features.monitor.presentation.components.SummaryCard
import com.kshrd.features.monitor.presentation.components.previewMonitor
import com.kshrd.features.monitor.presentation.model.MonitorEffect
import com.kshrd.features.monitor.presentation.model.MonitorEvent
import com.kshrd.features.monitor.presentation.model.MonitorTab
import com.kshrd.features.monitor.presentation.model.MonitorUiState
import com.kshrd.features.monitor.presentation.viewmodel.MonitorViewModel

@Composable
fun MonitorScreen(
    onOpenNotifications: () -> Unit,
    onBackToWebsites: () -> Unit,
    viewModel: MonitorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is MonitorEffect.OpenUrl -> runCatching { uriHandler.openUri(effect.url) }
            MonitorEffect.OpenNotifications -> onOpenNotifications()
            MonitorEffect.BackToWebsites -> onBackToWebsites()
        }
    }
    MonitorContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun MonitorContent(
    state: MonitorUiState,
    onEvent: (MonitorEvent) -> Unit,
) {
    val website = state.chrome.workspace.current
    LiquidBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            PortalTopBar(
                ownerName = state.ownerName,
                unreadCount = state.chrome.unreadCount,
                onBack = { onEvent(MonitorEvent.BackClicked) },
                onBell = { onEvent(MonitorEvent.BellClicked) },
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Space.screen)
                        .padding(top = Space.xs, bottom = dockContentPadding()),
                verticalArrangement = Arrangement.spacedBy(Space.m),
            ) {
                // The large title scrolls away with the content, like the iOS navigation title.
                PortalTitle(stringResource(R.string.bm_monitor_title), horizontalPadding = 0.dp)
                Text(
                    stringResource(R.string.bm_monitor_subtitle, website?.domain ?: stringResource(R.string.bm_your_website)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.wb.muted,
                )
                SiteHeaderCard(
                    website = website,
                    websites = state.chrome.workspace.websites,
                    onSwitch = { onEvent(MonitorEvent.WebsiteSwitched(it)) },
                )
                when (val monitor = state.monitor) {
                    is DataState.Success -> MonitorBody(monitor.data, state.tab, onEvent)
                    is DataState.Error -> ErrorStateView(message = stringResource(R.string.bm_monitor_error), onRetry = { onEvent(MonitorEvent.RetryClicked) })
                    else -> LoadingView()
                }
            }
        }
    }
}

@Composable
private fun MonitorBody(
    data: MonitorData,
    tab: MonitorTab,
    onEvent: (MonitorEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        SegmentedTabs(
            labels = listOf(stringResource(R.string.bm_tab_overview), stringResource(R.string.bm_tab_activity)),
            selectedIndex = tab.ordinal,
            onSelect = { onEvent(MonitorEvent.TabSelected(MonitorTab.entries[it])) },
        )
        when (tab) {
            MonitorTab.Overview -> {
                StatusCard(data, onVisitSite = { onEvent(MonitorEvent.VisitSiteClicked) })
                Section(stringResource(R.string.bm_summary)) { SummaryCard(data) }
                Section(stringResource(R.string.bm_plan_usage)) { PlanUsageCard(data) }
                Section(stringResource(R.string.bm_health_metrics)) { HealthTiles(data) }
                Section(stringResource(R.string.bm_deploy_activity)) { DeployActivityCard(data) }
            }
            MonitorTab.Logs -> {
                ActivityLog(data.logs)
                RecentDeploysCard(data)
            }
        }
    }
}

private val previewChrome =
    PortalChrome(
        WorkspaceState(
            current = WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com"),
            websites = listOf(WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com")),
        ),
        unreadCount = 1,
    )

@ThemePreviews
@Composable
private fun MonitorContentPreview() {
    PreviewSurface(padding = 0) {
        MonitorContent(
            state = MonitorUiState(chrome = previewChrome, ownerName = "Maya Sok", monitor = DataState.Success(previewMonitor)),
            onEvent = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MonitorActivityPreview() {
    PreviewSurface(padding = 0) {
        MonitorContent(
            state = MonitorUiState(chrome = previewChrome, ownerName = "Maya Sok", monitor = DataState.Success(previewMonitor), tab = MonitorTab.Logs),
            onEvent = {},
        )
    }
}
