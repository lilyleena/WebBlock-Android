package com.kshrd.features.monitor.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.ResultPill
import com.kshrd.coreui.components.card.WbCard
import com.kshrd.coreui.components.card.wbCard
import com.kshrd.coreui.components.text.Section
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.monitor.domain.model.Deploy
import com.kshrd.features.monitor.domain.model.LogCategory
import com.kshrd.features.monitor.domain.model.LogEntry
import com.kshrd.features.monitor.domain.model.LogOutcome
import com.kshrd.features.monitor.domain.model.MonitorData

private const val COLLAPSED_DEPLOYS = 4

// ---- Recent deploys --------------------------------------------------------------------------

/** iOS "Recent deploys" card: compact rows that expand in place; "Show all N deploys" for the rest. */
@Composable
fun RecentDeploysCard(data: MonitorData) {
    val wb = MaterialTheme.wb
    var expandedDeploy by rememberSaveable { mutableStateOf<Int?>(null) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val rows = if (showAll) data.deploys else data.deploys.take(COLLAPSED_DEPLOYS)
    val chevron by animateFloatAsState(if (showAll) 180f else 0f, label = "show-all-chevron")
    WbCard {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.bm_recent_deploys), style = MaterialTheme.typography.titleMedium, color = wb.text, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.bm_tap_for_details), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
            // Rows sit closer to each other than to the header.
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                rows.forEach { deploy ->
                    DeployRow(
                        deploy = deploy,
                        isExpanded = expandedDeploy == deploy.number,
                        onToggle = { expandedDeploy = if (expandedDeploy == deploy.number) null else deploy.number },
                    )
                }
            }
            if (data.deploys.size > COLLAPSED_DEPLOYS) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.compact).clip(RoundedCornerShape(Radius.tile)).clickable(role = Role.Button) { showAll = !showAll },
                ) {
                    Text(
                        if (showAll) stringResource(R.string.bm_show_less) else stringResource(R.string.bm_show_all, data.deploys.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = wb.brand,
                    )
                    Icon(WebIcons.ChevronDown, contentDescription = null, tint = wb.brand, modifier = Modifier.padding(start = Space.xxs).size(Sizes.icon).rotate(chevron))
                }
            }
        }
    }
}

/** One deploy: a compact summary row that expands in place to show who ran it, how long it took, etc. */
@Composable
private fun DeployRow(
    deploy: Deploy,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val wb = MaterialTheme.wb
    val ok = deploy.succeeded
    val shape = RoundedCornerShape(Radius.control)
    val chevron by animateFloatAsState(if (isExpanded) 180f else 0f, label = "deploy-chevron")
    // Gradient glass: card colour washing into the result's soft tint, with a bright-to-tinted rim.
    val accent = if (ok) wb.brand else wb.red
    val accentSoft = if (ok) wb.brandSoft else wb.redSoft
    val fill =
        Brush.linearGradient(
            listOf(
                wb.card.copy(alpha = if (wb.isDark) 0.9f else 0.95f),
                accentSoft.copy(alpha = if (isExpanded) 0.95f else if (wb.isDark) 0.5f else 0.7f),
            ),
        )
    val rim =
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (wb.isDark) 0.18f else 0.95f),
                accent.copy(alpha = if (isExpanded) 0.5f else 0.22f),
            ),
        )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape, ambientColor = accent.copy(alpha = 0.10f), spotColor = accent.copy(alpha = 0.12f))
                .clip(shape)
                .background(fill, shape)
                .border(BorderStroke(1.dp, rim), shape)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onToggle).padding(Space.s),
        ) {
            StatusDisc(icon = if (ok) WebIcons.Check else WebIcons.X, tint = if (ok) wb.green else wb.red, soft = if (ok) wb.greenSoft else wb.redSoft)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(stringResource(R.string.bm_deploy_hash, deploy.number), style = MaterialTheme.typography.labelLarge, color = wb.text)
                Text(formatDeployDate(deploy.atMillis), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
            ResultPill(ok, stringResource(R.string.bm_result_success), stringResource(R.string.bm_failed))
            Icon(WebIcons.ChevronDown, contentDescription = null, tint = wb.muted, modifier = Modifier.size(20.dp).rotate(chevron))
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier.padding(horizontal = Space.s).padding(bottom = Space.s),
            ) {
                HorizontalDivider(color = wb.border)
                DeployDetail(WebIcons.User, stringResource(R.string.bm_started_by), deploy.startedBy)
                DeployDetail(WebIcons.Clock, stringResource(R.string.bm_duration), formatDeployDuration(deploy.durationSeconds))
                deploy.liveFor?.let { DeployDetail(WebIcons.Globe, stringResource(R.string.bm_live_for), it) }
            }
        }
    }
}

@Composable
private fun DeployDetail(
    icon: ImageVector,
    title: String,
    value: String,
) {
    val wb = MaterialTheme.wb
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        Icon(icon, contentDescription = null, tint = wb.brand, modifier = Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = wb.muted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = wb.text)
    }
}

@Composable
private fun StatusDisc(
    icon: ImageVector,
    tint: Color,
    soft: Color,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).background(soft, CircleShape)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

// ---- Activity log ----------------------------------------------------------------------------

