package com.kshrd.features.auth.presentation.model

sealed interface OnboardingEvent {
    /** "Skip" or "Get started": remember the intro was seen and leave it. */
    data object Finish : OnboardingEvent
}

sealed interface OnboardingEffect {
    data object Finished : OnboardingEffect
}
