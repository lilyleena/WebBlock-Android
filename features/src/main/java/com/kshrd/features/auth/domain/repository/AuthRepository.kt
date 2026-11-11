package com.kshrd.features.auth.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.SessionOwner
import kotlinx.coroutines.flow.Flow

/** The seam to the future auth backend. Today it is backed by an in-memory data source. */
interface AuthRepository {
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
