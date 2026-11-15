package com.kshrd.core

/** Build-time switches from `local.properties` (see local.properties.example). */
object AppConfig {
    /** Every screen on the in-memory sample data instead of the v2 API. */
    val useMockData: Boolean = BuildConfig.USE_MOCK_DATA

    /** Reserved for the web app's sign-up page (`null` when unset). No screen reads it yet. */
    val signUpUrl: String? = BuildConfig.SIGN_UP_URL.ifBlank { null }

    /** Keycloak realm for "Continue with Google". */
    val authRealmUrl: String = BuildConfig.AUTH_REALM_URL.trimEnd('/')

    /** "Continue with Google" through the browser (the v2 API only). */
    val googleSignInEnabled: Boolean = BuildConfig.GOOGLE_SIGN_IN && !BuildConfig.USE_MOCK_DATA
}
