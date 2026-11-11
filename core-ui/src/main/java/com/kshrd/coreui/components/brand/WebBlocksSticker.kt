@file:Suppress("MagicNumber") // Sticker geometry and motion timings: the numbers are the drawing itself.

package com.kshrd.coreui.components.brand

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.decor.rememberReduceMotion
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val StickerTop = Color(0xFFFCFBFF)
private val StickerMid = Color(0xFFEBE9FD)
private val StickerBottom = Color(0xFFD6CFFA)
private val TabTop = Color(0xFFFFFFFF)
private val TabBottom = Color(0xFFE3E0F8)
private val SparkBlue = Color(0xFF2F5BFF)
private val EdgeLavender = Color(0x33705CE0)
private val FaceInk = Color(0xFF161B2E)
private val CheekPink = Color(0xFFFDA4BC)
private val StickerShadow = Color(0xFF6E5CF0)

// Layout inside the square canvas, as fractions of its side: the face block sits low-left so the
// tab and the sparks have room off its top-right corner.
private const val BlockSide = 0.66f
private const val BlockX = 0.08f
private const val BlockY = 0.25f
private const val BlockCorner = 0.26f

// Spark lines off the top-right corner: angle (degrees, 0 = right, -90 = up) of each line.
private val SparkAngles = floatArrayOf(-82f, -34f, 10f)

/**
 * The WebBlocks face as a glossy 3D sticker: a lavender block with a small tab behind its top-right
 * corner, three blue spark lines and a soft violet shadow. It pops in with a bounce, then floats with
 * a slight tilt, blinks now and then and its sparks twinkle; a tap makes it hop and the sparks burst.
 * Everything stays still when system animations are off. Decorative (no content description).
 */
@Composable
fun WebBlocksSticker(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
) {
    val reduceMotion = rememberReduceMotion()
    val density = LocalDensity.current
    val floatPx = with(density) { 3.dp.toPx() }
    val hopPx = with(density) { 10.dp.toPx() }

    // Pop-in once per screen (not again every time the list scrolls it back into view).
    var entered by rememberSaveable { mutableStateOf(false) }
    val enter = remember { Animatable(if (entered || reduceMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!entered && !reduceMotion) {
            delay(150)
            enter.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
        }
        entered = true
    }

    val idle = rememberInfiniteTransition(label = "stickerIdle")
    val bob by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val tilt by idle.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tilt",
    )
    val sparkPhase by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "spark",
    )

    val eyes = remember { Animatable(1f) }
    val hop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(2400, 4600))
            repeat(if (Random.nextInt(4) == 0) 2 else 1) {
                eyes.animateTo(0.1f, tween(70))
                eyes.animateTo(1f, tween(110))
            }
        }
    }
    val shadowPaint = remember { Paint() }

    Canvas(
        modifier
            .size(size)
            .graphicsLayer {
                if (!reduceMotion) {
                    val e = enter.value
                    alpha = e.coerceIn(0f, 1f)
                    translationY = bob * floatPx - hop.value * hopPx + (1f - e) * hopPx * 2
                    rotationZ = tilt + (1f - e) * -30f
                    // Grow in from small; stretch up on a hop, squash slightly as it lands.
                    val grow = 0.4f + 0.6f * e
                    scaleY = grow * (1f + hop.value * 0.08f)
                    scaleX = grow * (1f - hop.value * 0.05f)
                    transformOrigin = TransformOrigin(0.5f, 0.9f)
                }
            }.pointerInput(reduceMotion) {
                if (reduceMotion) return@pointerInput
                detectTapGestures {
                    scope.launch {
                        hop.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
                        hop.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
                    }
                    scope.launch {
                        eyes.animateTo(0.25f, tween(90))
                        delay(260)
                        eyes.animateTo(1f, tween(160))
                    }
                }
            },
    ) {
        val phase = if (reduceMotion) 0.25f else sparkPhase
        drawSticker(shadowPaint, eyeOpen = eyes.value, sparkPhase = phase, burst = hop.value)
    }
}

