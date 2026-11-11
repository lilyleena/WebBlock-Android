package com.kshrd.coreui.components.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import kotlinx.coroutines.delay

val FormCardShape = RoundedCornerShape(Radius.sheet)

/**
 * Big section card for forms and detail sheets: a bold title (with an optional action on the right),
 * then airy content.
 *
 * - Default: solid white, for the soft grey page colour.
 * - [glass] = true: frosted look (translucent fill, light gradient rim). Put it on a [GlassBackdrop]
 *   so there is something coloured to see through.
 * - [enterDelayMillis] >= 0 fades and slides the card up when it first appears, after that delay,
 *   so a column of cards can be staggered.
 */
@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    glass: Boolean = false,
    enterDelayMillis: Int = -1,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = FormCardShape,
        color = if (glass) scheme.surface.copy(alpha = 0.62f) else scheme.surface,
        border = if (glass) BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.15f)))) else null,
        modifier = modifier.fillMaxWidth().entrance(enterDelayMillis),
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(Space.m)) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = Space.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (actionLabel != null && onAction != null) AppTextAction(text = actionLabel, onClick = onAction)
                }
            }
            content()
        }
    }
}

/** Fade + rise-in once, [delayMillis] after the first composition. Negative = no animation. */
private fun Modifier.entrance(delayMillis: Int): Modifier =
    if (delayMillis < 0) {
        this
    } else {
        composed {
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(delayMillis.toLong())
                progress.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
            }
            graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 36.dp.toPx()
            }
        }
    }

/**
 * Page background for glass cards: a soft brand wash with two slowly drifting colour glows, so the
 * frosted cards have something to pick up.
 */
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val drift by rememberInfiniteTransition(label = "glass-drift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(scheme.primaryContainer.copy(alpha = 0.55f), scheme.surfaceContainer, scheme.surfaceContainer)))
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val a = Offset(w * (0.80f - 0.25f * drift), h * (0.18f + 0.06f * drift))
                    val b = Offset(w * (0.15f + 0.20f * drift), h * (0.62f - 0.08f * drift))
                    drawCircle(Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.28f), Color.Transparent), a, w * 0.6f), w * 0.6f, a)
                    drawCircle(Brush.radialGradient(listOf(scheme.tertiary.copy(alpha = 0.18f), Color.Transparent), b, w * 0.55f), w * 0.55f, b)
                },
        content = content,
    )
}

@ThemePreviews
@Composable
private fun FormCardPreview() {
    PreviewSurface { FormCard(title = "Contact") { Text("012 345 678", modifier = Modifier.padding(horizontal = 20.dp)) } }
}
