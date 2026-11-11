package com.kshrd.coreui.components.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private const val SHIMMER_TRAVEL = 1000f
private const val SHIMMER_DURATION_MS = 1500

/** Animated gradient used by skeleton placeholders while a screen loads. */
@Composable
fun ShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val shimmerColors = listOf(base.copy(alpha = 0.6f), base, base.copy(alpha = 0.6f))

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = SHIMMER_TRAVEL,
            animationSpec = infiniteRepeatable(animation = tween(SHIMMER_DURATION_MS), repeatMode = RepeatMode.Restart),
            label = "shimmer",
        )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translate.value, y = translate.value),
    )
}
