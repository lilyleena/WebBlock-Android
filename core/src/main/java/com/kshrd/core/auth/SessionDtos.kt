package com.kshrd.core.auth

import kotlinx.serialization.Serializable

/** User v2 `TokenSet`: the answer of `POST /sessions`, `/sessions/google` and `/sessions/refresh`. */
@Serializable
data class TokenSetDto(
    val accessToken: String,
    val refreshToken: String,
    /** Seconds. */
    val expiresIn: Long,
    /** Seconds; 0 or less means unknown (an offline session without a maximum, AWS finding A8). */
    val refreshExpiresIn: Long,
    val tokenType: String? = null,
    val userId: String,
    val email: String,
    val roles: List<String> = emptyList(),
)

/** Body of `POST /sessions/refresh` and `POST /sessions/revocations`. */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)
