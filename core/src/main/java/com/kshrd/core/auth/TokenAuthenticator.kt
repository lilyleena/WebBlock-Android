package com.kshrd.core.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On a 401, renews the session once and repeats the call with the new token. A second 401, a call sent without a
 * token, or a refused refresh gives up (the 401 reaches the caller; a refused refresh also ends the session).
 */
@Singleton
class TokenAuthenticator
    @Inject
    constructor(
        private val session: SessionManager,
    ) : Authenticator {
        override fun authenticate(
            route: Route?,
            response: Response,
        ): Request? {
            if (response.priorResponse != null) return null
            val sent = response.request.header(AUTHORIZATION)?.removePrefix(BEARER) ?: return null
            val fresh =
                try {
                    runBlocking { session.refreshAfterUnauthorized(sent) }
                } catch (e: HttpException) {
                    throw IOException("Session refresh failed (HTTP ${e.code()})", e)
                }
            return fresh?.let { response.request.newBuilder().header(AUTHORIZATION, BEARER + it).build() }
        }
    }
