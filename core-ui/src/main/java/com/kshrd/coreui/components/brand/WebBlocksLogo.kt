@file:Suppress("MagicNumber") // Logo geometry: the numbers are the drawing itself.

package com.kshrd.coreui.components.brand

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.kshrd.coreui.R
import com.kshrd.coreui.components.decor.rememberReduceMotion
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.kshrd.coreui.theme.wb
import androidx.compose.material3.MaterialTheme

// The logo is laid out on a 75 x 72 grid (the size of the original artwork): the block mark on top,
// the "WebBlocks" wordmark underneath. Drawing it in code keeps it sharp at every size and density.
private const val GridW = 75f
private const val GridH = 72f
private const val MarkH = 52f

private val BlockBlue = Color(0xFF2566EA)
private val BlockSky = Color(0xFF3B82F6)
private val BlockViolet = Color(0xFF7C4DFF)
private val FaceBlock = Color(0xFFEBE9FD)
private val FaceInk = Color(0xFF161B2E)
private val Blush = Color(0xFFFDB0C6)
private val MotionDot = Color(0xFFC9D4F5)
private val WordInk = Color(0xFF182030)
private val WordViolet = Color(0xFF5A5AFA)
private val WordVioletOnDark = Color(0xFFDCDBFF)

/**
 * The WebBlocks logo (iOS `Image("webblocks-logo")`), drawn at [height]
 * (iOS uses 48dp on onboarding and 56dp on sign-in). [markSize] is a legacy alias for [height].
 */
