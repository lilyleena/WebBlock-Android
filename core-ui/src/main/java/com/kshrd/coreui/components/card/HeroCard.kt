package com.kshrd.coreui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius

val HeroShape = RoundedCornerShape(Radius.sheet)

/** The brand gradient: our primary blue easing into a deeper shade of itself. */
@Composable
fun primaryGradient(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    return Brush.linearGradient(listOf(primary, lerp(primary, Color.Black, 0.28f)))
}

/**
 * Large rounded card in the primary gradient with two soft translucent circles for depth. Put the
 * headline content of a screen in it (counts, status). Text inside should use `onPrimary`.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth().clip(HeroShape).background(primaryGradient())) {
        val glow = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f)
        Box(Modifier.align(Alignment.TopEnd).offset(x = 40.dp, y = (-40).dp).size(160.dp).background(glow, CircleShape))
        Box(Modifier.align(Alignment.BottomEnd).offset(x = (-30).dp, y = 50.dp).size(110.dp).background(glow, CircleShape))
        Column(content = content)
    }
}

@ThemePreviews
@Composable
private fun HeroCardPreview() {
    PreviewSurface {
        HeroCard {
            Text("12", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
