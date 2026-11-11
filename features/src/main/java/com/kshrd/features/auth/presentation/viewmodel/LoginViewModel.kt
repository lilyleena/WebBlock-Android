package com.kshrd.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.SessionOwner
import com.kshrd.features.auth.domain.usecase.HasSeenOnboardingUseCase
import com.kshrd.features.auth.domain.usecase.SignInUseCase
import com.kshrd.features.auth.domain.usecase.SignInWithGoogleUseCase
import com.kshrd.features.auth.domain.validation.AuthValidator
import com.kshrd.features.auth.domain.validation.FieldError
import com.kshrd.features.auth.presentation.model.LoginEffect
import com.kshrd.features.auth.presentation.model.LoginEvent
import com.kshrd.features.auth.presentation.model.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val signIn: SignInUseCase,
        private val signInWithGoogle: SignInWithGoogleUseCase,
        private val hasSeenOnboarding: HasSeenOnboardingUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LoginUiState())
        val state: StateFlow<LoginUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<LoginEffect>()
        val effects = _effects.events

        /** Set once the owner taps "Sign in", so missing fields are explained only after they try. */
        private var attempted = false

        fun onEvent(event: LoginEvent) {
            when (event) {
                is LoginEvent.EmailChanged -> onEmailChanged(event.value)
                is LoginEvent.PasswordChanged ->
                    _state.update {
                        it.copy(password = event.value, passwordError = passwordErrorFor(event.value), submitError = null)
                    }
                is LoginEvent.EmailPrefilled -> {
                    attempted = false
                    _state.update { it.copy(email = event.value, password = "", emailError = null, passwordError = null, submitError = null) }
                }
                LoginEvent.SignInClicked -> submit()
                LoginEvent.ForgotPasswordClicked -> _effects.tryEmit(LoginEffect.OpenForgotPassword(_state.value.email))
                LoginEvent.GoogleClicked -> _state.update { it.copy(showGoogleChooser = true, submitError = null) }
                LoginEvent.GoogleDismissed -> _state.update { it.copy(showGoogleChooser = false, submitError = null) }
                LoginEvent.GoogleAccountClicked -> run(signInWithGoogle()) { _state.update { it.copy(showGoogleChooser = false) } }
            }
        }

        private fun onEmailChanged(value: String) {
            _state.update { it.copy(email = value, emailError = emailErrorFor(value), submitError = null) }
        }

        /** Empty: explained only after a sign-in attempt. Non-empty and malformed: explained right away. */
        private fun emailErrorFor(value: String): FieldError? =
            when {
                value.isEmpty() -> if (attempted) FieldError.Empty else null
                AuthValidator.isEmailValid(value) -> null
                else -> FieldError.InvalidEmail
            }

        private fun passwordErrorFor(value: String): FieldError? = if (attempted && value.isEmpty()) FieldError.Empty else null

        private fun submit() {
            val current = _state.value
            if (current.isLoading) return
            attempted = true
            val emailError = emailErrorFor(current.email)
            val passwordError = passwordErrorFor(current.password)
            if (emailError != null || passwordError != null || !AuthValidator.isEmailValid(current.email)) {
                _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
                return
            }
            run(signIn(current.email, current.password)) {}
        }

        private fun run(
            flow: Flow<DataState<SessionOwner>>,
            beforeEffect: () -> Unit,
        ) {
            if (_state.value.isLoading) return
            viewModelScope.launch {
                flow.collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isLoading = true, submitError = null) }
                        is DataState.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            beforeEffect()
                            _effects.emit(LoginEffect.SignedIn(showIntro = !hasSeenOnboarding(result.data.email)))
                        }
                        is DataState.Error -> _state.update { it.copy(isLoading = false, submitError = result.message) }
                        is DataState.MessageSuccess -> _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
