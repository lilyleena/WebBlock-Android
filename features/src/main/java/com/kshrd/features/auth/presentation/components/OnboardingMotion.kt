package com.kshrd.features.auth.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.components.decor.rememberReduceMotion
import com.kshrd.coreui.theme.wb
import kotlinx.coroutines.delay

/**
 * Frosted card for the illustration mock-ups (iOS `obGlass`): solid card colour (so overlapping cards never show through each other),
 * a faint brand outline, a white highlight rim and a whisper of shadow.
 */
@Composable
fun Modifier.obGlass(cornerRadius: Dp = 22.dp): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val wb = MaterialTheme.wb
    val rim = Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.9f)))
    return this
        .shadow(4.dp, shape, ambientColor = wb.brand.copy(alpha = 0.07f), spotColor = wb.brand.copy(alpha = 0.10f))
        .background(wb.card, shape)
        .border(1.dp, wb.brand.copy(alpha = 0.10f), shape)
        .border(BorderStroke(1.5.dp, rim), shape)
}

/** Gentle endless bob so the mock-ups feel alive (iOS `obFloat`). Still when system animations are off. */
@Composable
fun Modifier.obFloat(
    distance: Dp = 8.dp,
    durationMs: Int = 3000,
    delayMs: Int = 0,
): Modifier {
    if (rememberReduceMotion()) return this
    val transition = rememberInfiniteTransition(label = "ob-float")
    val phase by transition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(delayMs),
            ),
        label = "ob-float-phase",
    )
    return this.graphicsLayer { translationY = phase * distance.toPx() }
}

/** Springs a view in (slide + fade) once its page becomes [active] (iOS `Entrance`). */
@Composable
fun Modifier.entrance(
    active: Boolean,
    delayMs: Int = 0,
    x: Dp = 0.dp,
    y: Dp = 40.dp,
): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) delay(delayMs.toLong())
        progress.animateTo(if (active) 1f else 0f, spring(dampingRatio = 0.78f, stiffness = 80f))
    }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationX = (1f - p) * x.toPx()
        translationY = (1f - p) * y.toPx()
    }
}

/** A font size that ignores the user's font scale, for text inside the fixed-size illustrations. */
@Composable
fun fixedSp(size: Float): TextUnit = with(LocalDensity.current) { (size / fontScale).sp }

/** The plain onboarding canvas (iOS `OnboardingBackdrop`, minus the dot grid and floating shapes). */
@Composable
fun OnboardingBackdrop(content: @Composable BoxScope.() -> Unit) {
    val wb = MaterialTheme.wb
    Box(modifier = Modifier.fillMaxSize().background(wb.canvas)) {
        content()
    }
}
