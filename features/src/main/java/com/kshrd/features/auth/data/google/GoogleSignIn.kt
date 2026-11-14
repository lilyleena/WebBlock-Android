package com.kshrd.features.auth.data.google

import com.kshrd.core.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app-wide "Continue with Google" state: the sign-in screen starts it, the redirect activity hands the browser's
 * answer back, and the sign-in screen consumes it (kept until then, so a screen recreated meanwhile still gets it).
 */
@Singleton
class GoogleSignIn
    @Inject
    constructor() {
        private val flow = GoogleSignInFlow()
        private val _result = MutableStateFlow<GoogleCallback?>(null)
        val result: StateFlow<GoogleCallback?> = _result.asStateFlow()

        /** The browser URL for a new sign-in. */
        fun start(): String {
            _result.value = null
            return flow.start(AppConfig.authRealmUrl)
        }

        /** From the redirect activity; anything that isn't this sign-in's answer is ignored. */
        fun onRedirect(uri: String) {
            flow.handleRedirect(uri)?.let { _result.value = it }
        }

        /** The sign-in screen took the answer. */
        fun consume() {
            _result.value = null
        }
    }
