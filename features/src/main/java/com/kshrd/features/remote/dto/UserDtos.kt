package com.kshrd.features.remote.dto

import kotlinx.serialization.Serializable

// User v2 (`/api/user/v2`) wire types, contracts dev bdb5390. The TokenSet lives in :core (`TokenSetDto`).

/** `stayLoggedIn: true` asks for an offline session (G8): about 30 days idle, no maximum. */
@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    val stayLoggedIn: Boolean = true,
)

/** G1, used only when Google sign-in is turned on. */
@Serializable
data class AppSignInRequest(
    val code: String,
    val codeVerifier: String,
    val redirectUri: String,
)

@Serializable
data class ProfileDto(
    val id: String,
    val fullName: String,
    val email: String,
    /** `FREE_TRIAL`, `PLUS`, `PRO`, `ULTRA`; `null` while Payment is down. */
    val plan: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class EmailRequest(
    val email: String,
)

@Serializable
data class CodeSentDto(
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int,
)

@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String,
)

@Serializable
data class VerificationDto(
    val verificationToken: String,
    val expiresInSeconds: Int,
)

@Serializable
data class NewPasswordRequest(
    val verificationToken: String,
    val password: String,
    val confirmPassword: String,
)
