package com.kshrd.features.auth.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.core.session.OnboardingStore
import com.kshrd.core.session.SessionOwner
import com.kshrd.core.session.SessionStore
import com.kshrd.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/** Signs the owner in and, on success, starts the session. */
class SignInUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
        private val session: SessionStore,
    ) {
        operator fun invoke(
            email: String,
            password: String,
        ): Flow<DataState<SessionOwner>> =
            repository
                .signIn(email.trim(), password)
                .asUseCaseFlow()
                .onEach { if (it is DataState.Success) session.signIn(it.data) }
    }

/** "Continue with Google": signs in with the chosen Google account and starts the session. */
class SignInWithGoogleUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
        private val session: SessionStore,
    ) {
        operator fun invoke(): Flow<DataState<SessionOwner>> =
            repository
                .signInWithGoogle()
                .asUseCaseFlow()
                .onEach { if (it is DataState.Success) session.signIn(it.data) }
    }

/** Whether this account has already seen the intro. */
class HasSeenOnboardingUseCase
    @Inject
    constructor(
        private val store: OnboardingStore,
    ) {
        operator fun invoke(email: String): Boolean = store.hasSeen(email)
    }

/** Marks the intro as seen for the signed-in account. */
class MarkOnboardingSeenUseCase
    @Inject
    constructor(
        private val store: OnboardingStore,
        private val session: SessionStore,
    ) {
        operator fun invoke() {
            session.owner.value?.email?.let(store::markSeen)
        }
    }

class RequestResetCodeUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(email: String): Flow<DataState<Unit>> = repository.requestResetCode(email.trim()).asUseCaseFlow()
    }

class VerifyResetCodeUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            email: String,
            code: String,
        ): Flow<DataState<Unit>> = repository.verifyResetCode(email.trim(), code).asUseCaseFlow()
    }

class ResetPasswordUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        operator fun invoke(
            email: String,
            newPassword: String,
        ): Flow<DataState<Unit>> = repository.resetPassword(email.trim(), newPassword).asUseCaseFlow()
    }
