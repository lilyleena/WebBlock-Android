package com.kshrd.features.monitor.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.WbCard
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.monitor.domain.model.MonitorData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val PERCENT = 100

/** iOS "Summary" card: the success ring next to uptime / products / photos bars. */
@Composable
fun SummaryCard(data: MonitorData) {
    val wb = MaterialTheme.wb
    val plan = data.plan
    WbCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.m)) {
            SuccessRing(succeeded = data.succeededCount, failed = data.failedCount)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                MetricBar(
                    icon = WebIcons.Globe,
                    tint = wb.green,
                    title = stringResource(R.string.bm_uptime),
                    value = data.uptimePercent?.let { stringResource(R.string.bm_percent_decimal, it) } ?: stringResource(R.string.bm_dash),
                    fraction = ((data.uptimePercent ?: 0.0) / PERCENT).toFloat(),
                )
                MetricBar(
                    icon = WebIcons.Package,
                    tint = wb.brand,
                    title = stringResource(R.string.bm_products),
                    value = stringResource(R.string.bm_used_of, usageUsed(data, true), plan.maxProducts),
                    fraction = usageUsed(data, true).toFloat() / max(plan.maxProducts, 1),
                )
                MetricBar(
                    icon = Icons.Outlined.Image,
                    tint = wb.amber,
                    title = stringResource(R.string.bm_photos),
                    value = stringResource(R.string.bm_used_of, usageUsed(data, false), plan.maxPhotos),
                    fraction = usageUsed(data, false).toFloat() / max(plan.maxPhotos, 1),
                )
            }
        }
    }
}

private fun usageUsed(
    data: MonitorData,
    products: Boolean,
): Int =
    data.usage
        .firstOrNull { (it.kind == com.kshrd.features.monitor.domain.model.UsageKind.Products) == products }
        ?.used ?: 0

/** Ring with the success percentage in the middle. */
@Composable
private fun SuccessRing(
    succeeded: Int,
    failed: Int,
) {
    val wb = MaterialTheme.wb
    val total = succeeded + failed
    val fraction = if (total == 0) 0f else succeeded.toFloat() / total
    val percent = (fraction * PERCENT).roundToInt()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1000, delayMillis = 250, easing = FastOutSlowInEasing)) }
    val brand = wb.brand
    val description = stringResource(R.string.bm_percent_succeeded, percent)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(112.dp).clearAndSetSemantics { contentDescription = description }) {
        Canvas(modifier = Modifier.size(112.dp)) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(brand.copy(alpha = 0.12f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                brush = Brush.verticalGradient(listOf(Color(0xFF6C9BEA), brand)),
                startAngle = -90f,
                sweepAngle = 360f * max(0.001f, fraction * progress.value),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(percent * progress.value).roundToInt()}%", style = MaterialTheme.typography.headlineSmall, color = wb.text)
            Text(stringResource(R.string.bm_success), style = MaterialTheme.typography.labelMedium, color = wb.muted)
        }
    }
}

/** Icon tile + title + value on top, a thin coloured progress bar underneath. */
@Composable
private fun MetricBar(
    icon: ImageVector,
    tint: Color,
    title: String,
    value: String,
    fraction: Float,
) {
    val wb = MaterialTheme.wb
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val animated by animateFloatAsState(if (shown) fraction.coerceIn(0f, 1f) else 0f, tween(900, delayMillis = 300), label = "metric")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$title: $value" },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).background(wb.card.copy(alpha = 0.85f), RoundedCornerShape(10.dp)),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = wb.text)
                Text(value, style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
            Bar(fraction = animated, tint = tint, track = tint.copy(alpha = 0.14f), minWidth = 6.dp)
        }
    }
}

/** A 6dp capsule bar: [track] with [tint] filling [fraction] of its width. */
@Composable
private fun Bar(
    fraction: Float,
    tint: Color,
    track: Color,
    minWidth: androidx.compose.ui.unit.Dp,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(6.dp).clip(PillShape).background(track)) {
        Box(Modifier.fillMaxHeight().width(max(minWidth.value, maxWidth.value * fraction).dp).clip(PillShape).background(tint))
    }
}

/** iOS "Plan usage" card: redeploys and websites tiles with a resets-on line. */
@Composable
fun PlanUsageCard(data: MonitorData) {
    val wb = MaterialTheme.wb
    val plan = data.plan
    WbCard {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.m)) {
                UsageTile(WebIcons.Refresh, wb.purple, stringResource(R.string.bm_redeploys), data.redeploysUsed, plan.maxRedeploys, Modifier.weight(1f))
                UsageTile(Icons.Outlined.Layers, wb.brand, stringResource(R.string.bm_websites_label), data.websitesUsed, plan.maxWebsites, Modifier.weight(1f))
            }
            HorizontalDivider(color = wb.border)
            Text(
                stringResource(R.string.bm_plan_resets, plan.name, data.resetsOn),
                style = MaterialTheme.typography.labelMedium,
                color = wb.muted,
            )
        }
    }
}