private fun DrawScope.drawSticker(
    shadowPaint: Paint,
    eyeOpen: Float,
    sparkPhase: Float,
    burst: Float,
) {
    val w = size.width
    val b = w * BlockSide
    val bx = w * BlockX
    val by = w * BlockY
    val corner = b * BlockCorner

    // Block-relative point: (0, 0) is the block's top-left corner, (1, 1) its bottom-right.
    fun at(
        x: Float,
        y: Float,
    ) = Offset(bx + x * b, by + y * b)

    // Soft violet drop shadow under the block.
    drawIntoCanvas { canvas ->
        val fp = shadowPaint.asFrameworkPaint()
        fp.color = StickerShadow.copy(alpha = 0.32f).toArgb()
        fp.maskFilter = BlurMaskFilter(w * 0.04f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawRoundRect(bx + b * 0.04f, by + b * 0.08f, bx + b * 0.98f, by + b * 1.04f, corner, corner, shadowPaint)
    }

    // The little tab peeking out behind the top-right corner.
    val tab = at(0.62f, -0.12f)
    val tabSide = b * 0.3f
    drawRoundRect(
        Brush.verticalGradient(listOf(TabTop, TabBottom), startY = tab.y, endY = tab.y + tabSide),
        tab,
        Size(tabSide, tabSide),
        CornerRadius(tabSide * 0.38f),
    )

    // Glossy lavender block with a bright glass rim.
    drawRoundRect(
        Brush.linearGradient(listOf(StickerTop, StickerMid, StickerBottom), start = at(0f, 0f), end = at(0.7f, 1f)),
        at(0f, 0f),
        Size(b, b),
        CornerRadius(corner),
    )
    drawRoundRect(
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.1f)), start = at(0f, 0f), end = at(1f, 1f)),
        at(0f, 0f),
        Size(b, b),
        CornerRadius(corner),
        style = Stroke(width = w * 0.014f),
    )
    drawRoundRect(EdgeLavender, at(0f, 0f), Size(b, b), CornerRadius(corner), style = Stroke(width = w * 0.006f))

    // Face: big glossy eyes (squashed when blinking), rosy cheeks and a small smile.
    val eyeR = b * 0.085f
    listOf(at(0.3f, 0.44f), at(0.7f, 0.44f)).forEach { c ->
        drawOval(FaceInk, Offset(c.x - eyeR, c.y - eyeR * eyeOpen), Size(eyeR * 2, eyeR * 2 * eyeOpen))
        if (eyeOpen > 0.6f) drawCircle(Color.White, eyeR * 0.38f, c + Offset(eyeR * 0.35f, -eyeR * 0.35f))
    }
    listOf(at(0.17f, 0.62f), at(0.83f, 0.62f)).forEach { c ->
        drawOval(CheekPink.copy(alpha = 0.75f), Offset(c.x - b * 0.075f, c.y - b * 0.045f), Size(b * 0.15f, b * 0.09f))
    }
    val smileW = b * 0.16f
    drawArc(
        FaceInk,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = at(0.5f, 0.5f) - Offset(smileW / 2, 0f),
        size = Size(smileW, smileW * 0.8f),
        style = Stroke(width = b * 0.045f, cap = StrokeCap.Round),
    )

    // Three spark lines fanning out from the top-right corner; they twinkle in turn and burst on a tap.
    val c = at(0.92f, 0.08f)
    SparkAngles.forEachIndexed { i, deg ->
        val t = ((sparkPhase - i * 0.14f) % 1f + 1f) % 1f
        val pulse = (1f - cos(2f * PI.toFloat() * t)) / 2f
        val rad = deg * PI.toFloat() / 180f
        val dir = Offset(cos(rad), sin(rad))
        val start = b * (0.28f + 0.03f * pulse + 0.08f * burst)
        val len = b * (0.15f + 0.05f * pulse + 0.05f * burst)
        drawLine(
            SparkBlue.copy(alpha = 0.75f + 0.25f * pulse),
            c + dir * start,
            c + dir * (start + len),
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round,
        )
    }
}

@ThemePreviews
@Composable
private fun WebBlocksStickerPreview() {
    PreviewSurface { WebBlocksSticker() }
}
