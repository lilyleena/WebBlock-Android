package com.kshrd.features.notifications.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kshrd.core.navigation.NotificationDetail
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.features.notifications.domain.usecase.MarkNotificationReadUseCase
import com.kshrd.features.notifications.domain.usecase.ObserveNotificationsUseCase
import com.kshrd.features.notifications.presentation.model.NotificationDetailEffect
import com.kshrd.features.notifications.presentation.model.NotificationDetailEvent
import com.kshrd.features.notifications.presentation.model.NotificationDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeNotifications: ObserveNotificationsUseCase,
        private val markRead: MarkNotificationReadUseCase,
    ) : ViewModel() {
        private val id = savedStateHandle.toRoute<NotificationDetail>().notificationId
        private val _state = MutableStateFlow(NotificationDetailUiState(notificationId = id))
        val state: StateFlow<NotificationDetailUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<NotificationDetailEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch {
                observeNotifications().collect { result -> _state.update { it.copy(notifications = result) } }
            }
            // Opening the detail counts as reading it.
            viewModelScope.launch { markRead(id) }
        }

        fun onEvent(event: NotificationDetailEvent) {
            when (event) {
                NotificationDetailEvent.BackClicked -> _effects.tryEmit(NotificationDetailEffect.Back)
            }
        }
    }