/** Plan allowance tile: big count, "left" status and a row of block segments. */
@Composable
private fun UsageTile(
    icon: ImageVector,
    tint: Color,
    title: String,
    used: Int,
    limit: Int,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val left = max(limit - used, 0)
    val full = used >= limit
    val segments = min(max(limit, 1), 10)
    val filled = (min(used.toFloat() / max(limit, 1), 1f) * segments).roundToInt()
    val caption = if (full) stringResource(R.string.bm_limit_reached) else stringResource(R.string.bm_left, left)
    Column(
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier =
            modifier
                .background(tint.copy(alpha = 0.07f), RoundedCornerShape(Radius.panel))
                .padding(Space.s)
                .clearAndSetSemantics { contentDescription = "$title: $used / $limit, $caption" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Sizes.iconSmall))
            Text(title, style = MaterialTheme.typography.labelLarge, color = wb.text)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
            Text("$used", style = MaterialTheme.typography.headlineLarge, color = wb.text)
            Text(
                stringResource(R.string.bm_slash_limit, limit),
                style = MaterialTheme.typography.bodyMedium,
                color = wb.muted,
                modifier = Modifier.padding(bottom = Space.xxs),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(segments) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(if (i < filled) tint else tint.copy(alpha = 0.16f), RoundedCornerShape(3.dp)),
                )
            }
        }
        Text(
            caption,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (full) wb.amber else wb.muted,
        )
    }
}

/** iOS "Health metrics": three tinted tiles (deploys, failed, average time). */
@Composable
fun HealthTiles(data: MonitorData) {
    val wb = MaterialTheme.wb
    val total = max(data.totalDeploys, 1)
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        HealthTile(
            icon = Icons.AutoMirrored.Filled.Send,
            tint = wb.brand,
            soft = wb.brandSoft,
            title = stringResource(R.string.bm_tile_deploys),
            value = "${data.totalDeploys}",
            caption = stringResource(R.string.bm_in_total),
            modifier = Modifier.weight(1f),
        )
        HealthTile(
            icon = WebIcons.Alert,
            tint = wb.red,
            soft = wb.redSoft,
            title = stringResource(R.string.bm_tile_failed),
            value = "${data.failedCount}",
            barFraction = data.failedCount.toFloat() / total,
            modifier = Modifier.weight(1f),
        )
        HealthTile(
            icon = WebIcons.Clock,
            tint = wb.amber,
            soft = wb.amberSoft,
            title = stringResource(R.string.bm_tile_avg_time),
            value = data.averageDurationSeconds?.let(::formatAverageDuration) ?: stringResource(R.string.bm_dash),
            caption = stringResource(R.string.bm_per_deploy),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HealthTile(
    icon: ImageVector,
    tint: Color,
    soft: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    barFraction: Float? = null,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.panel)
    Column(
        verticalArrangement = Arrangement.spacedBy(Space.xxs),
        modifier =
            modifier
                .heightIn(min = 138.dp)
                .background(soft.copy(alpha = 0.9f), shape)
                .border(1.dp, tint.copy(alpha = 0.18f), shape)
                .padding(Space.s)
                .semantics(mergeDescendants = true) {},
    ) {
        Box(
            contentAlignment = Alignment.Center,
            // Card colour, not white, so the tile is not a bright square in dark mode.
            modifier = Modifier.padding(bottom = Space.xxs).size(32.dp).background(wb.card.copy(alpha = 0.9f), RoundedCornerShape(10.dp)),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Sizes.iconSmall))
        }
        Text(title, style = MaterialTheme.typography.labelMedium, color = wb.muted)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = wb.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(Modifier.weight(1f, fill = true).height(6.dp))
        if (barFraction != null) {
            Bar(fraction = barFraction.coerceIn(0f, 1f), tint = tint, track = wb.card.copy(alpha = 0.8f), minWidth = 8.dp)
        } else if (caption != null) {
            Text(caption, style = MaterialTheme.typography.labelMedium, color = wb.muted)
        }
    }
}

/** iOS "Deploy activity": a bar per deploy, green for succeeded and red for failed, rising to the latest. */
@Composable
fun DeployActivityCard(data: MonitorData) {
    val wb = MaterialTheme.wb
    val results = data.activityResults
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    WbCard {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.bm_deploys_count, data.totalDeploys),
                    style = MaterialTheme.typography.titleLarge,
                    color = wb.text,
                    modifier = Modifier.weight(1f),
                )
                Legend(wb.green, stringResource(R.string.bm_succeeded))
                Box(Modifier.width(Space.s))
                Legend(wb.red, stringResource(R.string.bm_failed))
            }
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Column(modifier = Modifier.fillMaxWidth().height(150.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    repeat(3) { HorizontalDivider(color = wb.border.copy(alpha = 0.6f)) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    results.forEachIndexed { i, ok ->
                        val fraction = 0.42f + 0.58f * i / max(results.size - 1, 1)
                        val height by animateFloatAsState(
                            targetValue = if (shown) 150f * fraction else 6f,
                            animationSpec = tween(550, delayMillis = 200 + i * 50, easing = FastOutSlowInEasing),
                            label = "bar$i",
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(height.dp)
                                .background(if (ok) wb.green else wb.red, PillShape),
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.bm_deploy_n, 1), style = MaterialTheme.typography.labelMedium, color = wb.muted)
                Text(stringResource(R.string.bm_deploy_n, data.totalDeploys), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
        }
    }
}

@Composable
private fun Legend(
    color: Color,
    title: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.wb.muted)
    }
}

@ThemePreviews
@Composable
private fun SummaryComponentsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            SummaryCard(previewMonitor)
            PlanUsageCard(previewMonitor)
            HealthTiles(previewMonitor)
            DeployActivityCard(previewMonitor)
        }
    }
}
