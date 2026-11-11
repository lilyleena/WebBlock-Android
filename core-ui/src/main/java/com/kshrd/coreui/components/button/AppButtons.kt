package com.kshrd.coreui.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

@Composable
private fun pressScale(source: MutableInteractionSource): Float {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(dampingRatio = 0.7f, stiffness = 500f), label = "press")
    return scale
}

/**
 * Filled brand pill (iOS `wbPrimary` + `LoadingButton`). While [loading] the label becomes a spinner;
 * taps are ignored but the button stays solid blue. Disabled = 45% opacity.
 * [fullWidth] = false hugs the label instead (empty states, inline actions); [icon] goes before the label.
 * [compact] is the small toolbar pill (36dp tall), for a Save in a top bar.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = true,
    compact: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val blue = MaterialTheme.wb.buttonBlue
    val fill = Brush.verticalGradient(listOf(lerp(blue, Color.White, 0.14f), blue))
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = if (compact) 36.dp else Sizes.control)
                .scale(pressScale(source))
                .alpha(if (enabled) 1f else 0.45f)
                .clip(PillShape)
                .background(fill)
                .clickable(interactionSource = source, indication = null, enabled = enabled && !loading, role = Role.Button, onClick = onClick)
                .padding(horizontal = if (fullWidth) Space.s else if (compact) Space.m else Space.l),
    ) {
        Crossfade(targetState = loading, label = "button-content") { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(if (compact) 18.dp else 22.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    if (icon != null) Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(Sizes.icon))
                    Text(
                        text,
                        style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * White gradient-glass pill for secondary actions, with a soft shadow. [destructive] keeps the same white
 * glass but turns the label, the [icon] and a faint outline red, so Delete reads as dangerous without a
 * red fill competing with the primary button beside it.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    val source = remember { MutableInteractionSource() }
    val wb = MaterialTheme.wb
    val content = if (destructive) wb.red else wb.text
    val border = BorderStroke(1.dp, if (destructive) wb.red.copy(alpha = 0.3f) else wb.brand.copy(alpha = 0.12f))
    val glass = Brush.verticalGradient(listOf(wb.card, lerp(wb.card, wb.brandSoft, 0.35f)))
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.control)
                .scale(pressScale(source))
                .alpha(if (enabled) 1f else 0.5f)
                .shadow(4.dp, PillShape, ambientColor = Color.Black.copy(alpha = 0.06f), spotColor = Color.Black.copy(alpha = 0.08f))
                .clip(PillShape)
                .background(glass)
                .border(border, PillShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = Space.s),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(Sizes.icon))
            Text(text, style = MaterialTheme.typography.titleMedium, color = content, textAlign = TextAlign.Center)
        }
    }
}

/** White "Continue with Google" pill (same height as every button, 1.5dp border, soft brand shadow) with the four-colour G. */
@Composable
fun GoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val wb = MaterialTheme.wb
    val glow = wb.brand.copy(alpha = if (pressed) 0.04f else 0.1f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.control)
                .scale(pressScale(source))
                .alpha(if (enabled) 1f else 0.5f)
                .shadow(if (pressed) 2.dp else 8.dp, PillShape, ambientColor = glow, spotColor = glow)
                .clip(PillShape)
                .background(glassFill())
                .border(1.5.dp, wb.border, PillShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        GoogleGlyph(Modifier.size(22.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = wb.text)
    }
}

/** The four-colour Google "G", drawn as arcs so no image asset is needed (port of iOS `GoogleGlyph`). */
@Composable
fun GoogleGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clearAndSetSemantics { }) {
        val d = size.minDimension
        val w = d * 0.2f
        val arcSize = Size(d - w, d - w)
        val topLeft = Offset(w / 2, w / 2)

        fun arc(
            from: Float,
            to: Float,
            color: Color,
        ) = drawArc(color, from, to - from, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(w, cap = StrokeCap.Butt))
        arc(0f, 50f, Color(0xFF4285F4))
        arc(50f, 135f, Color(0xFF34A853))
        arc(135f, 205f, Color(0xFFFBBC05))
        arc(205f, 315f, Color(0xFFEA4335))
        drawRect(Color(0xFF4285F4), topLeft = Offset(d * 0.5f, d / 2 - w / 2), size = Size(d * 0.5f, w))
    }
}

/** Brand-coloured text action ("Forgot password?", "Edit", "Back"). */
@Composable
fun AppTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = if (destructive) MaterialTheme.wb.red else MaterialTheme.wb.brand,
            ),
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}

@ThemePreviews
@Composable
private fun AppButtonsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs), modifier = Modifier.padding(Space.xxs)) {
            PrimaryButton(text = "Sign in", onClick = {})
            PrimaryButton(text = "Refresh", onClick = {}, fullWidth = false)
            PrimaryButton(text = "Sign in", onClick = {}, loading = true)
            PrimaryButton(text = "Sign in", onClick = {}, enabled = false)
            SecondaryButton(text = "Cancel", onClick = {})
            SecondaryButton(text = "Delete product", onClick = {}, destructive = true)
            GoogleButton(text = "Continue with Google", onClick = {})
            AppTextAction(text = "Forgot password?", onClick = {})
        }
    }
}
