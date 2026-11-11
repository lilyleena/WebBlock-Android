package com.kshrd.features.business.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.IconTile
import com.kshrd.coreui.components.menu.WbGlassDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuOption
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.DayHours
import java.time.DayOfWeek
import java.time.LocalDate

private const val MINUTES_PER_HOUR = 60
private const val DAYS_IN_WEEK = 7

@Composable
private fun hoursSummary(day: DayHours): String =
    if (day.isOpen) {
        stringResource(R.string.bm_hours_range, formatMinutes(day.openMinutes), formatMinutes(day.closeMinutes))
    } else {
        stringResource(R.string.bm_closed)
    }

/** Preview line for the collapsed opening-hours card: today's hours, or a count of open days. */
@Composable
fun todaysHoursPreview(hours: List<DayHours>): String {
    if (hours.size != DAYS_IN_WEEK) return stringResource(R.string.bm_days_open, hours.count { it.isOpen })
    val today = hours.firstOrNull { it.day == LocalDate.now().dayOfWeek } ?: return ""
    return if (today.isOpen) {
        stringResource(R.string.bm_hours_today, formatMinutes(today.openMinutes), formatMinutes(today.closeMinutes))
    } else {
        stringResource(R.string.bm_closed_today)
    }
}

/** iOS `OpeningHoursList`: weekday on the left, hours on the right, hairline dividers. */
@Composable
fun OpeningHoursList(
    days: List<DayHours>,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Column(modifier = modifier.fillMaxWidth()) {
        days.forEachIndexed { index, day ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Space.m),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(dayName(day.day), style = MaterialTheme.typography.bodyMedium, color = wb.text)
                Text(
                    hoursSummary(day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (day.isOpen) wb.text else wb.muted,
                )
            }
            if (index < days.lastIndex) HorizontalDivider(color = wb.border)
        }
    }
}

/** iOS `OpeningHoursDropdown`: icon, title and today's hours; tap to drop the full week down in place. */
@Composable
fun OpeningHoursDropdown(
    hours: List<DayHours>,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    var expanded by rememberSaveable { mutableStateOf(false) }
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.78f), label = "hours-chevron")
    // Same shape and outline as the website panel at the top of the page: both expand in place.
    val shape = CardShape
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(wb.brandSoft.copy(alpha = if (expanded) 0.55f else 0.3f), shape)
                .border(BorderStroke(1.2.dp, wb.brand.copy(alpha = if (expanded) 0.5f else 0.18f)), shape)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(Space.m),
        ) {
            IconTile(Icons.Outlined.Schedule)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(stringResource(R.string.bm_opening_hours), style = MaterialTheme.typography.titleMedium, color = wb.text)
                Text(todaysHoursPreview(hours), style = MaterialTheme.typography.bodyMedium, color = wb.muted)
            }
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.icon).rotate(chevron))
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = Space.m).padding(bottom = Space.xs)) {
                HorizontalDivider(color = wb.border)
                OpeningHoursList(hours)
            }
        }
    }
}

/**
 * iOS `OpeningHoursEditor`: one compact row per day (name, summary, on/off switch). Tap a row to expand it
 * and edit that day's times; only one day is open at a time.
 */
@Composable
fun OpeningHoursEditor(
    days: List<DayHours>,
    onToggle: (DayOfWeek, Boolean) -> Unit,
    onOpenChanged: (DayOfWeek, Int) -> Unit,
    onCloseChanged: (DayOfWeek, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf<DayOfWeek?>(null) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        days.forEach { day ->
            val isExpanded = expanded == day.day && day.isOpen
            DayEditorRow(
                day = day,
                isExpanded = isExpanded,
                onRowClick = { if (day.isOpen) expanded = if (isExpanded) null else day.day },
                onToggle = { open ->
                    onToggle(day.day, open)
                    if (open) expanded = day.day else if (expanded == day.day) expanded = null
                },
                onOpenChanged = { onOpenChanged(day.day, it) },
                onCloseChanged = { onCloseChanged(day.day, it) },
            )
        }
    }
}

