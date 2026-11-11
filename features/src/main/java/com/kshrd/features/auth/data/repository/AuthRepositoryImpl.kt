package com.kshrd.features.auth.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.toDataState
import com.kshrd.core.session.SessionOwner
import com.kshrd.features.auth.data.source.InMemoryAuthDataSource
import com.kshrd.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/** Delegates to the in-memory source. Replace the source with a service/API when the backend exists. */
class AuthRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryAuthDataSource,
    ) : AuthRepository {
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
