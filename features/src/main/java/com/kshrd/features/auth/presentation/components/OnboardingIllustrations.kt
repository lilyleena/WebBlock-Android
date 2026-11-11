@file:Suppress("MagicNumber") // Fixed-size illustrations: every number is a measurement from the iOS drawing.

package com.kshrd.features.auth.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import kotlinx.coroutines.delay

private val SiteInk = Color(0xFF1A273D)
private val BlueTop = Color(0xFF6C9BEA)
private val BlueBottom = Color(0xFF3F72D0)
private val BlueSheen = Brush.verticalGradient(listOf(BlueTop, BlueBottom))

/** Page 1: the petalandstem.webblock.com site in a glass browser with a phone preview. */
@Composable
fun WebsiteIllustration(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400)),
        label = "pulse-progress",
    )
    Box(modifier = modifier.fillMaxWidth().height(340.dp), contentAlignment = Alignment.Center) {
        Browser(active)
        PhoneMockup(
            Modifier
                .entrance(active, 500, x = 40.dp, y = 30.dp)
                .obFloat(8.dp, 3400, 300)
                .offset(x = 104.dp, y = 50.dp)
                .rotate(4f),
        )
        Row(
            modifier =
                Modifier
                    .entrance(active, 300, x = 30.dp, y = (-20).dp)
                    .obFloat(6.dp, 2600, 300)
                    .offset(x = 96.dp, y = (-118).dp)
                    .obGlass(16.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(8.dp)) {
                    drawCircle(wb.green)
                    val ringScale = 1f + 1.2f * pulse
                    drawCircle(wb.green.copy(alpha = 0.5f * (1f - pulse)), radius = size.minDimension / 2 * ringScale, style = Stroke(2.dp.toPx()))
                }
            }
            Text(stringResource(R.string.auth_ob_live), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = wb.text)
        }
        Box(
            modifier =
                Modifier
                    .entrance(active, 450, x = (-30).dp, y = 20.dp)
                    .obFloat(9.dp, 3600, 500)
                    .offset(x = (-120).dp, y = 122.dp)
                    .size(54.dp)
                    .obGlass(27.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Language, contentDescription = null, tint = wb.brand, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun Browser(active: Boolean) {
    val wb = MaterialTheme.wb
    val products =
        listOf(
            Triple(R.drawable.blue_lily_stem, R.string.auth_ob_product_1, R.string.auth_ob_price_1),
            Triple(R.drawable.gift_bouquet, R.string.auth_ob_product_2, R.string.auth_ob_price_2),
            Triple(R.drawable.stone_vase, R.string.auth_ob_product_3, R.string.auth_ob_price_3),
        )
    Column(
        modifier =
            Modifier
                .entrance(active, 50)
                .obFloat(6.dp, 3200)
                .offset(x = (-14).dp, y = (-8).dp)
                .rotate(-3f)
                .width(268.dp)
                .obGlass(22.dp)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(wb.red, wb.amber, wb.green).forEach { Box(Modifier.size(7.dp).background(it.copy(alpha = 0.85f), CircleShape)) }
            Row(
                modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.6f), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Lock, null, tint = wb.muted, modifier = Modifier.size(7.dp))
                Text(
                    stringResource(R.string.auth_ob_url),
                    fontSize = fixedSp(9f),
                    fontWeight = FontWeight.Medium,
                    color = wb.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(12.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.auth_ob_hero_headline), fontSize = fixedSp(11f), fontWeight = FontWeight.Bold, color = SiteInk)
                Box(Modifier.size(width = 44.dp, height = 12.dp).background(SiteInk, CircleShape), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.auth_ob_shop_flowers), fontSize = fixedSp(6f), fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                }
            }
            Image(
                painter = painterResource(R.drawable.table_arrangement),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 96.dp, height = 70.dp).clip(RoundedCornerShape(8.dp)),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            products.forEachIndexed { index, (image, name, price) ->
                Column(modifier = Modifier.weight(1f).entrance(active, 350 + index * 100, y = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Image(
                        painter = painterResource(image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(6.dp)),
                    )
                    Text(stringResource(name), fontSize = fixedSp(6f), fontWeight = FontWeight.Medium, color = SiteInk, maxLines = 1)
                    Text(stringResource(price), fontSize = fixedSp(6f), fontWeight = FontWeight.Bold, color = wb.brand)
                }
            }
        }
    }
}

