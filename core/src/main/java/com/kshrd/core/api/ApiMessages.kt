package com.kshrd.core.api

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_SERVER_ERROR_MIN = 500

/**
 * Plain sentences for the problem codes the owner can act on, the same wording as the iOS app.
 * Anything else shows the server's own `detail` or `title`.
 */
object ApiMessages {
    const val NO_CONNECTION = "No internet connection."
    const val UNEXPECTED_ANSWER = "The server sent an answer the app doesn't understand. Please try again later."
    const val GENERIC = "Something went wrong. Please try again."
    const val CHANGED_ELSEWHERE = "This product was changed somewhere else. Pull to refresh, then try again."

    private val byCode =
        mapOf(
            "invalid-credentials" to "The email or password is wrong.",
            "session-expired" to "Your session has ended. Please sign in again.",
            "account-suspended" to "This account is suspended. Contact WebBlock support.",
            "account-not-found" to "Create your account on the WebBlock website first, then sign in here.",
            "account-email-in-use" to "This email already has a WebBlock account. Sign in with its email and password.",
            "otp-too-many-attempts" to "Too many wrong codes. Ask for a new code and try again.",
            "otp-invalid" to "That code isn't right. Try again.",
            "otp-expired" to "That code has expired. Ask for a new one.",
            "verification-token-invalid" to "The reset has expired. Start again.",
            "password-too-weak" to "Use 8 or more characters, with an upper case letter, a lower case letter and a digit.",
            "storage-limit-reached" to "Your website's photo storage is full. Remove photos on the WebBlock website, then try again.",
            "photo-limit-reached" to "Your plan's photo limit is reached. Remove a photo or upgrade your plan on the WebBlock website.",
        )

    fun forProblem(error: ApiException): String =
        when {
            error.code == "otp-resend-too-soon" -> "Wait ${waitText(error.retryAfterSeconds)} before asking for a new code."
            else ->
                byCode[error.code]
                    ?: error.detail?.takeIf { it.isNotBlank() }
                    ?: error.title?.takeIf { it.isNotBlank() }
                    ?: forStatus(error.status)
        }

    private fun waitText(seconds: Int?): String =
        when (seconds) {
            null -> "a minute"
            1 -> "1 second"
            else -> "$seconds seconds"
        }

    private fun forStatus(status: Int): String =
        when {
            status == HTTP_UNAUTHORIZED -> "Please sign in again."
            status == HTTP_FORBIDDEN -> "You don't have permission to do this."
            status == HTTP_NOT_FOUND -> "Not found."
            status >= HTTP_SERVER_ERROR_MIN -> "Server error. Please try again later."
            else -> GENERIC
        }
}
