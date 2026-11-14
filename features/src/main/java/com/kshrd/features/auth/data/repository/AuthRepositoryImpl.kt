package com.kshrd.features.auth.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.toDataState
import com.kshrd.core.session.SessionOwner
import com.kshrd.features.auth.data.source.InMemoryAuthDataSource
import com.kshrd.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/** The sample-data auth (`use.mock.data=true`); `ApiAuthRepository` is the User v2 one. */
class AuthRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryAuthDataSource,
    ) : AuthRepository {
        /** The sample data keeps no session between launches. */
        override suspend fun restoreSession(): SessionOwner? = null

        override suspend fun signOut() = Unit

        override fun signIn(
            email: String,
            password: String,
        ): Flow<DataState<SessionOwner>> = flow { emit(source.signIn(email, password).toDataState()) }

        override fun signInWithGoogle(): Flow<DataState<SessionOwner>> = flow { emit(source.signInWithGoogle().toDataState()) }

        override fun requestResetCode(email: String): Flow<DataState<Unit>> = flow { emit(source.requestResetCode().toDataState()) }

        override fun verifyResetCode(
            email: String,
            code: String,
        ): Flow<DataState<Unit>> = flow { emit(source.verifyResetCode(code).toDataState()) }

        override fun resetPassword(
            email: String,
            newPassword: String,
        ): Flow<DataState<Unit>> = flow { emit(source.resetPassword().toDataState()) }
    }
