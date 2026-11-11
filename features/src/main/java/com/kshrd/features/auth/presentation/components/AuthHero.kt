package com.kshrd.features.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.components.brand.WebBlocksLogoInline
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.WbColors
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

/** How far the form sheet's rounded top rises over the hero. */
val SheetOverlap = 24.dp

/** Heights below which the hero drops parts, so the form never has to scroll. */
private val ShowHeadlineMin = 150.dp
private val ShowPreviewMin = 120.dp

/**
 * The sign-in hero, after the web sign-in panel: a soft brand wash with mesh glows and a blueprint grid,
 * the logo, a two-tone headline ([plain] in text colour, [accent] in brand blue) and the web's
 * browser-and-phone mockup of a WebBlock site. It adapts to the height it gets: the mockup scales down,
 * then disappears, then the headline does, so the form below always fits (also with the keyboard open).
 */
@Composable
fun AuthHero(
    plain: String,
    accent: String,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .drawBehind { drawWebPanel(wb) },
    ) {
        val h = maxHeight
        Column(
            // The panel runs under the status bar; its bottom edge sits under the form sheet's rounded top.
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = Space.screen).padding(top = Space.s, bottom = SheetOverlap + Space.m),
            verticalArrangement = Arrangement.spacedBy(Space.m),
        ) {
            WebBlocksLogoInline(height = 30.dp)
            if (h >= ShowHeadlineMin) {
                Text(
                    text =
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = wb.text)) { append(plain) }
                            withStyle(SpanStyle(color = wb.brand)) { append(accent) }
                        },
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (maxHeight >= ShowPreviewMin) {
                    // Width from the height available (the mockup is 644 x 529), never wider than the column.
                    val w = minOf(maxWidth, maxHeight * (644f / 529f))
                    SitePreview(modifier = Modifier.width(w))
                }
            }
        }
    }
}

/**
 * The web sign-in panel background: `bg/hero-top` wash fading down, two soft brand "mesh" glows, a faint
 * grid and a few lightly filled cells clustered top-right.
 */
private fun DrawScope.drawWebPanel(wb: WbColors) {
    val w = size.width
    val h = size.height
    drawRect(wb.canvas)
    drawRect(Brush.verticalGradient(0f to wb.brandSoft, 1f to wb.canvas, endY = h * 0.9f))
    val glow = wb.brand.copy(alpha = if (wb.isDark) 0.16f else 0.10f)
    drawCircle(Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(w * 0.95f, h * 0.05f), radius = w * 0.7f), radius = w * 0.7f, center = Offset(w * 0.95f, h * 0.05f))
    drawCircle(Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(w * 0.05f, h * 0.75f), radius = w * 0.6f), radius = w * 0.6f, center = Offset(w * 0.05f, h * 0.75f))
    val step = 34.dp.toPx()
    val line = wb.brand.copy(alpha = if (wb.isDark) 0.08f else 0.06f)
    val stroke = 1.dp.toPx()
    var x = w
    while (x > 0f) {
        drawLine(line, Offset(x, 0f), Offset(x, h), stroke)
        x -= step
    }
    var y = step
    while (y < h) {
        drawLine(line, Offset(0f, y), Offset(w, y), stroke)
        y += step
    }
    val cell = wb.brand.copy(alpha = if (wb.isDark) 0.10f else 0.06f)
    val right = w - (w % step)
    listOf(0 to 1, 1 to 2, 2 to 1, 3 to 3, 1 to 4, 5 to 2, 0 to 6).forEach { (col, row) ->
        drawRect(cell, Offset(right - (col + 1) * step, row * step), Size(step, step))
    }
}

/** Traffic-light dots of a desktop browser window (web Figma `color/red/67`, `orange/59`, `green/47`). */
private val WindowDots = listOf(Color(0xFFFF5F57), Color(0xFFFEBC2E), Color(0xFF28C840))

/** The web's address-bar text for the sample site (the same Petal & Stem site the mockup shows). */
private const val PREVIEW_DOMAIN = "petalandstem.webblock.com"

/**
 * The web sign-in panel's mockup: a desktop browser window showing a WebBlock site, with a phone showing
 * the same site overlapping its lower right. Frames are drawn natively; the page images are the real
 * site screenshots from the web Figma. Geometry is the Figma layout scaled to [modifier]'s width:
 * browser 87.6% wide, phone 35.6% wide at (64.4%, 10.9%), whole thing 644 x 529.
 */
@Composable
private fun SitePreview(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.clearAndSetSemantics { }) {
        val w = maxWidth
        Box(Modifier.fillMaxWidth().height(w * (529f / 644f))) {
            BrowserWindow(scale = w / 340.dp, modifier = Modifier.width(w * 0.876f))
            PhoneFrame(
                Modifier
                    .offset(x = w * 0.644f, y = w * (70f / 644f))
                    .width(w * 0.356f),
            )
        }
    }
}

@Composable
private fun BrowserWindow(
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.control * scale.coerceIn(0.6f, 1f))
    val dot: Dp = 7.dp * scale.coerceIn(0.6f, 1f)
    Column(
        modifier =
            modifier
                .shadow(16.dp, shape, ambientColor = wb.brand.copy(alpha = 0.10f), spotColor = wb.brand.copy(alpha = 0.20f))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, wb.border, shape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dot * 0.7f),
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5FC)).padding(horizontal = dot * 1.6f, vertical = dot),
        ) {
            WindowDots.forEach { Box(Modifier.size(dot).background(it, CircleShape)) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                modifier =
                    Modifier
                        .padding(start = dot * 1.6f, end = dot * 3f)
                        .weight(1f)
                        .background(Color.White, CircleShape)
                        .padding(vertical = 2.dp),
            ) {
                Icon(WebIcons.Lock, contentDescription = null, tint = Color(0xFF5A6B8C), modifier = Modifier.size(dot * 1.1f))
                Text(PREVIEW_DOMAIN, fontSize = (8f * scale.coerceIn(0.75f, 1f)).sp, color = Color(0xFF33486F), maxLines = 1)
            }
        }
        // The page: top of the real desktop screenshot, full width (Figma's 562 x 410 viewport).
        Image(
            painterResource(R.drawable.auth_site_desktop),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth().aspectRatio(562f / 410f).background(Color.White),
        )
    }
}

/** A modern phone: black bezel, rounded screen with the mobile screenshot, Dynamic Island, home bar. */
@Composable
private fun PhoneFrame(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val pw = maxWidth
        val outer = RoundedCornerShape(pw * 0.17f)
        val inner = RoundedCornerShape(pw * 0.145f)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(229f / 459f)
                .shadow(14.dp, outer, ambientColor = Color.Black.copy(alpha = 0.22f), spotColor = Color.Black.copy(alpha = 0.32f))
                .background(Color(0xFF14161C), outer)
                .padding(pw * 0.03f),
        ) {
            Image(
                painterResource(R.drawable.auth_site_mobile),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(inner)
                        .background(Color.White)
                        .padding(top = pw * 0.12f),
            )
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = pw * 0.035f)
                    .width(pw * 0.32f)
                    .height(pw * 0.095f)
                    .background(Color.Black, CircleShape),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = pw * 0.02f)
                    .width(pw * 0.33f)
                    .height(pw * 0.012f)
                    .background(Color.Black.copy(alpha = 0.8f), CircleShape),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AuthHeroPreview() {
    PreviewSurface(padding = 0) {
        Box(Modifier.height(400.dp)) { AuthHero(plain = "Pick up right\nwhere you ", accent = "left off.") }
    }
}
