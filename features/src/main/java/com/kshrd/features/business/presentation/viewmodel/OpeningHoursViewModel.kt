package com.kshrd.features.business.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObserveWorkspaceUseCase
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.usecase.ObserveBusinessUseCase
import com.kshrd.features.business.domain.usecase.OpeningHoursRules
import com.kshrd.features.business.domain.usecase.UpdateOpeningHoursUseCase
import com.kshrd.features.business.presentation.model.OpeningHoursEffect
import com.kshrd.features.business.presentation.model.OpeningHoursEvent
import com.kshrd.features.business.presentation.model.OpeningHoursUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class OpeningHoursViewModel
    @Inject
    constructor(
        private val observeWorkspace: ObserveWorkspaceUseCase,
        private val observeBusiness: ObserveBusinessUseCase,
        private val updateHours: UpdateOpeningHoursUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(OpeningHoursUiState())
        val state: StateFlow<OpeningHoursUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<OpeningHoursEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch {
                val websiteId = observeWorkspace().first { it.current != null }.current?.id ?: return@launch
                val info = observeBusiness(websiteId).first { it is DataState.Success } as DataState.Success
                _state.update { it.copy(loaded = true, hours = info.data.hours) }
            }
        }

        fun onEvent(event: OpeningHoursEvent) {
            when (event) {
                is OpeningHoursEvent.DayToggled -> editDay(event.day) { it.copy(isOpen = event.open) }
                is OpeningHoursEvent.OpenChanged -> editDay(event.day) { OpeningHoursRules.withOpen(it, event.minutes) }
                is OpeningHoursEvent.CloseChanged -> editDay(event.day) { OpeningHoursRules.withClose(it, event.minutes) }
                OpeningHoursEvent.CopyMondayClicked -> {
                    _state.update { it.copy(hours = OpeningHoursRules.copyMondayToWeekdays(it.hours)) }
                    _effects.tryEmit(OpeningHoursEffect.ShowMessage(R.string.hours_copied))
                }
                OpeningHoursEvent.SaveClicked -> save()
                OpeningHoursEvent.BackClicked -> _effects.tryEmit(OpeningHoursEffect.Back)
            }
        }

        private fun editDay(
            day: DayOfWeek,
            transform: (DayHours) -> DayHours,
        ) {
            _state.update { s -> s.copy(hours = s.hours.map { if (it.day == day) transform(it) else it }) }
        }

        private fun save() {
            val current = _state.value
            if (current.isSaving || !current.loaded) return
            viewModelScope.launch {
                val websiteId = observeWorkspace().first().current?.id ?: return@launch
                updateHours(websiteId, current.hours).collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isSaving = true) }
                        is DataState.Success -> _effects.emit(OpeningHoursEffect.Back)
                        else -> _state.update { it.copy(isSaving = false) }
                    }
                }
            }
        }
    }
