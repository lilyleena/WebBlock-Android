package com.kshrd.features.website.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ConfirmDialog
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.screen.DataStateScreenShell
import com.kshrd.coreui.components.text.SectionTitle
import com.kshrd.coreui.layout.LightStatusBarIcons
import com.kshrd.coreui.layout.navigationBarBottom
import com.kshrd.coreui.motion.appearAnimated
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.presentation.components.PortalTopBar
import com.kshrd.features.website.presentation.components.WebsiteCard
import com.kshrd.features.website.presentation.components.WelcomeCard
import com.kshrd.features.website.presentation.components.portalHeaderBackdrop
import com.kshrd.features.website.presentation.components.previewWebsite
import com.kshrd.features.website.presentation.model.MyWebsitesEffect
import com.kshrd.features.website.presentation.model.MyWebsitesEvent
import com.kshrd.features.website.presentation.model.MyWebsitesUiState
import com.kshrd.features.website.presentation.viewmodel.MyWebsitesViewModel

/** Landing screen after sign-in: every live website the owner can manage (iOS `WebsiteListView`). */
@Composable
fun MyWebsitesScreen(
    onOpenPortal: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSignedOut: () -> Unit,
    onReplayIntro: () -> Unit = {},
    viewModel: MyWebsitesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            MyWebsitesEffect.OpenPortal -> onOpenPortal()
            MyWebsitesEffect.OpenNotifications -> onOpenNotifications()
            MyWebsitesEffect.ReplayIntro -> onReplayIntro()
            MyWebsitesEffect.SignedOut -> onSignedOut()
        }
    }
    MyWebsitesContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun MyWebsitesContent(
    state: MyWebsitesUiState,
    onEvent: (MyWebsitesEvent) -> Unit,
) {
    LiquidBackdrop {
        DataStateScreenShell(
            state = state.websites,
            modifier = Modifier.fillMaxSize(),
            // iOS shows a bare centred spinner while the first load runs (no toolbar yet).
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingView(text = stringResource(R.string.home_loading)) } },
            error = {
                PageScaffold(state, onEvent, title = stringResource(R.string.home_title_error)) {
                    ErrorStateView(
                        message = (state.websites as? DataState.Error)?.message.orEmpty(),
                        onRetry = { onEvent(MyWebsitesEvent.RetryClicked) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
            isEmpty = { it.isEmpty() },
            empty = {
                PageScaffold(state, onEvent, title = stringResource(R.string.home_title_error)) {
                    NoLiveWebsite(onRefresh = { onEvent(MyWebsitesEvent.RefreshRequested) })
                }
            },
            success = { websites ->
                // No big title here: the welcome card at the top of the list greets the owner instead.
                PageScaffold(state, onEvent, title = null) {
                    WebsiteList(websites = websites, state = state, onEvent = onEvent)
                }
            },
        )
        if (state.showLogoutDialog) {
            ConfirmDialog(
                title = stringResource(R.string.home_logout_title),
                message = stringResource(R.string.home_logout_message, state.ownerEmail),
                confirmLabel = stringResource(R.string.home_log_out),
                cancelLabel = stringResource(R.string.home_cancel),
                onConfirm = { onEvent(MyWebsitesEvent.LogoutConfirmed) },
                onDismiss = { onEvent(MyWebsitesEvent.LogoutDismissed) },
                destructive = true,
            )
        }
    }
}

// Where the blue has fully faded into the page background (about the bottom of the welcome card).
private val HeaderFadeEnd = 380.dp

/**
 * Brand gradient header (greeting, bell, avatar menu, optional large [title]) that fades into the page
 * background further down, as in the BookMe+ reference: cards sit on the gradient, then on the page.
 */
@Composable
private fun BoxScope.PageScaffold(
    state: MyWebsitesUiState,
    onEvent: (MyWebsitesEvent) -> Unit,
    title: String?,
    content: @Composable () -> Unit,
) {
    LightStatusBarIcons()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .portalHeaderBackdrop(page = MaterialTheme.wb.background, fadeEnd = HeaderFadeEnd),
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            PortalTopBar(
                ownerName = state.ownerName,
                ownerInitials = state.ownerInitials,
                unreadCount = state.unreadCount,
                onBellClick = { onEvent(MyWebsitesEvent.BellClicked) },
                onReplayIntro = { onEvent(MyWebsitesEvent.ReplayIntroClicked) },
                onLogout = { onEvent(MyWebsitesEvent.LogoutClicked) },
                onBrand = true,
            )
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = Space.screen).padding(top = Space.xxs, bottom = Space.l).semantics { heading() },
                )
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebsiteList(
    websites: List<Website>,
    state: MyWebsitesUiState,
    onEvent: (MyWebsitesEvent) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(MyWebsitesEvent.RefreshRequested) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            // A little room under the toolbar so the welcome card's sticker isn't jammed against it.
            contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.s, bottom = navigationBarBottom(Space.l)),
            verticalArrangement = Arrangement.spacedBy(Space.m),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "welcome") {
                WelcomeCard(
                    firstName = state.ownerFirstName,
                    websiteCount = websites.size,
                    modifier = Modifier.padding(bottom = Space.xs),
                )
            }
            item {
                // The total lives in the welcome card above, so this is just a section label.
                SectionTitle(stringResource(R.string.home_live_websites))
            }
            itemsIndexed(websites, key = { _, site -> site.id }) { index, website ->
                WebsiteCard(
                    website = website,
                    onClick = { onEvent(MyWebsitesEvent.WebsiteClicked(website.id)) },
                    modifier = Modifier.appearAnimated(index),
                )
            }
        }
    }
}

/** iOS `NoLiveWebsiteView`: the shared empty state with a Refresh button. */
@Composable
private fun NoLiveWebsite(onRefresh: () -> Unit) {
    EmptyStateView(
        title = stringResource(R.string.home_no_live_title),
        message = stringResource(R.string.home_no_live_message),
        icon = Icons.Outlined.Public,
        actionLabel = stringResource(R.string.home_refresh),
        onAction = onRefresh,
    )
}

private val previewSites =
    listOf(
        previewWebsite,
        previewWebsite.copy(id = "bean", name = "Bean & Brew Café", businessType = "Café", url = "beanandbrew.webblock.com", logoRes = R.drawable.bean_brew_logo),
        previewWebsite.copy(id = "none", name = "Maple Rye", url = null, logoRes = null),
    )

@ThemePreviews
@Composable
private fun MyWebsitesContentPreview() {
    PreviewSurface(padding = 0) {
        MyWebsitesContent(
            state =
                MyWebsitesUiState(
                    ownerName = "Sovannarith Soeun",
                    ownerEmail = "sovannarith@gmail.com",
                    websites = DataState.Success(previewSites),
                    unreadCount = 4,
                ),
            onEvent = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MyWebsitesEmptyPreview() {
    PreviewSurface(padding = 0) {
        MyWebsitesContent(
            state = MyWebsitesUiState(ownerName = "Sovannarith Soeun", websites = DataState.Success(emptyList())),
            onEvent = {},
        )
    }
}