/** Page 3: a product card and business-hours card, tilted and stacked like the reference. */
@Composable
fun ProductsIllustration(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Box(modifier = modifier.fillMaxWidth().height(340.dp), contentAlignment = Alignment.Center) {
        // Business info card (back)
        Column(
            modifier =
                Modifier
                    .entrance(active, 250, x = 40.dp, y = 30.dp)
                    .obFloat(8.dp, 3400, 400)
                    .offset(x = 54.dp, y = 104.dp)
                    .rotate(7f)
                    .width(190.dp)
                    .obGlass(22.dp)
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(30.dp).background(wb.greenSoft, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Schedule, null, tint = wb.green, modifier = Modifier.size(15.dp))
                }
                Text(stringResource(R.string.auth_ob_open_today), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = wb.text)
            }
            Text(stringResource(R.string.auth_ob_hours), style = MaterialTheme.typography.bodySmall, color = wb.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("M", "T", "W", "T", "F").forEach { day ->
                    Box(Modifier.size(22.dp).background(wb.brand, CircleShape), contentAlignment = Alignment.Center) {
                        Text(day, fontSize = fixedSp(10f), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        // Product card (front)
        Column(
            modifier =
                Modifier
                    .entrance(active, 50, x = (-40).dp, y = 30.dp)
                    .obFloat(7.dp, 3000)
                    .offset(x = (-44).dp, y = (-34).dp)
                    .rotate(-6f)
                    .width(196.dp)
                    .obGlass(22.dp)
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.gift_bouquet),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(14.dp)),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.auth_ob_product_2), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = wb.text)
                    Text(stringResource(R.string.auth_ob_in_stock), style = MaterialTheme.typography.labelSmall, color = wb.green)
                }
                Text(stringResource(R.string.auth_ob_price_2), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = wb.brand)
            }
        }
        // Add badge
        Box(
            modifier =
                Modifier
                    .entrance(active, 450, x = 20.dp, y = (-30).dp)
                    .obFloat(9.dp, 2800, 600)
                    .offset(x = 112.dp, y = (-98).dp)
                    .shadow(14.dp, CircleShape, ambientColor = wb.brand.copy(alpha = 0.45f), spotColor = wb.brand.copy(alpha = 0.45f))
                    .size(54.dp)
                    .background(BlueSheen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

private val BarHeights = listOf(0.45f, 0.7f, 0.55f, 0.9f, 0.65f, 0.8f, 1.0f)

/** Page 4: a monitor card with a ring, bar chart and a notification bubble. */
@Composable
fun MonitorIllustration(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val progress = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            delay(300)
            progress.animateTo(1f, tween(1200))
        } else {
            progress.snapTo(0f)
        }
    }
    Box(modifier = modifier.fillMaxWidth().height(340.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier =
                Modifier
                    .entrance(active, 50)
                    .obFloat(7.dp, 3300)
                    .rotate(-3f)
                    .width(270.dp)
                    .obGlass(24.dp)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.auth_ob_site_health), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = wb.text)
                Text(
                    stringResource(R.string.auth_ob_succeeded),
                    fontSize = fixedSp(9f),
                    fontWeight = FontWeight.ExtraBold,
                    color = wb.green,
                    modifier = Modifier.background(wb.greenSoft, CircleShape).padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                UptimeRing(progress = progress.value)
                Row(
                    modifier = Modifier.height(74.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BarHeights.forEachIndexed { index, fraction -> Bar(fraction, active, index) }
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .entrance(active, 400, x = 30.dp, y = 30.dp)
                    .obFloat(8.dp, 2900, 400)
                    .offset(x = 28.dp, y = 112.dp)
                    .rotate(4f)
                    .obGlass(20.dp)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(30.dp).background(wb.amber, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Notifications, null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(stringResource(R.string.auth_ob_new_order), fontSize = fixedSp(11f), fontWeight = FontWeight.Bold, color = wb.text)
                Text(stringResource(R.string.auth_ob_just_now), fontSize = fixedSp(9f), color = wb.muted)
            }
        }
        Box(
            modifier =
                Modifier
                    .entrance(active, 500, x = (-30).dp, y = (-20).dp)
                    .obFloat(9.dp, 3500, 700)
                    .offset(x = (-118).dp, y = (-96).dp)
                    .size(56.dp)
                    .obGlass(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.VerifiedUser, null, tint = wb.green, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun UptimeRing(progress: Float) {
    val wb = MaterialTheme.wb
    Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(84.dp)) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(wb.brand.copy(alpha = 0.18f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(BlueSheen, -90f, 360f * progress * 0.98f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.auth_ob_uptime_value), fontSize = fixedSp(15f), fontWeight = FontWeight.Bold, color = wb.text)
            Text(stringResource(R.string.auth_ob_uptime), fontSize = fixedSp(9f), color = wb.muted)
        }
    }
}

@Composable
private fun Bar(
    fraction: Float,
    active: Boolean,
    index: Int,
) {
    val wb = MaterialTheme.wb
    val grow = remember { Animatable(0.1f) }
    LaunchedEffect(active) {
        if (active) delay(250L + index * 60L)
        grow.animateTo(if (active) 1f else 0.1f, spring(dampingRatio = 0.7f, stiffness = 120f))
    }
    val isLast = index == BarHeights.lastIndex
    Box(
        Modifier
            .size(width = 10.dp, height = (74f * fraction * grow.value).dp)
            .background(if (isLast) wb.brand else wb.brand.copy(alpha = 0.35f), CircleShape),
    )
}

@ThemePreviews
@Composable
private fun WebsiteIllustrationPreview() {
    PreviewSurface { WebsiteIllustration(active = true) }
}

@ThemePreviews
@Composable
private fun ProductsIllustrationPreview() {
    PreviewSurface { ProductsIllustration(active = true) }
}

@ThemePreviews
@Composable
private fun MonitorIllustrationPreview() {
    PreviewSurface { MonitorIllustration(active = true) }
}
