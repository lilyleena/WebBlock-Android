package com.kshrd.coreui.components.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * Web dashboard filter tab ("All 4 · Live 1 · Draft 2"): it sits in a [ChipRow] track. Selected = white pill
 * with a faint lift, brand text and a filled brand count bubble; unselected = bare muted text with a soft
 * count bubble. 40dp tall inside the track's 4dp inset, so each tab's touch target is 48dp.
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
    val textColor by animateColorAsState(if (selected) wb.brand else wb.muted, tween(200), label = "chip-text")
    val fill by animateColorAsState(if (selected) wb.card else wb.card.copy(alpha = 0f), tween(200), label = "chip-fill")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        modifier =
            modifier
                .heightIn(min = 40.dp)
                .shadow(if (selected) 2.dp else 0.dp, PillShape, ambientColor = wb.brand.copy(alpha = 0.12f), spotColor = wb.brand.copy(alpha = 0.18f))
                .clip(PillShape)
                .background(fill)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                )
                .padding(horizontal = Space.m),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = textColor, maxLines = 1)
        if (count != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                        .background(if (selected) wb.brand else wb.card, CircleShape)
                        .padding(horizontal = 6.dp),
            ) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else wb.muted,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * The web dashboard's filter track: a soft blue pill holding [FilterChip]s side by side. Scrolls
 * horizontally when the tabs are wider than the screen.
 */
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(MaterialTheme.wb.soft)
                .horizontalScroll(rememberScrollState())
                .padding(Space.xxs),
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
    // Same track and thumb as the web dashboard's segmented control (and [ChipRow]): soft blue track, white thumb.
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(wb.soft)
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
                .shadow(2.dp, PillShape, ambientColor = wb.brand.copy(alpha = 0.12f), spotColor = wb.brand.copy(alpha = 0.18f))
                .clip(PillShape)
                .background(wb.card),
        )
        Row {
            labels.forEachIndexed { index, label ->
                val textColor by animateColorAsState(
                    targetValue = if (index == selectedIndex) wb.brand else wb.muted,
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
