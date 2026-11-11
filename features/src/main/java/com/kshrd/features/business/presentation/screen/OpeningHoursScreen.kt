package com.kshrd.features.business.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.components.card.FormCard
import com.kshrd.coreui.components.card.FormCardShape
import com.kshrd.coreui.components.menu.WbGlassDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuOption
import com.kshrd.coreui.components.nav.BackTopBar
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.usecase.OpeningHoursRules
import com.kshrd.features.business.presentation.components.dayName
import com.kshrd.features.business.presentation.components.dayToggleColors
import com.kshrd.features.business.presentation.components.formatMinutes
import com.kshrd.features.business.presentation.model.OpeningHoursEffect
import com.kshrd.features.business.presentation.model.OpeningHoursEvent
import com.kshrd.features.business.presentation.model.OpeningHoursUiState
import com.kshrd.features.business.presentation.viewmodel.OpeningHoursViewModel
import java.time.DayOfWeek

private const val MINUTES_PER_DAY = 24 * 60

@Composable
fun OpeningHoursScreen(
    onBack: () -> Unit,
    viewModel: OpeningHoursViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            OpeningHoursEffect.Back -> onBack()
            is OpeningHoursEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.messageRes))
        }
    }
    OpeningHoursContent(state = state, snackbarHostState = snackbar, onEvent = viewModel::onEvent)
}

@Composable
fun OpeningHoursContent(
    state: OpeningHoursUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (OpeningHoursEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer).statusBarsPadding()) {
        BackTopBar(
            backLabel = stringResource(R.string.business_title),
            onBack = { onEvent(OpeningHoursEvent.BackClicked) },
            title = stringResource(R.string.hours_title),
            trailing = {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = Space.m).size(Sizes.icon), strokeWidth = 2.dp)
                } else {
                    AppTextAction(
                        text = stringResource(R.string.action_save),
                        onClick = { onEvent(OpeningHoursEvent.SaveClicked) },
                        enabled = state.loaded,
                    )
                }
            },
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Space.screen, vertical = Space.xs),
            verticalArrangement = Arrangement.spacedBy(Space.m),
        ) {
            Text(
                stringResource(R.string.hours_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FormCard(title = stringResource(R.string.hours_title)) {
                state.hours.forEachIndexed { index, day ->
                    DayRow(day, onEvent)
                    if (index < state.hours.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            Surface(
                onClick = { onEvent(OpeningHoursEvent.CopyMondayClicked) },
                shape = FormCardShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.hours_copy_monday),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(20.dp),
                )
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.padding(horizontal = Space.screen))
    }
}

@Composable
private fun DayRow(
    day: DayHours,
    onEvent: (OpeningHoursEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = day.isOpen,
            onCheckedChange = { onEvent(OpeningHoursEvent.DayToggled(day.day, it)) },
            colors = dayToggleColors(),
        )
        Text(
            dayName(day.day),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 10.dp).weight(1f),
            maxLines = 1,
        )
        if (day.isOpen) {
            TimeDropdown(day.openMinutes) { onEvent(OpeningHoursEvent.OpenChanged(day.day, it)) }
            Text("–", modifier = Modifier.padding(horizontal = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TimeDropdown(day.closeMinutes) { onEvent(OpeningHoursEvent.CloseChanged(day.day, it)) }
        } else {
            Text(
                stringResource(R.string.hours_closed_all_day),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = Space.xxs),
            )
        }
    }
}

@Composable
private fun TimeDropdown(
    minutes: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        onClick = { expanded = true },
        shape = PillShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.width(88.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(formatMinutes(minutes), style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Icon(WebIcons.ChevronDown, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        WbGlassDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp),
        ) {
            (0 until MINUTES_PER_DAY step OpeningHoursRules.STEP).forEach { option ->
                WbMenuOption(
                    label = formatMinutes(option),
                    selected = option == minutes,
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun OpeningHoursContentPreview() {
    PreviewSurface(padding = 0) {
        OpeningHoursContent(
            state =
                OpeningHoursUiState(
                    loaded = true,
                    hours = DayOfWeek.entries.map { DayHours(it, it != DayOfWeek.SUNDAY, 9 * 60, 18 * 60) },
                ),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}
