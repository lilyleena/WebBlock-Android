package com.kshrd.coreui.components.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * iOS `IconTile`: 44dp rounded square (radius 14) with the icon in brand blue on a brand@18% glass tint.
 * Pass [tint]/[container] to recolour (e.g. amber for near-limit metrics).
 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.wb.brand,
    container: Color = MaterialTheme.wb.brand.copy(alpha = 0.18f),
    size: Dp = 44.dp,
) {
    val shape = RoundedCornerShape(size * (14f / 44f))
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size).clip(shape).glass(shape, container).clearAndSetSemantics { },
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size / 2))
    }
}

/** iOS `AvatarView`: round brand@18% tile with bold rounded initials (size × 0.36). */
@Composable
fun AvatarView(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size).clip(CircleShape).glass(CircleShape, MaterialTheme.wb.brand.copy(alpha = 0.18f)).clearAndSetSemantics { },
    ) {
        Text(
            text = initials,
            color = MaterialTheme.wb.brand,
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(size.value * INITIALS_SCALE, androidx.compose.ui.unit.TextUnitType.Sp),
        )
    }
}

/**
 * Round logo with a hairline border (iOS `WebsiteLogo`). Without a [logoRes] it falls back to the brand
 * gradient with [fallbackIcon] if given, otherwise white [initials] (size x 0.36, like [AvatarView]).
 */
@Composable
fun LogoAvatar(
    @DrawableRes logoRes: Int?,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    fallbackIcon: ImageVector? = null,
) {
    val wb = MaterialTheme.wb
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .then(if (logoRes == null) Modifier.background(wb.brandGradient) else Modifier.background(Color.White))
                .border(1.dp, wb.border, CircleShape)
                .clearAndSetSemantics { },
    ) {
        when {
            logoRes != null -> Image(painterResource(logoRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size))
            fallbackIcon != null -> Icon(fallbackIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * LOGO_GLYPH_SCALE))
            else ->
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(size.value * INITIALS_SCALE, androidx.compose.ui.unit.TextUnitType.Sp),
                    maxLines = 1,
                )
        }
    }
}

private const val INITIALS_SCALE = 0.36f
private const val LOGO_GLYPH_SCALE = 0.42f

@ThemePreviews
@Composable
private fun IconTilePreview() {
    PreviewSurface {
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.s)) {
            IconTile(WebIcons.Phone)
            AvatarView("SS", size = 44.dp)
            LogoAvatar(logoRes = null, initials = "PS", size = 44.dp)
        }
    }
}
