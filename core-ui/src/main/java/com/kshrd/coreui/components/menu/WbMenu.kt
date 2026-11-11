package com.kshrd.coreui.components.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import kotlinx.coroutines.launch

/** Rounded popup menu: card colour, 24dp corners, a soft brand-tinted border and shadow. Fill it with [WbMenuItem]s. */
@Composable
fun WbDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wb = MaterialTheme.wb
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(Radius.card),
        containerColor = wb.card,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, wb.brand.copy(alpha = 0.14f)),
        modifier = modifier.widthIn(min = 200.dp),
        content = content,
    )
}

/**
 * Popup for pick-one lists (times, sizes…): 24dp corners on a solid card-colour fill (white in light
 * mode) so the page behind never shows through, with a faint brand border. Fill it with [WbMenuOption]s.
 * When it opens it scrolls the selected option to the middle, so a long list (every half hour of the
 * day) shows the current value instead of always starting at the top.
 */
@Composable
fun WbGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wb = MaterialTheme.wb
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    // Scroll once per opening, the first time the selected option is laid out.
    var centred by remember(expanded) { mutableStateOf(false) }
    val centreSelected: (Int, Int) -> Unit = { top, height ->
        if (!centred) {
            centred = true
            scope.launch { scrollState.scrollTo((top + height / 2 - scrollState.viewportSize / 2).coerceAtLeast(0)) }
        }
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(Radius.card),
        containerColor = wb.card,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, wb.brand.copy(alpha = 0.14f)),
        scrollState = scrollState,
        modifier = modifier.widthIn(min = 150.dp),
    ) {
        CompositionLocalProvider(LocalSelectedOptionPlaced provides centreSelected) { content() }
    }
}

/** Reports where the selected [WbMenuOption] sits (top, height in px) so its menu can scroll to it. */
private val LocalSelectedOptionPlaced = staticCompositionLocalOf<((Int, Int) -> Unit)?> { null }

/** One pill-shaped choice in a [WbGlassDropdownMenu]. The [selected] row gets a brand gradient and a check. */
@Composable
fun WbMenuOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val wb = MaterialTheme.wb
    val onPlaced = LocalSelectedOptionPlaced.current
    Row(
        modifier =
            Modifier
                .then(
                    if (selected && onPlaced != null) {
                        Modifier.onGloballyPositioned { onPlaced(it.positionInParent().y.toInt(), it.size.height) }
                    } else {
                        Modifier
                    },
                ).padding(horizontal = Space.xs, vertical = Space.xxxs)
                .fillMaxWidth()
                .clip(PillShape)
                .then(
                    if (selected) {
                        Modifier.background(Brush.horizontalGradient(listOf(wb.brand, wb.brand.copy(alpha = 0.75f))))
                    } else {
                        Modifier
                    },
                ).clickable(onClick = onClick)
                .padding(horizontal = Space.s, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else wb.text,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(WebIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/** One rounded menu row: a tinted round icon tile and a semibold label. [destructive] turns it red. */
@Composable
fun WbMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val wb = MaterialTheme.wb
    val tint = if (destructive) wb.red else wb.brand
    val container = if (destructive) wb.redSoft else wb.brandSoft
    Row(
        modifier =
            Modifier
                .padding(horizontal = Space.xs)
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.control))
                .clickable(onClick = onClick)
                .padding(horizontal = Space.xs, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (destructive) wb.red else wb.text,
            modifier = Modifier.padding(end = Space.s),
        )
    }
}

@ThemePreviews
@Composable
private fun WbMenuItemPreview() {
    PreviewSurface {
        Box {
            androidx.compose.foundation.layout.Column {
                WbMenuItem(WebIcons.Pencil, "Edit product", onClick = {})
                WbMenuItem(WebIcons.Trash, "Delete product", onClick = {}, destructive = true)
            }
        }
    }
}