@Composable
fun WebBlocksLogo(
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    markSize: Dp? = null,
) {
    val h = markSize ?: height
    val unit = h / GridH
    val description = stringResource(R.string.wb_logo_description)
    // Sized from dp (not sp) so the logo keeps its proportions when the user enlarges system text.
    val wordSize = with(LocalDensity.current) { (unit * 13.6f).toSp() }
    // The wordmark flips to its on-dark colours in dark mode, where the page behind it is navy.
    val onDark = MaterialTheme.wb.isDark
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Canvas(Modifier.size(width = unit * GridW, height = unit * MarkH)) { drawMark(size.width / GridW) }
        Text(
            text =
                buildAnnotatedString {
                    withStyle(SpanStyle(color = if (onDark) Color.White else WordInk)) { append(stringResource(R.string.wb_logo_word_web)) }
                    withStyle(SpanStyle(color = if (onDark) WordVioletOnDark else WordViolet)) { append(stringResource(R.string.wb_logo_word_blocks)) }
                },
            fontSize = wordSize,
            lineHeight = 1.2.em,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.01).em,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * One-line logo for toolbars: the block mark with "WebBlocks" beside it, [height] tall. With [onDark]
 * the mark sits on a white rounded tile (its blue blocks would vanish on a blue header) and the
 * wordmark is white / pale lavender.
 */
@Composable
fun WebBlocksLogoInline(
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    onDark: Boolean = false,
) {
    val description = stringResource(R.string.wb_logo_description)
    val wordSize = with(LocalDensity.current) { (height * 0.56f).toSp() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        if (onDark) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(height)
                        .shadow(6.dp, RoundedCornerShape(height * 0.3f), ambientColor = Color(0xFF0E2A66).copy(alpha = 0.35f), spotColor = Color(0xFF0E2A66).copy(alpha = 0.35f))
                        .background(Color.White, RoundedCornerShape(height * 0.3f)),
            ) {
                WebBlocksMark(width = height * 0.66f, trimmed = true)
            }
        } else {
            WebBlocksMark(width = height * (GridW / MarkH))
        }
        Spacer(Modifier.width(height * 0.28f))
        Text(
            text =
                buildAnnotatedString {
                    withStyle(SpanStyle(color = if (onDark) Color.White else WordInk)) { append(stringResource(R.string.wb_logo_word_web)) }
                    withStyle(SpanStyle(color = if (onDark) WordVioletOnDark else WordViolet)) { append(stringResource(R.string.wb_logo_word_blocks)) }
                },
            fontSize = wordSize,
            lineHeight = 1.15.em,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.01).em,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * Just the block mark (no wordmark), [width] wide, e.g. as a sticker on a card. Decorative: it has
 * no content description, so pair it with real text.
 */
@Composable
fun WebBlocksMark(
    modifier: Modifier = Modifier,
    width: Dp = 72.dp,
    trimmed: Boolean = false,
) {
    if (trimmed) {
        // Only the blocks + sparkles (the full grid has empty space on the left), so the mark centres in a tile.
        Canvas(modifier.size(width = width, height = width * (TrimH / TrimW))) {
            val u = size.width / TrimW
            translate(left = -TrimX * u) { drawMark(u) }
        }
    } else {
        Canvas(modifier.size(width = width, height = width * (MarkH / GridW))) { drawMark(size.width / GridW) }
    }
}

// Bounding box of the drawn mark on the grid: x 16..69, y 0..51.
private const val TrimX = 16f
private const val TrimW = 53f
private const val TrimH = 51f

/** Side of the face block on the logo grid; its corner radius is [FaceCorner]. */
private const val FaceSide = 19.2f
private const val FaceCorner = 5f

/**
 * Just the logo's lavender face block (eyes, blush, tiny mouth), [size] square, e.g. as a sticker.
 * Decorative: it has no content description. Its rounded corner is [faceCornerFor] of [size].
 */
@Composable
fun WebBlocksFace(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    Canvas(modifier.size(size)) { drawFace(this.size.width / FaceSide, ox = 0f, oy = 0f) }
}

/**
 * [WebBlocksFace] that feels alive: it floats with a slight tilt, blinks every few seconds, and hops
 * with a squint when tapped. Everything stays still when system animations are turned off. Any
 * [modifier] (shadow, rotation…) moves together with the face.
 */
@Composable
fun AnimatedWebBlocksFace(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val reduceMotion = rememberReduceMotion()
    val density = LocalDensity.current
    val floatPx = with(density) { 3.dp.toPx() }
    val hopPx = with(density) { 10.dp.toPx() }

    val idle = rememberInfiniteTransition(label = "faceIdle")
    val bob by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val tilt by idle.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tilt",
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

    Canvas(
        Modifier
            .graphicsLayer {
                if (!reduceMotion) {
                    translationY = bob * floatPx - hop.value * hopPx
                    rotationZ = tilt
                    // Stretch up on the jump, squash slightly as it lands back.
                    scaleY = 1f + hop.value * 0.08f
                    scaleX = 1f - hop.value * 0.05f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
            }.then(modifier)
            .size(size)
            .pointerInput(reduceMotion) {
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
    ) { drawFace(this.size.width / FaceSide, ox = 0f, oy = 0f, eyeOpen = eyes.value) }
}

/** Corner radius matching [WebBlocksFace] drawn at [size], for clipping or shadows. */
fun faceCornerFor(size: Dp): Dp = size * (FaceCorner / FaceSide)

private const val EyeRadius = 1.35f

/**
 * The face block with its top-left corner at ([ox], [oy]) on the logo grid, [u] px per grid unit.
 * [eyeOpen] squashes the eyes vertically (1 = open, near 0 = shut) for blinking.
 */
private fun DrawScope.drawFace(
    u: Float,
    ox: Float,
    oy: Float,
    eyeOpen: Float = 1f,
) {
    fun at(
        x: Float,
        y: Float,
    ) = Offset((ox + x) * u, (oy + y) * u)

    fun eye(
        cx: Float,
        cy: Float,
    ) = drawOval(
        FaceInk,
        at(cx - EyeRadius, cy - EyeRadius * eyeOpen),
        Size(2 * EyeRadius * u, 2 * EyeRadius * eyeOpen * u),
    )
    drawRoundRect(FaceBlock, at(0f, 0f), Size(FaceSide * u, FaceSide * u), CornerRadius(FaceCorner * u))
    eye(6.6f, 8.5f)
    eye(12.8f, 8.9f)
    if (eyeOpen > 0.6f) {
        drawCircle(Color.White, 0.45f * u, at(7.0f, 8.0f))
        drawCircle(Color.White, 0.45f * u, at(13.2f, 8.4f))
    }
    drawOval(Blush.copy(alpha = 0.85f), at(1.9f, 10.6f), Size(2.6f * u, 1.6f * u))
    drawOval(Blush.copy(alpha = 0.85f), at(13.4f, 12.2f), Size(2.6f * u, 1.6f * u))
    drawLine(FaceInk, at(8.9f, 12.0f), at(10.3f, 12.1f), strokeWidth = 0.6f * u, cap = StrokeCap.Round)
}

/** Four rounded blocks; the top-right one is a lavender face block with motion dots and sparkles. */
private fun DrawScope.drawMark(u: Float) {
    fun block(
        x: Float,
        y: Float,
        color: Color,
    ) = drawRoundRect(color, Offset(x * u, y * u), Size(19.2f * u, 19.2f * u), CornerRadius(5f * u))

    block(17f, 10f, BlockBlue)
    block(17f, 31f, BlockSky)
    block(38f, 31f, BlockViolet)

    // Motion dots trailing the face block (left and below).
    listOf(12.5f, 15.6f, 18.7f, 21.9f).forEach { y -> drawCircle(MotionDot, 0.55f * u, Offset(38.6f * u, y * u)) }
    listOf(41f, 44.5f, 48f, 51.5f).forEach { x -> drawCircle(MotionDot, 0.55f * u, Offset(x * u, 28.6f * u)) }

    drawFace(u, ox = 40f, oy = 7.6f)

    // Sparkles off the top-right corner.
    val spark = 1.3f * u
    drawLine(BlockViolet, Offset(57.6f * u, 3.6f * u), Offset(59.2f * u, 0.4f * u), spark, StrokeCap.Round)
    drawLine(BlockViolet, Offset(62.6f * u, 8.0f * u), Offset(66.1f * u, 5.8f * u), spark, StrokeCap.Round)
    drawLine(BlockViolet, Offset(63.9f * u, 13.1f * u), Offset(68.3f * u, 13.1f * u), spark, StrokeCap.Round)
}

@ThemePreviews
@Composable
private fun WebBlocksLogoPreview() {
    PreviewSurface { WebBlocksLogo(height = 56.dp) }
}
