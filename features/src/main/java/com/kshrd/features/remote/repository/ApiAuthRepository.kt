package com.kshrd.features.remote.repository

import android.util.Log
import com.kshrd.core.api.ApiException
import com.kshrd.core.api.apiCall
import com.kshrd.core.api.apiFlow
import com.kshrd.core.auth.RefreshTokenRequest
import com.kshrd.core.auth.SessionManager
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.ErrorCode
import com.kshrd.core.session.SessionOwner
import com.kshrd.features.auth.domain.repository.AuthRepository
import com.kshrd.features.remote.api.UserApi
import com.kshrd.features.remote.dto.EmailRequest
import com.kshrd.features.remote.dto.NewPasswordRequest
import com.kshrd.features.remote.dto.SignInRequest
import com.kshrd.features.remote.dto.VerifyCodeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ApiAuthRepository"

/** Shown when an admin-only account signs in: the app is for website owners (as iOS). */
internal const val OWNERS_ONLY = "This app is for website owners. Admins use the WebBlock website."

/** Google sign-in stays off until User's change is on AWS and the owner turns it on (G1). */
internal const val GOOGLE_OFF = "Google sign-in isn't available in the app yet. Sign in with your email and password."

/** User v2 sign-in, session and password reset (docs/API-MAPPING.md 2.1). */
@Singleton
class ApiAuthRepository
    @Inject
    constructor(
        private val userApi: UserApi,
        private val session: SessionManager,
    ) : AuthRepository {
        /** `POST /password-resets/verifications` gives a token for the confirm step; kept in memory only, by email. */
        private val verificationTokens = ConcurrentHashMap<String, String>()

        override fun signIn(
            email: String,
            password: String,
        ): Flow<DataState<SessionOwner>> =
            apiFlow {
                val tokens = userApi.signIn(SignInRequest(email, password))
                session.start(tokens)
                if ("ADMIN" in tokens.roles && "USER" !in tokens.roles) {
                    signOut()
                    throw OwnersOnlyException()
                }
                owner()
            }

        override fun signInWithGoogle(): Flow<DataState<SessionOwner>> =
            flowOf(DataState.Error(ErrorCode.UNKNOWN_ERROR, GOOGLE_OFF))

        override suspend fun restoreSession(): SessionOwner? {
            if (!session.hasSession()) return null
            // A refused refresh ends the session (SessionManager); without an answer the stored session stays and
            // the sign-in screen shows. Either way the owner can sign in again.
            return try {
                session.refreshNow()?.let { apiCall { owner() } }
            } catch (e: IOException) {
                Log.w(TAG, "Session not restored: no answer", e)
                null
            } catch (e: HttpException) {
                Log.w(TAG, "Session not restored: HTTP ${e.code()}", e)
                null
            } catch (e: ApiException) {
                Log.w(TAG, "Session not restored: ${e.code}", e)
                null
            }
        }

        override suspend fun signOut() {
            val refreshToken = session.refreshToken()
            session.clear()
            if (refreshToken != null) runCatching { userApi.signOut(RefreshTokenRequest(refreshToken)) }
        }

        override fun requestResetCode(email: String): Flow<DataState<Unit>> = apiFlow { userApi.requestPasswordReset(EmailRequest(email)) }

        override fun verifyResetCode(
            email: String,
            code: String,
        ): Flow<DataState<Unit>> =
            apiFlow {
                verificationTokens[email.lowercase()] = userApi.verifyResetCode(VerifyCodeRequest(email, code)).verificationToken
            }

        override fun resetPassword(
            email: String,
            newPassword: String,
        ): Flow<DataState<Unit>> {
            val token =
                verificationTokens[email.lowercase()]
                    ?: return flowOf(DataState.Error(ErrorCode.UNKNOWN_ERROR, "The reset has expired. Start again."))
            return apiFlow {
                userApi.confirmPasswordReset(NewPasswordRequest(token, newPassword, newPassword))
                verificationTokens.remove(email.lowercase())
            }
        }

        private suspend fun owner(): SessionOwner {
            val profile = userApi.me()
            return SessionOwner(id = profile.id, name = profile.fullName, email = profile.email, plan = profile.plan)
        }
    }

/** An admin-only account: its message is the owner-facing sentence. */
internal class OwnersOnlyException : Exception(OWNERS_ONLY)
