package com.kshrd.features.website.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.components.brand.WebBlocksSticker
import com.kshrd.coreui.preview.FontScalePreviews
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.overline
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

// The WebBlocks sticker: its canvas size, and how far that canvas pokes up above the card's top
// edge (the block itself ends up about a third above the card, on its top-right corner).
private val StickerSize = 88.dp
private val StickerRise = 42.dp
private val ShadowInk = Color(0xFF1B3A7A)
private val IconTileSize = 46.dp

/**
 * Welcome card at the top of the websites list: a frosted-glass card with a "✦ YOUR WORKSPACE" pill,
 * a bold greeting and a short hint on the left, a hairline divider, then a gradient globe tile with
 * the total number of websites beside it. The animated WebBlocks sticker sits on the top-right
 * corner, soft colour waves fill the bottom-right and a small dot grid finishes the bottom-left.
 */
@Composable
fun WelcomeCard(
    firstName: String,
    websiteCount: Int,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.card + 4.dp)
    val fill =
        Brush.linearGradient(
            listOf(
                wb.card,
                wb.purpleSoft.copy(alpha = if (wb.isDark) 0.35f else 0.5f).compositeOver(wb.card),
            ),
        )
    val rim = Brush.linearGradient(listOf(Color.White.copy(alpha = if (wb.isDark) 0.22f else 1f), wb.brand.copy(alpha = 0.28f)))
    val waveBlue = wb.brand.copy(alpha = if (wb.isDark) 0.16f else 0.1f)
    val wavePurple = wb.purple.copy(alpha = if (wb.isDark) 0.22f else 0.16f)
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.m),
            modifier =
                Modifier
                    .padding(top = StickerRise)
                    .fillMaxWidth()
                    .shadow(8.dp, shape, ambientColor = ShadowInk.copy(alpha = 0.12f), spotColor = ShadowInk.copy(alpha = 0.16f))
                    .clip(shape)
                    .background(fill)
                    .drawBehind { drawWaves(waveBlue, wavePurple) }
                    .border(BorderStroke(1.dp, rim), shape)
                    .padding(start = Space.screen, end = Space.screen, top = Space.l, bottom = Space.l),
        ) {
            Eyebrow()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            if (firstName.isBlank()) {
                                stringResource(R.string.home_welcome_title_no_name)
                            } else {
                                stringResource(R.string.home_welcome_title, firstName)
                            },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = wb.text,
                    )
                    Text(
                        text = stringResource(R.string.home_choose_website),
                        style = MaterialTheme.typography.bodyMedium,
                        color = wb.muted,
                    )
                }
                // Hairline divider between the greeting and the stat.
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 10.dp, vertical = Space.xs)
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(wb.brand.copy(alpha = 0.18f)),
                )
                WebsiteTotal(
                    value = websiteCount,
                    label = pluralStringResource(R.plurals.home_total_websites_count, websiteCount),
                )
            }
            DotGrid()
        }
        // The sticker: drawn after the card so it sits on top of the card's corner. Tap it to make it hop.
        WebBlocksSticker(
            size = StickerSize,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = Space.xs)
                    .rotate(10f),
        )
    }
}

/** Soft overlapping colour waves in the bottom-right corner, plus a faint glow under the sticker. */
private fun DrawScope.drawWaves(
    blue: Color,
    purple: Color,
) {
    fun blob(
        color: Color,
        center: Offset,
        radius: Float,
    ) = drawCircle(
        Brush.radialGradient(0f to color, 0.88f to color.copy(alpha = color.alpha * 0.85f), 1f to Color.Transparent, center = center, radius = radius),
        radius = radius,
        center = center,
    )
        blob(blue, Offset(size.width * 1.02f, size.height * 1.38f), size.width * 0.6f)
    blob(purple, Offset(size.width * 1.06f, size.height * 1.12f), size.width * 0.3f)
}

/** "✦ YOUR WORKSPACE" pill above the greeting. */
@Composable
private fun Eyebrow() {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        modifier =
            Modifier
                .background(wb.brandSoft.copy(alpha = if (wb.isDark) 0.9f else 1f), PillShape)
                .padding(horizontal = Space.s, vertical = Space.xxs),
    ) {
        Sparkle(color = wb.buttonBlue, modifier = Modifier.size(15.dp))
        Text(
            text = stringResource(R.string.home_welcome_eyebrow).uppercase(),
            style = MaterialTheme.typography.overline,
            color = wb.buttonBlue,
        )
    }
}

/** A single four-point sparkle (✦) with gently curved sides. */
@Composable
private fun Sparkle(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r = size.minDimension / 2
        val pinch = r * 0.14f
        val star =
            Path().apply {
                moveTo(cx, cy - r)
                quadraticTo(cx + pinch, cy - pinch, cx + r, cy)
                quadraticTo(cx + pinch, cy + pinch, cx, cy + r)
                quadraticTo(cx - pinch, cy + pinch, cx - r, cy)
                quadraticTo(cx - pinch, cy - pinch, cx, cy - r)
                close()
            }
        drawPath(star, color)
    }
}

/** Gradient globe tile on the left; the big total with its label underneath on the right. */
@Composable
private fun WebsiteTotal(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val tileShape = RoundedCornerShape(Radius.control)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(IconTileSize)
                    .shadow(5.dp, tileShape, ambientColor = wb.purple.copy(alpha = 0.4f), spotColor = wb.brand.copy(alpha = 0.5f))
                    .clip(tileShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7FB0FF), wb.brand, Color(0xFF8B5CF6))))
                    .drawBehind {
                        // Glossy sheen across the top half of the tile.
                        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent), endY = size.height * 0.6f))
                    }.border(BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), tileShape),
        ) {
            Icon(Icons.Outlined.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.widthIn(max = 64.dp)) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.Black),
                color = wb.text,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium, lineHeight = 15.sp),
                color = wb.muted,
                maxLines = 2,
            )
        }
    }
}

/** Two rows of four soft dots, a quiet decoration under the text. */
@Composable
private fun DotGrid(modifier: Modifier = Modifier) {
    val dot = MaterialTheme.wb.brand.copy(alpha = 0.3f)
    Column(verticalArrangement = Arrangement.spacedBy(Space.xs), modifier = modifier) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { Box(Modifier.size(6.dp).background(dot, CircleShape)) }
            }
        }
    }
}

@ThemePreviews
@FontScalePreviews
@Composable
private fun WelcomeCardPreview() {
    PreviewSurface { WelcomeCard(firstName = "Sovannarith", websiteCount = 4) }
}
