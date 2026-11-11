package com.kshrd.features.notifications.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.GlassBlock
import com.kshrd.coreui.components.card.LogoAvatar
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.FontScalePreviews
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.notifications.domain.model.AppNotification

/** iOS `NotificationAvatar`: website logo, bell tile (WebBlock) or initials tile, with an unread dot on its corner. */
@Composable
fun NotificationAvatar(
    item: AppNotification,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val wb = MaterialTheme.wb
    val dot by animateFloatAsState(if (item.unread) 1f else 0f, spring(dampingRatio = 0.6f, stiffness = 300f), label = "unread-dot")
    Box(modifier = modifier.size(size)) {
        LogoAvatar(
            logoRes = item.logoRes,
            initials = item.initials,
            size = size,
            fallbackIcon = if (item.isFromWebBlock) WebIcons.Bell else null,
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .graphicsLayer {
                    alpha = dot
                    scaleX = dot
                    scaleY = dot
                }.size(12.dp)
                .background(wb.brand, CircleShape)
                .border(2.dp, wb.card, CircleShape),
        )
    }
}

/** A group of notifications in one rounded gradient-glass card, rows split by inset hairlines. */
@Composable
fun NotificationSection(
    items: List<AppNotification>,
    onItemClick: (AppNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassBlock(modifier = modifier, spacing = 0.dp, contentPadding = 0.dp) {
        items.forEachIndexed { index, item ->
            NotificationRow(item = item, onClick = { onItemClick(item) })
            if (index < items.lastIndex) {
                HorizontalDivider(color = MaterialTheme.wb.border.copy(alpha = 0.8f), modifier = Modifier.padding(start = RowDividerInset))
            }
        }
    }
}

/** Divider starts under the text, past the avatar: screen padding + avatar + gap. */
private val RowDividerInset = Space.m + 48.dp + Space.s

/**
 * One notification row: round avatar, the sender (bold while unread) over a two-line message, and the time
 * on the right with an unread dot beneath it. Unread rows carry a faint brand tint.
 */
@Composable
fun NotificationRow(
    item: AppNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val unread = stringResource(R.string.notif_unread)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(if (item.unread) wb.brandSoft.copy(alpha = 0.4f) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = Space.m, vertical = Space.s)
                .semantics(mergeDescendants = true) { if (item.unread) contentDescription = unread },
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            NotificationAvatar(item.copy(unread = false), size = 48.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
            Text(
                text = item.source,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (item.unread) FontWeight.Bold else FontWeight.Medium,
                color = wb.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = wb.muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Text(
                text = item.timeText,
                style = MaterialTheme.typography.labelMedium,
                color = if (item.unread) wb.brand else wb.muted,
                fontWeight = if (item.unread) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (item.unread) {
                Box(Modifier.size(10.dp).background(wb.brand, CircleShape))
            }
        }
    }
}

private val previewItem =
    AppNotification(
        id = "1",
        source = "Petal & Stem",
        message = "Your latest changes are live.",
        timeText = "2 mins ago",
        unread = true,
        logoRes = R.drawable.petal_stem_logo,
    )

@ThemePreviews
@FontScalePreviews
@Composable
private fun NotificationRowPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            NotificationRow(previewItem, onClick = {})
            NotificationRow(previewItem.copy(source = "WebBlock Billing", logoRes = null, unread = false), onClick = {})
            NotificationRow(previewItem.copy(source = "Lantern Café", logoRes = null), onClick = {})
        }
    }
}