@Composable
private fun DayEditorRow(
    day: DayHours,
    isExpanded: Boolean,
    onRowClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onOpenChanged: (Int) -> Unit,
    onCloseChanged: (Int) -> Unit,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.panel)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (isExpanded) wb.brandSoft.copy(alpha = 0.55f) else wb.card.copy(alpha = 0.6f), shape)
                .border(BorderStroke(1.2.dp, wb.brand.copy(alpha = if (isExpanded) 0.5f else 0.18f)), shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onRowClick)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier = Modifier.padding(horizontal = Space.m, vertical = Space.s),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(dayName(day.day), style = MaterialTheme.typography.labelLarge, color = wb.text)
                Text(
                    hoursSummary(day),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (day.isOpen) wb.brand else wb.muted,
                )
            }
            Switch(
                checked = day.isOpen,
                onCheckedChange = onToggle,
                colors = dayToggleColors(),
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(horizontal = Space.m).padding(bottom = Space.s), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                HorizontalDivider(color = wb.border)
                Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    TimeField(stringResource(R.string.bm_opens), day.openMinutes, onOpenChanged, Modifier.weight(1f))
                    TimeField(stringResource(R.string.bm_closes), day.closeMinutes, onCloseChanged, Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Open/closed day toggle: brand track with a white thumb when open; the same brand colour faded out
 * (still with a white thumb) when closed, so "closed" reads like a disabled switch, not a missing one.
 */
@Composable
internal fun dayToggleColors(): SwitchColors {
    val wb = MaterialTheme.wb
    return SwitchDefaults.colors(
        checkedTrackColor = wb.brand,
        checkedThumbColor = Color.White,
        checkedBorderColor = wb.brand,
        uncheckedTrackColor = wb.brand.copy(alpha = 0.3f),
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = Color.Transparent,
    )
}

@Composable
private fun TimeField(
    title: String,
    minutes: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.tile)
    var expanded by remember { mutableStateOf(false) }
    // Opens and Closes are identical boxes that each fill their half of the row.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
        // Indented by the box's own horizontal padding so the label lines up with the clock icon below.
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = wb.muted,
            modifier = Modifier.padding(start = Space.s),
        )
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Sizes.compact)
                        .clip(shape)
                        .background(wb.card, shape)
                        .border(BorderStroke(1.dp, wb.brand.copy(alpha = 0.22f)), shape)
                        .clickable(role = Role.Button) { expanded = true }
                        .padding(horizontal = Space.s),
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.iconSmall))
                Text(
                    text = formatMinutes(minutes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = wb.text,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = wb.muted,
                    modifier = Modifier.size(Sizes.icon).rotate(if (expanded) 180f else 0f),
                )
            }
            WbGlassDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 280.dp),
            ) {
                TIME_OPTIONS.forEach { option ->
                    WbMenuOption(
                        label = formatMinutes(option),
                        selected = option == minutes,
                        onClick = {
                            expanded = false
                            onChange(option)
                        },
                    )
                }
            }
        }
    }
}

/** Every half hour of the day, 12:00 AM through 11:30 PM, as minutes since midnight. */
private const val TIME_STEP_MINUTES = 30
private val TIME_OPTIONS = (0 until 24 * MINUTES_PER_HOUR step TIME_STEP_MINUTES).toList()

private val previewHours =
    DayOfWeek.entries.map { DayHours(it, it.value <= 5, 9 * MINUTES_PER_HOUR, 18 * MINUTES_PER_HOUR) }

@ThemePreviews
@Composable
private fun OpeningHoursDropdownPreview() {
    PreviewSurface { OpeningHoursDropdown(previewHours) }
}

@ThemePreviews
@Composable
private fun OpeningHoursEditorPreview() {
    PreviewSurface { OpeningHoursEditor(previewHours, { _, _ -> }, { _, _ -> }, { _, _ -> }) }
}
