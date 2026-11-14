package com.kshrd.core

/** Build-time switches from `local.properties` (see local.properties.example). */
object AppConfig {
    /** Every screen on the in-memory sample data instead of the v2 API. */
    val useMockData: Boolean = BuildConfig.USE_MOCK_DATA

    /** The web app's sign-up page, or `null` to hide the link (until the coordinator gives the real URL). */
    val signUpUrl: String? = BuildConfig.SIGN_UP_URL.ifBlank { null }
}
