package com.kshrd.starter.ui.navigation

import androidx.lifecycle.ViewModel
import com.kshrd.core.auth.SessionManager
import com.kshrd.core.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Watches for the session ending on the server (a refused refresh: a password change, a suspension, the offline
 * session's idle time). The owner and the selected website are forgotten, and the graph goes back to sign-in.
 */
@HiltViewModel
class SessionWatcherViewModel
    @Inject
    constructor(
        sessionManager: SessionManager,
        private val sessionStore: SessionStore,
    ) : ViewModel() {
        val expired: Flow<Unit> = sessionManager.expired.onEach { sessionStore.signOut() }
    }
