@file:Suppress("MagicNumber") // Animation timings and the splash-icon geometry are the design itself.

package com.kshrd.starter.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.brand.WebBlocksWordmark
import com.kshrd.coreui.components.decor.rememberReduceMotion
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb
import com.kshrd.starter.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Same size as the Android 12+ splash icon: the adaptive icon is drawn 240dp wide and masked to a
// 160dp circle. (The system splash itself shows no icon; see values-v31/themes.xml.)
private val IconCircle = 160.dp
private val IconLayer = 240.dp * 0.68f // ic_launcher_foreground insets the art by 16% per side.
private val WordHeight = 40.dp
private val WordGap = 12.dp

/** How far the icon starts below its final spot, so it begins centred exactly like the system splash. */
private val IconStartOffset = (WordHeight + WordGap) / 2

/**
 * Cold-start splash: the logo pops in centred, then lifts a little while the "WebBlocks" name rises
 * and wipes in below it. After a short hold it fades away and calls
 * [onFinished]. With system animations off it shows the finished logo briefly instead.
 */
@Composable
fun AppSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    val finish by rememberUpdatedState(onFinished)
    val icon = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val lift = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val word = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            // 1. The logo pops in on its own, centred.
            icon.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
            // 2. Then it lifts while the name rises and wipes in below it.
            coroutineScope {
                launch { lift.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
                launch {
                    delay(120)
                    word.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                }
            }
        }
        delay(if (reduceMotion) 700 else 600)
        exit.animateTo(1f, tween(if (reduceMotion) 150 else 320))
        finish()
    }

    AppSplashContent(
        icon = icon.value,
        lift = lift.value,
        word = word.value,
        exit = exit.value,
        modifier = modifier,
    )
}

/**
 * The splash drawn at a given point of its animation: [icon] (logo pops in), [lift] (logo moves up),
 * [word] (name reveal) and [exit] (fade-out), each 0..1 ([icon] may overshoot slightly).
 */
@Composable
private fun AppSplashContent(
    icon: Float,
    lift: Float,
    word: Float,
    exit: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { IconStartOffset.toPx() }
    val wordRisePx = with(density) { 14.dp.toPx() }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - exit
                    val grow = 1f + 0.04f * exit
                    scaleX = grow
                    scaleY = grow
                }.background(MaterialTheme.wb.background)
                // Swallow taps so nothing underneath reacts while the splash is up.
                .clickable(interactionSource = null, indication = null) {},
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .graphicsLayer {
                            translationY = startOffsetPx * (1f - lift)
                            alpha = icon.coerceIn(0f, 1f)
                            val pop = 0.6f + 0.4f * icon
                            scaleX = pop
                            scaleY = pop
                        }
                        .size(IconCircle)
                        .clip(CircleShape)
                        .background(Color.White), // The adaptive icon's own background layer.
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_art),
                    contentDescription = null, // The wordmark below carries the name.
                    modifier = Modifier.requiredSize(IconLayer),
                )
            }
            Spacer(Modifier.height(WordGap))
            WebBlocksWordmark(
                height = WordHeight,
                modifier =
                    Modifier
                        .graphicsLayer {
                            alpha = word
                            translationY = wordRisePx * (1f - word)
                        }.drawWithContent {
                            // Left-to-right wipe, slightly ahead of the fade so the letters "write" in.
                            val reveal = (word * 1.25f).coerceAtMost(1f)
                            clipRect(right = size.width * reveal) { this@drawWithContent.drawContent() }
                        },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AppSplashContentPreview() {
    PreviewSurface { AppSplashContent(icon = 1f, lift = 1f, word = 1f, exit = 0f) }
}
