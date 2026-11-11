package com.kshrd.features.auth.domain.validation

/** Why a field is invalid. The UI maps each to a string resource. */
enum class FieldError { Empty, InvalidEmail, TooShort, CodeIncomplete, Mismatch }

/** The live checks shown under the "new password" field (mirrors the iOS rules list). */
data class PasswordChecks(
    val hasLength: Boolean,
    val hasUpperAndNumber: Boolean,
    val hasSymbol: Boolean,
    /** 0 (empty) to 4. */
    val strength: Int,
) {
    val meetsRequirements: Boolean get() = hasLength && hasUpperAndNumber
}

/** Pure form validation shared by login and forgot-password. No Android imports. */
object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 8
    const val STRONG_PASSWORD_LENGTH = 12
    const val CODE_LENGTH = 6

    /** iOS rule: exactly one "@", something before it, a dot after it, no spaces. */
    fun isEmailValid(value: String): Boolean {
        val parts = value.split('@')
        return parts.size == 2 && parts[0].isNotEmpty() && parts[1].contains('.') && !value.contains(' ')
    }

    fun email(value: String): FieldError? =
        when {
            value.isBlank() -> FieldError.Empty
            !isEmailValid(value.trim()) -> FieldError.InvalidEmail
            else -> null
        }

    fun loginPassword(value: String): FieldError? = if (value.isEmpty()) FieldError.Empty else null

    fun newPassword(value: String): FieldError? =
        when {
            value.isEmpty() -> FieldError.Empty
            value.length < MIN_PASSWORD_LENGTH -> FieldError.TooShort
            else -> null
        }

    fun confirmPassword(
        password: String,
        confirm: String,
    ): FieldError? = if (password != confirm) FieldError.Mismatch else null

    fun code(value: String): FieldError? = if (value.length != CODE_LENGTH) FieldError.CodeIncomplete else null

    fun passwordChecks(password: String): PasswordChecks {
        val hasLength = password.length >= MIN_PASSWORD_LENGTH
        val hasUpperAndNumber = password.any(Char::isUpperCase) && password.any(Char::isDigit)
        val hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        val strength =
            if (password.isEmpty()) {
                0
            } else {
                listOf(hasLength, hasUpperAndNumber, hasSymbol, password.length >= STRONG_PASSWORD_LENGTH).count { it }
            }
        return PasswordChecks(hasLength, hasUpperAndNumber, hasSymbol, strength)
    }
}
