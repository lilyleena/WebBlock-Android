package com.kshrd.features.auth.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.decor.rememberReduceMotion
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import kotlinx.coroutines.delay

private data class Step(val title: Int, val subtitle: Int)

private val Steps =
    listOf(
        Step(R.string.auth_ob_step_1, R.string.auth_ob_step_1_sub),
        Step(R.string.auth_ob_step_2, R.string.auth_ob_step_2_sub),
        Step(R.string.auth_ob_step_3, R.string.auth_ob_step_3_sub),
    )

private const val LAST_STEP = 2

/** Page 2: "Three steps to a live site": a scrolling strip of template looks above an animated step list. */
@Composable
fun StepsIllustration(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(active, reduceMotion) {
        step = 0
        if (!active || reduceMotion) {
            step = LAST_STEP
            return@LaunchedEffect
        }
        while (true) {
            delay(1300)
            step = if (step == LAST_STEP) 0 else step + 1
        }
    }
    Column(
        modifier = modifier.fillMaxWidth().height(340.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
    ) {
        TemplateMarquee(running = active && !reduceMotion, modifier = Modifier.entrance(active, 50, y = (-16).dp))
        StepList(active = active, step = step)
    }
}

@Composable
private fun StepList(
    active: Boolean,
    step: Int,
) {
    val wb = MaterialTheme.wb
    Column(
        modifier =
            Modifier
                .obFloat(5.dp, 3400)
                .rotate(-2f)
                .width(274.dp)
                .obGlass(24.dp)
                .padding(16.dp),
    ) {
        Steps.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.entrance(active, 200 + index * 120, x = (-20).dp, y = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val reached = step >= index
                    val badgeScale by androidx.compose.animation.core.animateFloatAsState(
                        if (step == index) 1.12f else 1f,
                        spring(dampingRatio = 0.75f, stiffness = 200f),
                        label = "badge-scale",
                    )
                    Box(
                        modifier = Modifier.size(28.dp).scale(badgeScale).background(if (reached) wb.brand else wb.brandSoft, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", fontSize = fixedSp(12f), fontWeight = FontWeight.Bold, color = if (reached) Color.White else wb.brand)
                    }
                    if (index < Steps.lastIndex) {
                        val fill by animateDpAsState(if (step > index) 26.dp else 0.dp, spring(dampingRatio = 0.75f), label = "connector")
                        Box(Modifier.size(width = 2.dp, height = 26.dp).background(wb.brand.copy(alpha = 0.18f), CircleShape)) {
                            Box(Modifier.size(width = 2.dp, height = fill).background(wb.brand, CircleShape))
                        }
                    }
                }
                Column(modifier = Modifier.padding(top = 3.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(stringResource(item.title), style = MaterialTheme.typography.titleSmall, color = wb.text)
                    Text(stringResource(item.subtitle), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = wb.muted)
                    if (index == LAST_STEP && step == LAST_STEP) {
                        Row(
                            modifier = Modifier.padding(top = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(WebIcons.CircleCheck, null, tint = wb.green, modifier = Modifier.size(12.dp))
                            Text(
                                stringResource(R.string.auth_ob_step_3_url),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = wb.green,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Look(val title: Int, val background: Color, val foreground: Color, val photo: Int?)

private val Looks =
    listOf(
        Look(R.string.auth_ob_look_1, Color(0xFFFBDCE6), Color(0xFF8A2F4D), null),
        Look(R.string.auth_ob_look_2, Color(0xFFF4EEE6), Color(0xFF1A273D), R.drawable.table_arrangement),
        Look(R.string.auth_ob_look_3, Color(0xFF2B1D16), Color(0xFFF3E3CF), null),
        Look(R.string.auth_ob_look_4, Color(0xFFECEEF2), Color(0xFF1A273D), R.drawable.stone_vase),
    )

private const val CARD_WIDTH = 112f
private const val CARD_GAP = 10f
private const val MARQUEE_SPEED = 22f

/** Endlessly scrolling row of template "looks", like the website's template carousel. */
@Composable
private fun TemplateMarquee(
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    val loop = (CARD_WIDTH + CARD_GAP) * Looks.size
    val transition = rememberInfiniteTransition(label = "marquee")
    val travelled by transition.animateFloat(
        initialValue = 0f,
        targetValue = loop,
        animationSpec = infiniteRepeatable(tween((loop / MARQUEE_SPEED * 1000).toInt(), easing = LinearEasing)),
        label = "marquee-x",
    )
    Box(
        modifier =
            modifier
                .width(330.dp)
                .height(74.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush =
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.1f to Color.Black,
                                0.9f to Color.Black,
                                1f to Color.Transparent,
                            ),
                        blendMode = BlendMode.DstIn,
                    )
                },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier =
                Modifier
                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                    .offset { IntOffset(if (running) -(travelled * density).toInt() else 0, 0) },
            horizontalArrangement = Arrangement.spacedBy(CARD_GAP.dp),
        ) {
            repeat(Looks.size * 2) { index -> LookCard(Looks[index % Looks.size]) }
        }
    }
}

@Composable
private fun LookCard(look: Look) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier =
            Modifier
                .size(width = CARD_WIDTH.dp, height = 66.dp)
                .shadow(4.dp, shape, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
                .background(look.background, shape)
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(look.title), fontSize = fixedSp(8f), fontWeight = FontWeight.Bold, color = look.foreground, modifier = Modifier.weight(1f))
        if (look.photo != null) {
            Image(
                painter = painterResource(look.photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 38.dp, height = 54.dp).clip(RoundedCornerShape(6.dp)),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun StepsIllustrationPreview() {
    PreviewSurface { StepsIllustration(active = true) }
}
