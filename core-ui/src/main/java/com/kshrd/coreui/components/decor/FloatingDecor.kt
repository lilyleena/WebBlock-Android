package com.kshrd.coreui.components.decor

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb

/** True when the user turned animations off in system settings (the Android "reduce motion"). */
@Composable
fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember {
        runCatching { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
    }
}

/** Plain white sign-in backdrop (dark navy in dark mode), then [content] on top. */
@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.wb.canvas)) {
        content()
    }
}

/**
 * The page behind every portal screen: the web sign-in panel's "blueprint" header. A soft brand wash at
 * the top (web `bg/hero-top`) with a faint square grid and a few lightly filled cells clustered top-right,
 * all fading into the flat page colour by [HeaderFade], so cards and lists below sit on a clean page.
 * Static: nothing animates. It scrolls with nothing; it is the page itself.
 */
@Composable
fun LiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val wb = MaterialTheme.wb
    val wash = if (wb.isDark) wb.brandSoft.copy(alpha = 0.55f) else wb.brandSoft
    val line = wb.brand.copy(alpha = if (wb.isDark) 0.06f else 0.05f)
    val cell = wb.brand.copy(alpha = if (wb.isDark) 0.08f else 0.05f)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(wb.canvas)
                .drawBehind { drawBlueprintHeader(wash, line, cell, page = wb.canvas) },
        content = content,
    )
}

private val HeaderFade = 360.dp
private val GridCell = 40.dp

// Filled cells as (column from the right, row from the top): a loose cluster like the web panel.
private val FilledCells = listOf(1 to 1, 2 to 1, 1 to 2, 3 to 2, 2 to 3, 4 to 0, 0 to 3, 3 to 4)

private fun DrawScope.drawBlueprintHeader(
    wash: Color,
    line: Color,
    cell: Color,
    page: Color,
) {
    val fade = HeaderFade.toPx()
    val step = GridCell.toPx()
    val w = size.width
    // 1. Wash: brand tint at the top, gone by `fade`.
    drawRect(Brush.verticalGradient(0f to wash, 1f to page, endY = fade), size = Size(w, fade))
    // 2. Filled cells, anchored to the right edge so the cluster sits behind the bell / avatar side.
    val right = w - (w % step)
    FilledCells.forEach { (col, row) ->
        val x = right - (col + 1) * step
        val y = row * step
        val strength = (1f - y / fade).coerceIn(0f, 1f)
        drawRect(cell.copy(alpha = cell.alpha * strength), topLeft = Offset(x, y), size = Size(step, step))
    }
    // 3. Grid lines, each fading with depth so the grid dissolves into the page.
    val stroke = 1.dp.toPx()
    var x = right
    while (x > 0f) {
        drawLine(Brush.verticalGradient(0f to line, 1f to Color.Transparent, endY = fade), Offset(x, 0f), Offset(x, fade), stroke)
        x -= step
    }
    var y = 0f
    while (y < fade) {
        drawLine(line.copy(alpha = line.alpha * (1f - y / fade)), Offset(0f, y), Offset(w, y), stroke)
        y += step
    }
}

/** The WebBlock mark: a blue rounded square with four dots, as in the Figma header. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val shape = RoundedCornerShape(size * 0.28f)
    val dotSize = size * 0.2f
    val gap = size * 0.1f
    Box(
        modifier =
            modifier
                .size(size)
                .shadow(6.dp, shape, ambientColor = Color(0xFF4379D6).copy(alpha = 0.3f), spotColor = Color(0xFF4379D6).copy(alpha = 0.3f))
                .background(Brush.verticalGradient(listOf(Color(0xFF6C9BEA), Color(0xFF3F72D0))), shape)
                .clearAndSetSemantics { },
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gap)) {
            repeat(2) {
                androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gap)) {
                    repeat(2) {
                        Box(Modifier.size(dotSize).background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(dotSize * 0.3f)))
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun AuthBackgroundPreview() {
    PreviewSurface(padding = 0) { AuthBackground() }
}
