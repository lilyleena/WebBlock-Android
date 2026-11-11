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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
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
 * iOS `liquidBackdrop()` behind every portal screen: a soft lavender wash at the top that fades into the
 * page colour, with two large faint circles on the top-right (a light one at the corner, a brand-tinted
 * one lower down), so white cards and the search bar stand off the page. Static: nothing animates.
 */
@Composable
fun LiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val wb = MaterialTheme.wb
    val wash = if (wb.isDark) wb.brandSoft.copy(alpha = 0.35f).compositeOver(wb.canvas) else lerp(wb.brandSoft, wb.purpleSoft, 0.5f)
    val page = if (wb.isDark) wb.canvas else wb.background
    val light = if (wb.isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.6f)
    val tinted = wb.brand.copy(alpha = if (wb.isDark) 0.10f else 0.07f)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    drawRect(page)
                    drawRect(Brush.verticalGradient(0f to wash, 1f to page, endY = w * 1.3f))
                    drawCircle(light, radius = w * 0.34f, center = Offset(w * 0.9f, -w * 0.1f))
                    drawCircle(tinted, radius = w * 0.36f, center = Offset(w * 1.06f, w * 0.58f))
                },
        content = content,
    )
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
