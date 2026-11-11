package com.kshrd.coreui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/** Card surface used everywhere on iOS (Liquid Glass): the card colour at ~92% so the page decor whispers through. */
@Composable
fun glassFill(tint: Color? = null): Color {
    val base = MaterialTheme.wb.card.copy(alpha = 0.92f)
    return tint?.compositeOver(base) ?: base
}

/**
 * Android stand-in for iOS `.glass(in:tint:)`: a near-opaque card-coloured fill clipped to [shape],
 * optionally washed with [tint]. Use for pills, fields and small tiles.
 */
@Composable
fun Modifier.glass(
    shape: Shape,
    tint: Color? = null,
): Modifier = this.clip(shape).background(glassFill(tint), shape)

/** The hairline every card carries: 1dp brand@12%. */
@Composable
fun cardBorder(): BorderStroke = BorderStroke(1.dp, MaterialTheme.wb.brand.copy(alpha = 0.12f))

/** The barely-there shadow under every card: no coloured glow. */
fun Modifier.cardShadow(shape: Shape = CardShape): Modifier =
    shadow(3.dp, shape, clip = false, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))

/** iOS `wbCard()`: radius 24 glass card, 1dp brand@12% border, very soft shadow, inner [padding]. */
@Composable
fun Modifier.wbCard(padding: Dp = Space.m): Modifier =
    this
        .fillMaxWidth()
        .cardShadow()
        .glass(CardShape)
        .border(cardBorder(), CardShape)
        .padding(padding)

/** Composable form of [wbCard]. */
@Composable
fun WbCard(
    modifier: Modifier = Modifier,
    padding: Dp = Space.m,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.wbCard(padding), content = content)
}

@ThemePreviews
@Composable
private fun WbCardPreview() {
    PreviewSurface {
        WbCard { Text("Business details", style = MaterialTheme.typography.titleMedium) }
    }
}
