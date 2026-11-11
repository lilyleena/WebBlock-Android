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

/** Card surface (web `--color-bg-surface`): solid, so text contrast never depends on what is behind it. */
@Composable
fun glassFill(tint: Color? = null): Color {
    val base = MaterialTheme.wb.card
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

/** The hairline every card carries (web `--color-border-subtle`). */
@Composable
fun cardBorder(): BorderStroke = BorderStroke(1.dp, MaterialTheme.wb.border)

/** Web `Shadow/Card`: a soft, low, blue-tinted lift (#3C78CF at 5–12%), never a grey smudge. */
fun Modifier.cardShadow(shape: Shape = CardShape): Modifier =
    shadow(8.dp, shape, clip = false, ambientColor = CardShadowTint.copy(alpha = 0.06f), spotColor = CardShadowTint.copy(alpha = 0.14f))

private val CardShadowTint = Color(0xFF3C78CF)

/** Web dashboard card: radius 20, solid surface, 1dp hairline border, soft blue shadow, inner [padding]. */
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