/** iOS "Recent activity log": one card of expandable rows (icon, title, short date). */
@Composable
fun ActivityLog(logs: List<LogEntry>) {
    val wb = MaterialTheme.wb
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    Section(stringResource(R.string.bm_recent_activity)) {
        Column(modifier = Modifier.wbCard(padding = 0.dp)) {
            if (logs.isEmpty()) {
                Text(
                    stringResource(R.string.bm_no_activity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = wb.muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = Space.l),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            logs.forEachIndexed { index, item ->
                ActivityRow(item, open = expanded == item.id, onToggle = { expanded = if (expanded == item.id) null else item.id })
                if (index < logs.lastIndex) HorizontalDivider(color = wb.border, modifier = Modifier.padding(start = 58.dp, end = Space.s))
            }
        }
    }
}

@Composable
private fun ActivityRow(
    item: LogEntry,
    open: Boolean,
    onToggle: () -> Unit,
) {
    val wb = MaterialTheme.wb
    val failed = item.outcome == LogOutcome.Failed
    val chevron by animateFloatAsState(if (open) 180f else 0f, label = "activity-chevron")
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(if (open) wb.brandSoft.copy(alpha = 0.35f) else Color.Transparent)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onToggle).padding(Space.s),
        ) {
            StatusDisc(icon = iconFor(item), tint = tintFor(item), soft = softFor(item))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(item.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium), color = wb.text)
                Text(formatActivityDate(item.atMillis), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
            Icon(WebIcons.ChevronDown, contentDescription = null, tint = wb.muted, modifier = Modifier.size(20.dp).rotate(chevron))
        }
        AnimatedVisibility(visible = open) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Space.xs),
                modifier = Modifier.padding(start = 42.dp + Space.s, end = Space.s, bottom = Space.s),
            ) {
                DetailLine(stringResource(R.string.bm_type)) { Pill(text = kindLabel(item.category), tint = kindTint(item.category), background = kindSoft(item.category)) }
                DetailLine(stringResource(R.string.bm_status)) {
                    Text(
                        stringResource(if (failed) R.string.bm_failed else R.string.bm_completed),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (failed) wb.red else wb.green,
                    )
                }
                DetailLine(stringResource(R.string.bm_when)) {
                    Text(formatActivityDateLong(item.atMillis), style = MaterialTheme.typography.bodyMedium, color = wb.text)
                }
                item.detail?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = wb.red,
                        modifier = Modifier.fillMaxWidth().background(wb.redSoft, RoundedCornerShape(Radius.tile)).padding(Space.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.muted, modifier = Modifier.weight(1f))
        value()
    }
}

private fun iconFor(item: LogEntry): ImageVector =
    if (item.outcome == LogOutcome.Failed) {
        WebIcons.X
    } else {
        when (item.category) {
            LogCategory.Deploys -> WebIcons.Check
            LogCategory.Products -> WebIcons.Package
            LogCategory.Business -> WebIcons.Pencil
        }
    }

@Composable
private fun tintFor(item: LogEntry): Color {
    val wb = MaterialTheme.wb
    if (item.outcome == LogOutcome.Failed) return wb.red
    return when (item.category) {
        LogCategory.Deploys -> wb.green
        LogCategory.Products -> wb.brand
        LogCategory.Business -> wb.purple
    }
}

@Composable
private fun softFor(item: LogEntry): Color {
    val wb = MaterialTheme.wb
    if (item.outcome == LogOutcome.Failed) return wb.redSoft
    return when (item.category) {
        LogCategory.Deploys -> wb.greenSoft
        LogCategory.Products -> wb.brandSoft
        LogCategory.Business -> wb.purpleSoft
    }
}

@Composable
private fun kindLabel(category: LogCategory): String =
    stringResource(
        when (category) {
            LogCategory.Deploys -> R.string.bm_kind_deploy
            LogCategory.Products -> R.string.bm_kind_product
            LogCategory.Business -> R.string.bm_kind_business
        },
    )

@Composable
private fun kindTint(category: LogCategory): Color =
    when (category) {
        LogCategory.Deploys -> MaterialTheme.wb.brand
        LogCategory.Products -> MaterialTheme.wb.green
        LogCategory.Business -> MaterialTheme.wb.purple
    }

@Composable
private fun kindSoft(category: LogCategory): Color =
    when (category) {
        LogCategory.Deploys -> MaterialTheme.wb.brandSoft
        LogCategory.Products -> MaterialTheme.wb.greenSoft
        LogCategory.Business -> MaterialTheme.wb.purpleSoft
    }

private val previewLogs =
    listOf(
        LogEntry("1", "Deploy 10 succeeded", LogCategory.Deploys, 1_792_000_000_000, LogOutcome.Success),
        LogEntry("2", "Product “Lily bunch” added", LogCategory.Products, 1_791_900_000_000, LogOutcome.Info),
        LogEntry("3", "Opening hours updated", LogCategory.Business, 1_791_800_000_000, LogOutcome.Info),
        LogEntry("4", "Deploy 8 failed", LogCategory.Deploys, 1_791_300_000_000, LogOutcome.Failed, "Image “spring-tulips.jpg” is over 10 MB"),
    )

@ThemePreviews
@Composable
private fun ActivityComponentsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            ActivityLog(previewLogs)
            RecentDeploysCard(previewMonitor)
        }
    }
}
