package com.kshrd.coreui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * Rounded gradient-glass block: card colour fading into a soft brand tint, a light rim and a whisper of
 * shadow. Use it for the separate sections of a detail page.
 */
@Composable
fun GlassBlock(
    modifier: Modifier = Modifier,
    spacing: Dp = Space.m,
    contentPadding: Dp = Space.m,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.card)
    val fill = Brush.linearGradient(listOf(wb.card, wb.brandSoft.copy(alpha = if (wb.isDark) 0.45f else 0.55f)))
    val rim = Brush.linearGradient(listOf(Color.White.copy(alpha = if (wb.isDark) 0.18f else 0.95f), wb.brand.copy(alpha = 0.16f)))
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(4.dp, shape, ambientColor = wb.brand.copy(alpha = 0.08f), spotColor = wb.brand.copy(alpha = 0.12f))
                .clip(shape)
                .background(fill, shape)
                .border(BorderStroke(1.dp, rim), shape)
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@ThemePreviews
@Composable
private fun GlassBlockPreview() {
    PreviewSurface {
        GlassBlock { Text("Rounded gradient glass") }
    }
}
