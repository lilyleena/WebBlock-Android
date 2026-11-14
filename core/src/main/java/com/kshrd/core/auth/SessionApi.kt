package com.kshrd.core.auth

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * The refresh call alone. It runs on a client without the auth interceptor and authenticator,
 * so a refresh never triggers another refresh.
 */
interface SessionApi {
    @POST("api/user/v2/sessions/refresh")
    suspend fun refresh(
        @Body body: RefreshTokenRequest,
    ): TokenSetDto
}
