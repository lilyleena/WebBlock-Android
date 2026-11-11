package com.kshrd.features.website.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb

// Fixed brand colours (not theme tokens) so white text on the header stays readable in light and dark
// mode. Taken from the logo's blocks: royal blue -> indigo, with a violet and a sky glow.
private val HeaderRoyal = Color(0xFF2448D8)
private val HeaderIndigo = Color(0xFF4F62F0)
private val GlowViolet = Color(0xFF8B5CF6)
private val GlowSky = Color(0xFF4FB0FF)

/**
 * The websites list's header backdrop: a royal-blue -> indigo gradient with a violet glow top-right and
 * a sky glow on the left, a few faint floating blocks echoing the logo, all fading into [page] by
 * [fadeEnd] (about the bottom of the welcome card).
 */
fun Modifier.portalHeaderBackdrop(
    page: Color,
    fadeEnd: Dp,
): Modifier =
    drawBehind {
        val end = fadeEnd.toPx()
        drawRect(page)
        drawRect(Brush.verticalGradient(0f to HeaderRoyal, 0.42f to HeaderIndigo, 1f to page, endY = end))
        glow(GlowViolet.copy(alpha = 0.6f), Offset(size.width * 1.02f, end * 0.04f), size.width * 0.72f)
        glow(GlowSky.copy(alpha = 0.32f), Offset(0f, end * 0.4f), size.width * 0.55f)
        floatingBlocks()
    }

private fun DrawScope.glow(
    color: Color,
    center: Offset,
    radius: Float,
) = drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius), radius, center)

/** Faint rounded squares drifting in the header: two outlined, two filled, at small tilts. */
private fun DrawScope.floatingBlocks() {
    fun block(
        x: Float,
        y: Dp,
        side: Dp,
        degrees: Float,
        filled: Boolean,
    ) {
        val s = side.toPx()
        val topLeft = Offset(x - s / 2, y.toPx() - s / 2)
        rotate(degrees, pivot = topLeft + Offset(s / 2, s / 2)) {
            val corner = CornerRadius(s * 0.28f)
            if (filled) {
                drawRoundRect(Color.White.copy(alpha = 0.1f), topLeft, Size(s, s), corner)
            } else {
                drawRoundRect(Color.White.copy(alpha = 0.16f), topLeft, Size(s, s), corner, style = Stroke(1.4.dp.toPx()))
            }
        }
    }
    block(size.width * 0.55f, 22.dp, 44.dp, 16f, filled = false)
    block(size.width * 0.66f, 96.dp, 16.dp, -14f, filled = true)
    block(size.width * 0.42f, 118.dp, 10.dp, 24f, filled = true)
    block(size.width * 0.08f, 150.dp, 26.dp, -10f, filled = false)
}

@ThemePreviews
@Composable
private fun PortalHeaderBackdropPreview() {
    PreviewSurface(padding = 0) {
        Box(Modifier.fillMaxWidth().height(400.dp).portalHeaderBackdrop(MaterialTheme.wb.background, 380.dp))
    }
}
