package com.kshrd.features.notifications.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.card.SegmentedTabs
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.screen.DataStateScreenShell
import com.kshrd.coreui.components.text.SectionTitle
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.layout.navigationBarBottom
import com.kshrd.coreui.motion.appearAnimated
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.presentation.components.NotificationSection
import com.kshrd.features.notifications.presentation.model.NotificationTab
import com.kshrd.features.notifications.presentation.model.NotificationsEffect
import com.kshrd.features.notifications.presentation.model.NotificationsEvent
import com.kshrd.features.notifications.presentation.model.NotificationsUiState
import com.kshrd.features.notifications.presentation.viewmodel.NotificationsViewModel

/**
 * Full-screen notifications list (iOS `NotificationsSheet`). Tapping a notification opens its detail
 * page via [onOpenDetail].
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            NotificationsEffect.Back -> onBack()
            is NotificationsEffect.OpenDetail -> onOpenDetail(effect.id)
        }
    }
    NotificationsContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit,
) {
    val wb = MaterialTheme.wb
    LiquidBackdrop {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onEvent(NotificationsEvent.BackClicked) }) {
                    Icon(WebIcons.X, contentDescription = stringResource(R.string.notif_close_cd), tint = wb.text)
                }
                TextButton(onClick = { onEvent(NotificationsEvent.MarkAllReadClicked) }) {
                    Text(
                        stringResource(R.string.notif_mark_all),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = wb.brand,
                    )
                }
            }
            Text(
                text = stringResource(R.string.notif_title),
                style = MaterialTheme.typography.displaySmall,
                color = wb.brand,
                modifier = Modifier.padding(horizontal = Space.screen).semantics { heading() },
            )
            SegmentedTabs(
                labels = listOf(stringResource(R.string.notif_tab_all), stringResource(R.string.notif_tab_unread, state.unreadCount)),
                selectedIndex = state.tab.ordinal,
                onSelect = { onEvent(NotificationsEvent.TabSelected(NotificationTab.entries[it])) },
                modifier = Modifier.padding(Space.screen),
            )
            DataStateScreenShell(
                state = state.visible,
                modifier = Modifier.weight(1f),
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingView() } },
                error = {
                    ErrorStateView(
                        message = stringResource(R.string.notif_error),
                        onRetry = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                isEmpty = { it.isEmpty() },
                empty = { AllCaughtUp() },
                success = { items ->
                    val unreadItems = items.filter { it.unread }
                    val readItems = items.filterNot { it.unread }
                    val open = { item: AppNotification -> onEvent(NotificationsEvent.ItemClicked(item.id)) }
                    LazyColumn(
                        contentPadding = PaddingValues(start = Space.screen, end = Space.screen, bottom = navigationBarBottom(Space.l)),
                        verticalArrangement = Arrangement.spacedBy(Space.xs),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (unreadItems.isNotEmpty()) {
                            item(key = "header-new") {
                                SectionTitle(stringResource(R.string.notif_section_new), count = unreadItems.size)
                            }
                            item(key = "section-new") {
                                NotificationSection(unreadItems, onItemClick = open, modifier = Modifier.appearAnimated(0))
                            }
                        }
                        if (readItems.isNotEmpty()) {
                            item(key = "header-earlier") {
                                SectionTitle(
                                    stringResource(R.string.notif_section_earlier),
                                    modifier = Modifier.padding(top = if (unreadItems.isNotEmpty()) Space.s else 0.dp),
                                )
                            }
                            item(key = "section-earlier") {
                                NotificationSection(readItems, onItemClick = open, modifier = Modifier.appearAnimated(1))
                            }
                        }
                    }
                },
            )
        }
    }
}

/** iOS `ContentUnavailableView("You’re all caught up", systemImage: "bell.slash")`. */
@Composable
private fun AllCaughtUp() {
    EmptyStateView(
        title = stringResource(R.string.notif_empty_title),
        message = stringResource(R.string.notif_empty_message),
        icon = Icons.Outlined.NotificationsOff,
    )
}

private val previewItems =
    listOf(
        AppNotification("1", "Petal & Stem", "Your latest changes are live.", "2 mins ago", true, R.drawable.petal_stem_logo),
        AppNotification("2", "Northpoint Audio", "Website suspended · reactivate it any time.", "1 hour ago", true),
        AppNotification("3", "WebBlock Billing", "Your free trial ends in 9 days. Pick a plan.", "3 hours ago", true),
        AppNotification("4", "Lantern Café", "Draft saved · publish when you’re ready.", "Yesterday", false),
    )

@ThemePreviews
@Composable
private fun NotificationsContentPreview() {
    PreviewSurface(padding = 0) {
        NotificationsContent(state = NotificationsUiState(notifications = DataState.Success(previewItems)), onEvent = {})
    }
}

@ThemePreviews
@Composable
private fun NotificationsEmptyPreview() {
    PreviewSurface(padding = 0) {
        NotificationsContent(
            state = NotificationsUiState(notifications = DataState.Success(previewItems.map { it.copy(unread = false) }), tab = NotificationTab.Unread),
            onEvent = {},
        )
    }
}
