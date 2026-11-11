package com.kshrd.coreui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** iOS `appearAnimated(index:)`: fades, slides up 18dp and scales in from 0.96 once, staggered by [index] (max 8). */
fun Modifier.appearAnimated(index: Int = 0): Modifier =
    composed {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(minOf(index, 8) * 60L)
            progress.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 120f))
        }
        graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * 18.dp.toPx()
            val s = 0.96f + 0.04f * p
            scaleX = s
            scaleY = s
        }
    }

/**
 * iOS `wbCardPress`: a tappable card that springs down on touch and bounces back on release.
 * [pressed] lets the caller add its own press feedback (the "spotify" arrow on website cards).
 */
@Composable
fun rememberPressSource(): Pair<MutableInteractionSource, Boolean> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    return source to pressed
}

fun Modifier.cardPress(
    source: MutableInteractionSource,
    pressed: Boolean,
    scale: Float = 0.97f,
    onClick: () -> Unit,
): Modifier =
    composed {
        val s by animateFloatAsState(
            if (pressed) scale else 1f,
            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
            label = "card-press",
        )
        graphicsLayer {
            scaleX = s
            scaleY = s
        }.clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
    }
