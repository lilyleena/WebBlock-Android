package com.kshrd.features.auth.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.navigation.ForgotPassword
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.features.auth.domain.usecase.RequestResetCodeUseCase
import com.kshrd.features.auth.domain.usecase.ResetPasswordUseCase
import com.kshrd.features.auth.domain.usecase.VerifyResetCodeUseCase
import com.kshrd.features.auth.domain.validation.AuthValidator
import com.kshrd.features.auth.presentation.model.CODE_VALID_SECONDS
import com.kshrd.features.auth.presentation.model.ForgotPasswordEffect
import com.kshrd.features.auth.presentation.model.ForgotPasswordEvent
import com.kshrd.features.auth.presentation.model.ForgotPasswordUiState
import com.kshrd.features.auth.presentation.model.ResetError
import com.kshrd.features.auth.presentation.model.ResetStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val requestCode: RequestResetCodeUseCase,
        private val verifyCode: VerifyResetCodeUseCase,
        private val resetPassword: ResetPasswordUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ForgotPasswordUiState(email = savedStateHandle.toRoute<ForgotPassword>().email))
        val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<ForgotPasswordEffect>()
        val effects = _effects.events

        private var timer: Job? = null

        fun onEvent(event: ForgotPasswordEvent) {
            when (event) {
                is ForgotPasswordEvent.EmailChanged -> _state.update { it.copy(email = event.value, formError = null, serviceError = null) }
                is ForgotPasswordEvent.CodeChanged ->
                    _state.update { it.copy(code = event.value.filter(Char::isDigit).take(AuthValidator.CODE_LENGTH)) }
                is ForgotPasswordEvent.NewPasswordChanged -> _state.update { it.copy(newPassword = event.value) }
                is ForgotPasswordEvent.ConfirmChanged -> _state.update { it.copy(confirm = event.value) }
                ForgotPasswordEvent.PrimaryClicked -> onPrimary()
                ForgotPasswordEvent.ResendClicked -> resend()
                ForgotPasswordEvent.ChangeEmailClicked -> goTo(ResetStep.Email)
                ForgotPasswordEvent.BackClicked -> onBack()
            }
        }

        private fun onPrimary() {
            if (_state.value.isLoading) return
            when (_state.value.step) {
                ResetStep.Email -> sendCode()
                ResetStep.Code -> verify()
                ResetStep.NewPassword -> update()
            }
        }

        private fun sendCode() {
            val current = _state.value
            if (!current.isEmailValid) {
                _state.update { it.copy(formError = if (current.email.isEmpty()) ResetError.EmailEmpty else ResetError.EmailInvalid) }
                return
            }
            run(requestCode(current.email)) {
                goTo(ResetStep.Code, forward = true)
                startTimer()
            }
        }

        private fun resend() {
            if (_state.value.isLoading) return
            run(requestCode(_state.value.email)) {
                _state.update { it.copy(code = "") }
                startTimer()
            }
        }

        private fun verify() {
            val current = _state.value
            if (current.secondsLeft <= 0) {
                _state.update { it.copy(formError = ResetError.CodeExpired, serviceError = null) }
                return
            }
            if (current.code.length != AuthValidator.CODE_LENGTH) {
                _state.update { it.copy(formError = ResetError.CodeIncomplete, serviceError = null) }
                return
            }
            run(verifyCode(current.email, current.code)) { goTo(ResetStep.NewPassword, forward = true) }
        }

        private fun update() {
            val current = _state.value
            val checks = current.checks
            val error =
                when {
                    !checks.meetsRequirements -> ResetError.PasswordRules
                    current.confirm.isEmpty() -> ResetError.ConfirmEmpty
                    !current.matches -> ResetError.Mismatch
                    else -> null
                }
            if (error != null) {
                _state.update { it.copy(formError = error, serviceError = null) }
                return
            }
            run(resetPassword(current.email, current.newPassword)) {
                _effects.tryEmit(ForgotPasswordEffect.Finished(changedEmail = current.email))
            }
        }

        private fun onBack() {
            when (_state.value.step) {
                ResetStep.Email -> _effects.tryEmit(ForgotPasswordEffect.Finished(changedEmail = null))
                ResetStep.Code -> goTo(ResetStep.Email)
                ResetStep.NewPassword -> goTo(ResetStep.Code)
            }
        }

        private fun goTo(
            step: ResetStep,
            forward: Boolean = step.number > _state.value.step.number,
        ) {
            if (step == ResetStep.Code && forward) _state.update { it.copy(code = "") }
            if (step != ResetStep.Code) timer?.cancel()
            _state.update { it.copy(step = step, goingBack = !forward, formError = null, serviceError = null) }
            if (step == ResetStep.Code && !forward) startTimer(reset = false)
        }

        private fun startTimer(reset: Boolean = true) {
            timer?.cancel()
            if (reset) _state.update { it.copy(secondsLeft = CODE_VALID_SECONDS) }
            timer =
                viewModelScope.launch {
                    while (_state.value.secondsLeft > 0) {
                        delay(TICK_MS)
                        _state.update { if (it.secondsLeft > 0) it.copy(secondsLeft = it.secondsLeft - 1) else it }
                    }
                }
        }

        private fun run(
            flow: Flow<DataState<Unit>>,
            onSuccess: () -> Unit,
        ) {
            viewModelScope.launch {
                flow.collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isLoading = true, formError = null, serviceError = null) }
                        is DataState.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            onSuccess()
                        }
                        is DataState.Error -> _state.update { it.copy(isLoading = false, serviceError = result.message) }
                        is DataState.MessageSuccess -> _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        }

        private companion object {
            const val TICK_MS = 1000L
        }
    }
