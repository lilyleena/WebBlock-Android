package com.kshrd.features.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.features.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import com.kshrd.features.notifications.domain.usecase.MarkNotificationReadUseCase
import com.kshrd.features.notifications.domain.usecase.ObserveNotificationsUseCase
import com.kshrd.features.notifications.presentation.model.NotificationsEffect
import com.kshrd.features.notifications.presentation.model.NotificationsEvent
import com.kshrd.features.notifications.presentation.model.NotificationsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel
    @Inject
    constructor(
        observeNotifications: ObserveNotificationsUseCase,
        private val markAllRead: MarkAllNotificationsReadUseCase,
        private val markRead: MarkNotificationReadUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(NotificationsUiState())
        val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<NotificationsEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch {
                observeNotifications().collect { result -> _state.update { it.copy(notifications = result) } }
            }
        }

        fun onEvent(event: NotificationsEvent) {
            when (event) {
                is NotificationsEvent.TabSelected -> _state.update { it.copy(tab = event.tab) }
                is NotificationsEvent.ItemClicked -> onItemClicked(event.id)
                NotificationsEvent.MarkAllReadClicked -> viewModelScope.launch { markAllRead() }
                NotificationsEvent.BackClicked -> _effects.tryEmit(NotificationsEffect.Back)
            }
        }

        /** Every notification opens its own detail page; nothing jumps to another screen. */
        private fun onItemClicked(id: String) {
            viewModelScope.launch { markRead(id) }
            _effects.tryEmit(NotificationsEffect.OpenDetail(id))
        }
    }
