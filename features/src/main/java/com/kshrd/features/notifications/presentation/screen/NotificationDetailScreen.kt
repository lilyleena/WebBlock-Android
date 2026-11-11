package com.kshrd.features.notifications.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.card.GlassBlock
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.EmptyStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.nav.BackTopBar
import com.kshrd.coreui.layout.navigationBarBottom
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.overline
import com.kshrd.coreui.theme.paragraph
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.presentation.components.NotificationAvatar
import com.kshrd.features.notifications.presentation.model.NotificationDetailEffect
import com.kshrd.features.notifications.presentation.model.NotificationDetailEvent
import com.kshrd.features.notifications.presentation.model.NotificationDetailUiState
import com.kshrd.features.notifications.presentation.viewmodel.NotificationDetailViewModel

/** One notification's own page (iOS `NotificationDetailView`). Read-only: it shows the details, and Back is the only way out. */
@Composable
fun NotificationDetailScreen(
    onBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            NotificationDetailEffect.Back -> onBack()
        }
    }
    NotificationDetailContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun NotificationDetailContent(
    state: NotificationDetailUiState,
    onEvent: (NotificationDetailEvent) -> Unit,
) {
    val item = state.notification
    LiquidBackdrop {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        BackTopBar(
            backLabel = stringResource(R.string.action_back),
            onBack = { onEvent(NotificationDetailEvent.BackClicked) },
            title = stringResource(R.string.notification_detail_title),
        )
        when {
            item != null -> DetailBody(item)
            state.notifications is DataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingView() }
            else -> EmptyStateView(title = stringResource(R.string.notif_missing))
        }
    }
    }
}

@Composable
private fun DetailBody(item: AppNotification) {
    val wb = MaterialTheme.wb
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.screen)
                .padding(top = Space.xs, bottom = navigationBarBottom(Space.l)),
        verticalArrangement = Arrangement.spacedBy(Space.m),
    ) {
        // Headline card: who sent it, what it says, when, and whether it is new.
        GlassBlock {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                NotificationAvatar(item.copy(unread = false))
                Text(
                    text = item.source,
                    style = MaterialTheme.typography.titleSmall,
                    color = wb.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = item.message,
                style = MaterialTheme.typography.headlineMedium.copy(lineBreak = LineBreak.Heading),
                color = wb.text,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                StatusChip(unread = item.unread)
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.iconSmall))
                Text(item.timeText, style = MaterialTheme.typography.bodyMedium, color = wb.muted)
            }
        }
        // Message card: the longer explanation, when there is one.
        if (item.detail.isNotEmpty()) {
            GlassBlock(spacing = Space.s) {
                BlockLabel(stringResource(R.string.notification_detail_message))
                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.paragraph,
                    color = wb.text.copy(alpha = 0.88f),
                )
            }
        }
        // Activity card: what happened to this notification.
        GlassBlock(spacing = Space.xs) {
            BlockLabel(stringResource(R.string.notif_detail_activity))
            ActivityRow(
                icon = Icons.Filled.Notifications,
                tint = wb.brand,
                container = wb.brandSoft,
                title = stringResource(R.string.notif_detail_event_received),
                time = item.timeText,
                showLine = !item.unread,
            )
            if (!item.unread) {
                ActivityRow(
                    icon = Icons.Filled.Check,
                    tint = wb.green,
                    container = wb.greenSoft,
                    title = stringResource(R.string.notif_detail_event_read),
                    time = stringResource(R.string.notif_detail_event_read_now),
                    showLine = false,
                )
            }
        }
    }
}

@Composable
private fun BlockLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.overline,
        color = MaterialTheme.wb.muted,
    )
}

/** "Read" / "New" capsule with an icon, tinted green or brand. */
@Composable
private fun StatusChip(unread: Boolean) {
    val wb = MaterialTheme.wb
    // The shared status pill, tinted like "Live" elsewhere.
    Pill(
        text = stringResource(if (unread) R.string.notification_detail_unread else R.string.notification_detail_read),
        icon = if (unread) Icons.Filled.Notifications else Icons.Filled.CheckCircle,
        tint = if (unread) wb.brand else wb.green,
        background = if (unread) wb.brandSoft else wb.greenSoft,
    )
}

/** One timeline entry: a round icon, with a thin line down to the next entry when [showLine] is set. */
@Composable
private fun ActivityRow(
    icon: ImageVector,
    tint: Color,
    container: Color,
    title: String,
    time: String,
    showLine: Boolean,
) {
    val wb = MaterialTheme.wb
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Sizes.icon))
            }
            if (showLine) {
                Box(Modifier.width(2.dp).weight(1f).padding(vertical = Space.xxxs).background(wb.border, PillShape))
            }
        }
        Column(
            modifier = Modifier.padding(bottom = if (showLine) Space.m else 0.dp),
            verticalArrangement = Arrangement.spacedBy(Space.xxxs),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = wb.text)
            Text(time, style = MaterialTheme.typography.bodyMedium, color = wb.muted)
        }
    }
}

@ThemePreviews
@Composable
private fun NotificationDetailPreview() {
    val item =
        AppNotification(
            id = "1",
            source = "Petal & Stem",
            message = "Your latest changes are live.",
            timeText = "2 mins ago",
            unread = false,
            logoRes = R.drawable.petal_stem_logo,
            detail = "Deploy 10 finished successfully and your latest changes are now live on petalandstem.webblock.com.",
            actionTitle = "Open Monitor",
        )
    PreviewSurface(padding = 0) {
        NotificationDetailContent(state = NotificationDetailUiState("1", DataState.Success(listOf(item))), onEvent = {})
    }
}
