package com.kshrd.features.auth.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.features.auth.domain.validation.FieldError

@Immutable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    /** Failure from the service ("Email or password is incorrect..."), shown above the button and in the chooser. */
    val submitError: String? = null,
    val isLoading: Boolean = false,
    val showGoogleChooser: Boolean = false,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent

    data class PasswordChanged(val value: String) : LoginEvent

    /** The forgot-password flow finished with a new password: fill the email back in, clear the password. */
    data class EmailPrefilled(val value: String) : LoginEvent

    data object SignInClicked : LoginEvent

    data object ForgotPasswordClicked : LoginEvent

    data object GoogleClicked : LoginEvent

    data object GoogleDismissed : LoginEvent

    data object GoogleAccountClicked : LoginEvent
}

sealed interface LoginEffect {
    /** [showIntro] is true on this account's first login, so the intro is shown once before My Websites. */
    data class SignedIn(
        val showIntro: Boolean,
    ) : LoginEffect

    data class OpenForgotPassword(val email: String) : LoginEffect
}
