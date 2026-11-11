package com.kshrd.features.website.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObservePortalChromeUseCase
import com.kshrd.features.website.domain.usecase.GetWebsitesUseCase
import com.kshrd.features.website.domain.usecase.ObserveOwnerUseCase
import com.kshrd.features.website.domain.usecase.OpenWebsiteUseCase
import com.kshrd.features.website.domain.usecase.SignOutUseCase
import com.kshrd.features.website.presentation.model.MyWebsitesEffect
import com.kshrd.features.website.presentation.model.MyWebsitesEvent
import com.kshrd.features.website.presentation.model.MyWebsitesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyWebsitesViewModel
    @Inject
    constructor(
        private val getWebsites: GetWebsitesUseCase,
        private val openWebsite: OpenWebsiteUseCase,
        private val observeOwner: ObserveOwnerUseCase,
        private val observeChrome: ObservePortalChromeUseCase,
        private val signOut: SignOutUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(MyWebsitesUiState())
        val state: StateFlow<MyWebsitesUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<MyWebsitesEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch {
                observeOwner().collect { owner -> _state.update { it.copy(ownerName = owner?.name.orEmpty(), ownerEmail = owner?.email.orEmpty()) } }
            }
            viewModelScope.launch {
                observeChrome().collect { chrome -> _state.update { it.copy(unreadCount = chrome.unreadCount) } }
            }
            loadWebsites()
        }

        fun onEvent(event: MyWebsitesEvent) {
            when (event) {
                MyWebsitesEvent.RetryClicked -> loadWebsites()
                MyWebsitesEvent.RefreshRequested -> {
                    _state.update { it.copy(isRefreshing = true) }
                    loadWebsites()
                }
                MyWebsitesEvent.ReplayIntroClicked -> _effects.tryEmit(MyWebsitesEffect.ReplayIntro)
                is MyWebsitesEvent.WebsiteClicked -> {
                    val website = (_state.value.websites as? DataState.Success)?.data?.firstOrNull { it.id == event.websiteId }
                    if (website?.hasPortal == true) {
                        openWebsite(event.websiteId)
                        _effects.tryEmit(MyWebsitesEffect.OpenPortal)
                    }
                }
                MyWebsitesEvent.BellClicked -> _effects.tryEmit(MyWebsitesEffect.OpenNotifications)
                MyWebsitesEvent.LogoutClicked -> _state.update { it.copy(showLogoutDialog = true) }
                MyWebsitesEvent.LogoutDismissed -> _state.update { it.copy(showLogoutDialog = false) }
                MyWebsitesEvent.LogoutConfirmed -> {
                    _state.update { it.copy(showLogoutDialog = false) }
                    signOut()
                    _effects.tryEmit(MyWebsitesEffect.SignedOut)
                }
            }
        }

        private fun loadWebsites() {
            viewModelScope.launch {
                getWebsites().collect { result ->
                    _state.update {
                        // While pulling to refresh, keep the current list on screen instead of flashing the loader.
                        if (it.isRefreshing && result is DataState.Loading) it else it.copy(websites = result, isRefreshing = false)
                    }
                }
            }
        }
    }
