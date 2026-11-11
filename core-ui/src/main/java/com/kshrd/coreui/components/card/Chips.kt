package com.kshrd.coreui.components.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * iOS `FilterChip`: outlined pill; the selected one is filled `0x3F72D0` with white text and scaled 1.04.
 * Unselected = card@92% with a brand@30% outline. Min height `Sizes.compact`, horizontal padding `Space.m`. [count] is dimmed.
 */
@Composable
fun FilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val wb = MaterialTheme.wb
    val textColor by animateColorAsState(if (selected) androidx.compose.ui.graphics.Color.White else wb.text, tween(200), label = "chip-text")
    val fill by animateColorAsState(if (selected) wb.buttonBlue else wb.card.copy(alpha = 0.92f), tween(200), label = "chip-fill")
    val outline by animateColorAsState(wb.brand.copy(alpha = if (selected) 0.9f else 0.3f), tween(200), label = "chip-outline")
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, spring(dampingRatio = 0.65f, stiffness = 400f), label = "chip-scale")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        modifier =
            modifier
                .scale(scale)
                .heightIn(min = Sizes.compact)
                .clip(PillShape)
                .background(fill)
                .border(1.dp, outline, PillShape)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                )
                .padding(horizontal = Space.m),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = textColor)
        if (count != null) Text("$count", style = MaterialTheme.typography.labelLarge, color = textColor, modifier = Modifier.alpha(0.6f))
    }
}

/** Horizontally scrolling row of separate [FilterChip]s that stays inside the cards' left and right edges. */
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = Space.xxs, horizontal = Space.xxxs),
        content = content,
    )
}

/** iOS `FlowLayout`: wraps children onto new lines inside the same width as the form fields. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WbFlowRow(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = Space.xs,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) { content() }
}

/** Filter pill ("All 12", "Flowers"). Kept for existing call sites: same look as [FilterChip]. */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = FilterChip(title = text, selected = selected, onClick = onClick, modifier = modifier)

/**
 * Two-or-more segment switch (Overview | Activity, All | Unread). A white gradient glass pill slides
 * under the selected label on a soft white-blue gradient track, and the label colours cross-fade.
 */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val inset = Space.xxs
    val trackBrush =
        Brush.verticalGradient(
            if (wb.isDark) listOf(Color(0xFF1B2540), Color(0xFF141C31)) else listOf(Color(0xFFEFF3FB), Color(0xFFFAFBFE)),
        )
    val thumbBrush =
        Brush.verticalGradient(
            if (wb.isDark) listOf(Color(0xFF3A4668), Color(0xFF28324D)) else listOf(Color(0xFFFFFFFF), Color(0xFFF3F6FC)),
        )
    val thumbRim =
        Brush.verticalGradient(
            if (wb.isDark) listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f)) else listOf(Color.White, Color(0xFFDCE5F5)),
        )
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(trackBrush)
                .border(1.dp, wb.border.copy(alpha = 0.7f), PillShape)
                .padding(inset),
    ) {
        val segmentWidth = maxWidth / labels.size
        val offset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            label = "segment-offset",
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(segmentWidth)
                .height(40.dp)
                .shadow(3.dp, PillShape, ambientColor = wb.brand.copy(alpha = 0.18f), spotColor = wb.brand.copy(alpha = 0.18f))
                .clip(PillShape)
                .background(thumbBrush)
                .border(1.dp, thumbRim, PillShape),
        )
        Row {
            labels.forEachIndexed { index, label ->
                val textColor by animateColorAsState(
                    targetValue = if (index == selectedIndex) wb.text else wb.muted,
                    animationSpec = tween(durationMillis = 200),
                    label = "segment-text",
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(PillShape)
                            .selectable(
                                selected = index == selectedIndex,
                                onClick = { onSelect(index) },
                                role = Role.Tab,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ),
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ChipsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            ChipRow {
                FilterChip("All", selected = true, onClick = {}, count = 12)
                FilterChip("Flowers", selected = false, onClick = {}, count = 5)
                FilterChip("Vases", selected = false, onClick = {})
            }
            SegmentedTabs(labels = listOf("Overview", "Logs"), selectedIndex = 0, onSelect = {})
        }
    }
}
