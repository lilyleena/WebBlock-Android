package com.kshrd.core.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

internal const val AUTHORIZATION = "Authorization"
internal const val BEARER = "Bearer "

/** User v2 routes that take no token: sign-in, refresh, sign-out and password reset. */
private val PUBLIC_PATH_PREFIXES = listOf("/api/user/v2/sessions", "/api/user/v2/password-resets")

/**
 * Adds `Authorization: Bearer <accessToken>` to every v2 call except the public User routes.
 * It runs on OkHttp's threads, so blocking for a refresh here is fine.
 */
@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val session: SessionManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (isPublic(request.url.encodedPath) || request.header(AUTHORIZATION) != null) return chain.proceed(request)
            val token =
                try {
                    runBlocking { session.accessToken() }
                } catch (e: HttpException) {
                    // OkHttp only reports IOExceptions to the caller; anything else would crash its thread.
                    throw IOException("Session refresh failed (HTTP ${e.code()})", e)
                }
            val authorized = token?.let { request.newBuilder().header(AUTHORIZATION, BEARER + it).build() } ?: request
            return chain.proceed(authorized)
        }

        internal companion object {
            fun isPublic(path: String): Boolean = PUBLIC_PATH_PREFIXES.any { path == it || path.startsWith("$it/") }
        }
    }
