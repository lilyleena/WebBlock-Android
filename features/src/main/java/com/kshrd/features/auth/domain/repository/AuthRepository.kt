package com.kshrd.features.auth.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.SessionOwner
import kotlinx.coroutines.flow.Flow

/** The seam to the auth backend: the in-memory sample source, or User v2 (docs/API-MAPPING.md 2.1). */
interface AuthRepository {
    /** The stored session's owner after a refresh, or `null` without one (then the sign-in screen stays). */
    suspend fun restoreSession(): SessionOwner?

    /** Ends the session on the server (best effort) and forgets it on the device. */
    suspend fun signOut()

    fun signIn(
        email: String,
        password: String,
    ): Flow<DataState<SessionOwner>>

    fun signInWithGoogle(): Flow<DataState<SessionOwner>>

    fun requestResetCode(email: String): Flow<DataState<Unit>>

    fun verifyResetCode(
        email: String,
        code: String,
    ): Flow<DataState<Unit>>

    fun resetPassword(
        email: String,
        newPassword: String,
    ): Flow<DataState<Unit>>
}
