package com.kshrd.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.features.auth.domain.usecase.MarkOnboardingSeenUseCase
import com.kshrd.features.auth.presentation.model.OnboardingEffect
import com.kshrd.features.auth.presentation.model.OnboardingEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val markSeen: MarkOnboardingSeenUseCase,
    ) : ViewModel() {
        private val _effects = OneShotEvents<OnboardingEffect>()
        val effects = _effects.events

        fun onEvent(event: OnboardingEvent) {
            when (event) {
                OnboardingEvent.Finish -> {
                    markSeen()
                    _effects.tryEmit(OnboardingEffect.Finished)
                }
            }
        }
    }
