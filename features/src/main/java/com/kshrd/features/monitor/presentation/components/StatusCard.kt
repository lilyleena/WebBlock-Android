package com.kshrd.features.monitor.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.cardBorder
import com.kshrd.coreui.components.card.cardShadow
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.monitor.domain.model.Deploy
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.model.Plan
import com.kshrd.features.monitor.domain.model.SiteHealth

/** Heights (0..1, bottom up) of the line's points, as in the iOS Status card. */
private val Rise = floatArrayOf(0.12f, 0.28f, 0.36f, 0.5f, 0.58f, 0.76f, 0.9f)

/**
 * iOS `StatusCard`: online banner with a pulsing ring, then a smooth line through the last seven deploys
 * (blue dot = succeeded, red = failed), and a "Visit site" link.
 */
@Composable
fun StatusCard(
    data: MonitorData,
    onVisitSite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val online = data.health == SiteHealth.Online
    val tint = if (online) wb.green else wb.red
    val shape = CardShape
    val drawn = remember { Animatable(0f) }
    LaunchedEffect(Unit) { drawn.animateTo(1f, tween(1200, delayMillis = 300)) }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "pulse",
    )
    val brand = wb.brand
    Column(
        verticalArrangement = Arrangement.spacedBy(Space.m),
        modifier =
            modifier
                .fillMaxWidth()
                .cardShadow()
                .glass(shape)
                .border(cardBorder(), shape)
                .padding(Space.m),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                Box(
                    Modifier
                        .size(64.dp)
                        .scale(1f + 0.35f * pulse)
                        .border(2.dp, tint.copy(alpha = 0.35f * 0.9f * (1f - pulse)), CircleShape),
                )
                Box(Modifier.size(64.dp).background(wb.brandSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        if (online) WebIcons.Globe else Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = if (online) wb.brand else wb.red,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(
                    stringResource(if (online) R.string.bm_status_online else R.string.bm_status_offline),
                    style = MaterialTheme.typography.titleMedium,
                    color = wb.text,
                )
                Text(stringResource(R.string.bm_last_deploy, data.lastDeployAgo), style = MaterialTheme.typography.bodyMedium, color = wb.muted)
                Text(data.lastDeployDetail, style = MaterialTheme.typography.labelMedium, color = wb.muted)
            }
            // The same status pill used for "Live" on the website cards.
            Pill(
                text = stringResource(if (online) R.string.bm_live_badge else R.string.bm_offline_badge),
                tint = tint,
                background = if (online) wb.greenSoft else wb.redSoft,
                uppercase = true,
            )
        }
        HorizontalDivider(color = wb.border)
        DeployLine(results = data.recentTimeline, progress = drawn.value)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.bm_last_seven), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            Text(stringResource(R.string.bm_latest), style = MaterialTheme.typography.labelMedium, color = wb.muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (online) R.string.bm_changes_live else R.string.bm_site_offline),
                style = MaterialTheme.typography.bodyMedium,
                color = wb.text,
                modifier = Modifier.weight(1f).padding(end = Space.xs),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                modifier = Modifier.clip(PillShape).clickable(role = Role.Button, onClick = onVisitSite).padding(horizontal = Space.xxs, vertical = Space.xs),
            ) {
                Text(stringResource(R.string.bm_visit_site), style = MaterialTheme.typography.labelLarge, color = wb.brand)
                Icon(WebIcons.ExternalLink, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.iconSmall))
            }
        }
    }
}

/** Smooth line through the deploy results with a soft fill under it and a dot per deploy. */
@Composable
private fun DeployLine(
    results: List<Deploy>,
    progress: Float,
) {
    val wb = MaterialTheme.wb
    val brand = wb.brand
    val red = wb.red
    val n = results.size
    Canvas(modifier = Modifier.fillMaxWidth().height(96.dp).padding(horizontal = 6.dp)) {
        if (n == 0) return@Canvas
        val points =
            List(n) { i ->
                val x = if (n == 1) 0.5f else i.toFloat() / (n - 1)
                Offset(x * size.width, size.height - Rise[minOf(i, Rise.lastIndex)] * size.height)
            }
        val line = smoothPath(points)
        if (points.size > 1) {
            val fill = Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(brand.copy(alpha = 0.18f * progress), brand.copy(alpha = 0f))))
            val measure = PathMeasure().apply { setPath(line, false) }
            val segment = Path()
            measure.getSegment(0f, measure.length * progress, segment, true)
            drawPath(segment, brand, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        points.forEachIndexed { i, p ->
            val pop = (progress * n - i).coerceIn(0f, 1f)
            if (pop > 0f) {
                val r = 5.5.dp.toPx() * pop
                drawCircle(if (results[i].succeeded) brand else red, r, p)
                drawCircle(Color.White, r, p, style = Stroke(width = 2.dp.toPx() * pop))
            }
        }
    }
}

/** Catmull-Rom through [pts], as cubic Béziers (the iOS `SmoothLine`). */
private fun smoothPath(pts: List<Offset>): Path {
    val path = Path()
    if (pts.size < 2) return path
    path.moveTo(pts[0].x, pts[0].y)
    for (i in 0 until pts.lastIndex) {
        val p0 = if (i > 0) pts[i - 1] else pts[i]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = if (i + 2 < pts.size) pts[i + 2] else p2
        path.cubicTo(
            p1.x + (p2.x - p0.x) / 6f,
            p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f,
            p2.y - (p3.y - p1.y) / 6f,
            p2.x,
            p2.y,
        )
    }
    return path
}

internal val previewMonitor =
    MonitorData(
        health = SiteHealth.Online,
        plan = Plan("Plus", false, maxProducts = 30, maxPhotos = 60, maxRedeploys = 5, maxWebsites = 1),
        uptimePercent = 99.98,
        deploys =
            listOf(
                Deploy(10, 1_792_000_000_000, 101, true, "Maya Sok", "2 days (current)"),
                Deploy(9, 1_791_700_000_000, 98, true, "Maya Sok", "3 days"),
                Deploy(8, 1_791_300_000_000, 69, false, "Maya Sok", null),
                Deploy(7, 1_790_900_000_000, 104, true, "Maya Sok", "7 days"),
                Deploy(6, 1_790_500_000_000, 112, true, "Maya Sok", "6 days"),
                Deploy(5, 1_790_100_000_000, 107, false, "Maya Sok", null),
                Deploy(4, 1_789_700_000_000, 72, true, "Maya Sok", "11 days"),
            ),
        usage = emptyList(),
        logs = emptyList(),
        lastDeployAgo = "2 days ago",
        lastDeployDetail = "Deploy 10 · 18 Oct, 2:15 PM",
        redeploysUsed = 5,
        websitesUsed = 1,
        resetsOn = "1 Nov",
        succeededTotal = 8,
        failedTotal = 2,
    )

@ThemePreviews
@Composable
private fun StatusCardPreview() {
    PreviewSurface { StatusCard(previewMonitor, onVisitSite = {}) }
}
