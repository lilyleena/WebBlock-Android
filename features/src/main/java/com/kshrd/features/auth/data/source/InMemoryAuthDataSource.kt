package com.kshrd.features.auth.data.source

import com.kshrd.core.session.SessionOwner
import com.kshrd.features.mock.MockIds
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake auth backend, same rules as the iOS `MockWebBlockService`:
 * - any email containing "@" with a password of 8+ characters signs in;
 * - Google sign-in returns the sample account;
 * - the reset email is "sent" for any address, and any 6-digit code is accepted.
 */
@Singleton
class InMemoryAuthDataSource
    @Inject
    constructor() {
        suspend fun signIn(
            email: String,
            password: String,
        ): Result<SessionOwner> {
            delay(LATENCY_MS)
            return if (email.contains("@") && password.length >= MIN_PASSWORD) {
                Result.success(SessionOwner(MockIds.OWNER_ALEX, SAMPLE_NAME, email))
            } else {
                Result.failure(IllegalArgumentException("Email or password is incorrect. Check them and try again."))
            }
        }

        suspend fun signInWithGoogle(): Result<SessionOwner> {
            delay(LATENCY_MS)
            return Result.success(SessionOwner(MockIds.OWNER_ALEX, SAMPLE_NAME, SAMPLE_EMAIL))
        }

        suspend fun requestResetCode(): Result<Unit> {
            delay(LATENCY_MS)
            return Result.success(Unit)
        }

        suspend fun verifyResetCode(code: String): Result<Unit> {
            delay(LATENCY_MS)
            return if (code.length == CODE_LENGTH) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("That code isn’t right. Try again."))
            }
        }

        suspend fun resetPassword(): Result<Unit> {
            delay(LATENCY_MS)
            return Result.success(Unit)
        }

        private companion object {
            const val SAMPLE_NAME = "Sovannarith Soeun"
            const val SAMPLE_EMAIL = "sovannarith@gmail.com"
            const val MIN_PASSWORD = 8
            const val CODE_LENGTH = 6
            const val LATENCY_MS = 450L
        }
    }
