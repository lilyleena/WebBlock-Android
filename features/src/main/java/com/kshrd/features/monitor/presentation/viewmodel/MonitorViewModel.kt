package com.kshrd.features.monitor.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObservePortalChromeUseCase
import com.kshrd.core.session.SwitchWebsiteUseCase
import com.kshrd.features.business.domain.usecase.ObserveOwnerUseCase
import com.kshrd.features.monitor.domain.usecase.ObserveMonitorUseCase
import com.kshrd.features.monitor.presentation.model.MonitorEffect
import com.kshrd.features.monitor.presentation.model.MonitorEvent
import com.kshrd.features.monitor.presentation.model.MonitorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel
    @Inject
    constructor(
        private val observeChrome: ObservePortalChromeUseCase,
        private val observeOwner: ObserveOwnerUseCase,
        private val observeMonitor: ObserveMonitorUseCase,
        private val switchWebsite: SwitchWebsiteUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(MonitorUiState())
        val state: StateFlow<MonitorUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<MonitorEffect>()
        val effects = _effects.events

        private val reload = MutableStateFlow(0)

        init {
            viewModelScope.launch { observeChrome().collect { chrome -> _state.update { it.copy(chrome = chrome) } } }
            viewModelScope.launch { observeOwner().collect { owner -> _state.update { it.copy(ownerName = owner?.name.orEmpty()) } } }
            loadMonitor()
        }

        fun onEvent(event: MonitorEvent) {
            when (event) {
                is MonitorEvent.TabSelected -> _state.update { it.copy(tab = event.tab) }
                MonitorEvent.RetryClicked -> reload.update { it + 1 }
                MonitorEvent.VisitSiteClicked ->
                    _state.value.chrome.workspace.current
                        ?.let { _effects.tryEmit(MonitorEffect.OpenUrl("https://${it.domain}")) }
                MonitorEvent.BellClicked -> _effects.tryEmit(MonitorEffect.OpenNotifications)
                is MonitorEvent.WebsiteSwitched -> switchWebsite(event.websiteId)
                MonitorEvent.BackClicked -> _effects.tryEmit(MonitorEffect.BackToWebsites)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadMonitor() {
            viewModelScope.launch {
                observeChrome()
                    .map { it.workspace.current?.id }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .combine(reload) { websiteId, _ -> websiteId }
                    .onEach { _state.update { s -> s.copy(monitor = DataState.Loading) } }
                    .flatMapLatest { observeMonitor(it) }
                    .collect { result -> _state.update { it.copy(monitor = result) } }
            }
        }
    }
