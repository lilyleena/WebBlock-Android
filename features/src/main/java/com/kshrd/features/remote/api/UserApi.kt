package com.kshrd.features.remote.api

import com.kshrd.core.auth.RefreshTokenRequest
import com.kshrd.core.auth.TokenSetDto
import com.kshrd.features.remote.dto.AppSignInRequest
import com.kshrd.features.remote.dto.CodeSentDto
import com.kshrd.features.remote.dto.EmailRequest
import com.kshrd.features.remote.dto.NewPasswordRequest
import com.kshrd.features.remote.dto.ProfileDto
import com.kshrd.features.remote.dto.SignInRequest
import com.kshrd.features.remote.dto.VerificationDto
import com.kshrd.features.remote.dto.VerifyCodeRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** User v2 owner routes (docs/API-MAPPING.md 2.1). The refresh is `SessionApi` in :core. */
interface UserApi {
    @POST("api/user/v2/sessions")
    suspend fun signIn(
        @Body body: SignInRequest,
    ): TokenSetDto

    /** G1; off until the owner's Google decision is deployed. */
    @POST("api/user/v2/sessions/google")
    suspend fun signInWithGoogle(
        @Body body: AppSignInRequest,
    ): TokenSetDto

    @POST("api/user/v2/sessions/revocations")
    suspend fun signOut(
        @Body body: RefreshTokenRequest,
    )

    @GET("api/user/v2/me")
    suspend fun me(): ProfileDto

    @POST("api/user/v2/password-resets")
    suspend fun requestPasswordReset(
        @Body body: EmailRequest,
    ): CodeSentDto

    @POST("api/user/v2/password-resets/verifications")
    suspend fun verifyResetCode(
        @Body body: VerifyCodeRequest,
    ): VerificationDto

    @POST("api/user/v2/password-resets/confirmations")
    suspend fun confirmPasswordReset(
        @Body body: NewPasswordRequest,
    )
}
