package com.kshrd.coreui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextOverflow
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/** Soft diagonal wash from a light brand tint into white. For cards that should feel warmer than flat grey. */
@Composable
fun softGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        listOf(scheme.primaryContainer.copy(alpha = 0.75f).compositeOver(scheme.surface), scheme.surface),
    )
}

/**
 * Rounded card. By default white with a faint border and a very soft brand-tinted shadow.
 * Pass [tint] for the flat "soft fill" style (no border, no shadow, light colour), or [brush] for
 * the same flat style with a gradient.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tint: Color? = null,
    brush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    // A translucent tint is blended onto the page colour so the card is solid.
    val color = tint?.compositeOver(surface) ?: surface
    val border = if (tint == null && brush == null) cardBorder() else null
    val flat = tint != null || brush != null
    // Linear-style: a hairline border and a barely-there shadow, no coloured glow.
    val shaped = if (flat) modifier else modifier.cardShadow()
    val filled = if (brush != null) shaped.clip(CardShape).background(brush) else shaped
    if (onClick != null) {
        Surface(onClick = onClick, modifier = filled, shape = CardShape, color = if (brush != null) Color.Transparent else color, contentColor = MaterialTheme.colorScheme.onSurface, border = border) {
            Column(content = content)
        }
    } else {
        Surface(modifier = filled, shape = CardShape, color = if (brush != null) Color.Transparent else color, contentColor = MaterialTheme.colorScheme.onSurface, border = border) {
            Column(content = content)
        }
    }
}

/** Small uppercase section caption with an optional trailing action ("Edit"). */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.wb.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (actionLabel != null && onAction != null) {
            AppTextAction(text = actionLabel, onClick = onAction)
        }
    }
}

@ThemePreviews
@Composable
private fun AppCardPreview() {
    PreviewSurface {
        Column {
            SectionLabel(text = "Contact & location", actionLabel = "Edit", onAction = {})
            AppCard(modifier = Modifier.padding(top = Space.xxs)) {
                Text("012 345 678", modifier = Modifier.padding(Space.m))
            }
        }
    }
}
