package com.kshrd.features.auth.presentation.model

import androidx.compose.runtime.Immutable
import com.kshrd.features.auth.domain.validation.AuthValidator
import com.kshrd.features.auth.domain.validation.PasswordChecks

/** Email -> code -> new password. Matches the "Step n of 3" bar in the design. */
enum class ResetStep(val number: Int) { Email(1), Code(2), NewPassword(3) }

/** Form-level messages the UI maps to strings (service failures arrive as plain text in `serviceError`). */
enum class ResetError { EmailEmpty, EmailInvalid, CodeExpired, CodeIncomplete, PasswordRules, ConfirmEmpty, Mismatch }

const val CODE_VALID_SECONDS = 300

@Immutable
data class ForgotPasswordUiState(
    val step: ResetStep = ResetStep.Email,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirm: String = "",
    val isLoading: Boolean = false,
    val formError: ResetError? = null,
    val serviceError: String? = null,
    val secondsLeft: Int = CODE_VALID_SECONDS,
    /** Picks the slide direction of the step transition. */
    val goingBack: Boolean = false,
) {
    val isEmailValid: Boolean get() = AuthValidator.isEmailValid(email)
    val checks: PasswordChecks get() = AuthValidator.passwordChecks(newPassword)
    val matches: Boolean get() = confirm.isNotEmpty() && confirm == newPassword
    val timerText: String get() = "%02d:%02d".format(secondsLeft / SECONDS_PER_MINUTE, secondsLeft % SECONDS_PER_MINUTE)

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent

    data class CodeChanged(val value: String) : ForgotPasswordEvent

    data class NewPasswordChanged(val value: String) : ForgotPasswordEvent

    data class ConfirmChanged(val value: String) : ForgotPasswordEvent

    /** "Send code", "Verify code" or "Update password", depending on the step. */
    data object PrimaryClicked : ForgotPasswordEvent

    data object ResendClicked : ForgotPasswordEvent

    data object ChangeEmailClicked : ForgotPasswordEvent

    data object BackClicked : ForgotPasswordEvent
}

sealed interface ForgotPasswordEffect {
    /** The flow closed. [changedEmail] is non-null when the password was updated. */
    data class Finished(val changedEmail: String?) : ForgotPasswordEffect
}
