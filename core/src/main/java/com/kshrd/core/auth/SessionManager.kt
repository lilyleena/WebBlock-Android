package com.kshrd.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403

/** A refresh answering one of these ends the session (docs/API-MAPPING.md section 1). */
private val SESSION_ENDING_STATUSES = setOf(HTTP_BAD_REQUEST, HTTP_UNAUTHORIZED, HTTP_FORBIDDEN)

/**
 * Owns the signed-in session's tokens: keeps them in the [TokenStore], hands out a valid access token,
 * and renews it with `POST /api/user/v2/sessions/refresh` (early, and once after a 401).
 *
 * Concurrent callers share one refresh: Keycloak rotates the refresh token, so a second refresh with the old one
 * would fail and end the session.
 */
@Singleton
class SessionManager internal constructor(
    private val store: TokenStore,
    private val sessionApi: SessionApi,
    private val now: () -> Long,
) {
    @Inject
    constructor(store: TokenStore, sessionApi: SessionApi) : this(store, sessionApi, System::currentTimeMillis)

    private val mutex = Mutex()

    @Volatile
    private var loaded = false

    @Volatile
    private var tokens: StoredTokens? = null

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the refresh token stops working, so the app goes back to sign-in. */
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    /** A session is stored whose refresh token may still work. */
    fun hasSession(): Boolean = current()?.canRefresh(now()) == true

    /** For `POST /sessions/revocations` at sign-out. */
    fun refreshToken(): String? = current()?.refreshToken

    /** Keeps the tokens of a new sign-in or refresh. */
    fun start(set: TokenSetDto) {
        val fresh = StoredTokens.from(set, now())
        tokens = fresh
        loaded = true
        store.save(fresh)
    }

    /** Forgets the session (sign-out); does not emit [expired]. */
    fun clear() {
        tokens = null
        loaded = true
        store.clear()
    }

    /**
     * An access token to send, renewed first when it is due. `null` without a session or when the session ended.
     * A failed renewal without an answer (no network) still returns the current token while it is valid.
     */
    suspend fun accessToken(): String? {
        val current = current() ?: return null
        if (!current.needsRefresh(now())) return current.accessToken
        return try {
            refreshShared(staleAccessToken = null)
        } catch (e: IOException) {
            if (current.isAccessValid(now())) current.accessToken else throw e
        }
    }

    /** After a 401 for [staleAccessToken]: the renewed token, or `null` when the session ended. */
    suspend fun refreshAfterUnauthorized(staleAccessToken: String): String? = refreshShared(staleAccessToken)

    /** Renews now, whatever the expiry (restoring the session on launch). `null` when the session ended. */
    suspend fun refreshNow(): String? =
        mutex.withLock {
            val current = current() ?: return@withLock null
            performRefresh(current.refreshToken)
        }

    private suspend fun refreshShared(staleAccessToken: String?): String? =
        mutex.withLock {
            val current = current() ?: return@withLock null
            val renewedMeanwhile =
                if (staleAccessToken != null) current.accessToken != staleAccessToken else !current.needsRefresh(now())
            if (renewedMeanwhile) current.accessToken else performRefresh(current.refreshToken)
        }

    /** Must run under [mutex]. */
    private suspend fun performRefresh(refreshToken: String): String? {
        val set =
            try {
                sessionApi.refresh(RefreshTokenRequest(refreshToken))
            } catch (e: HttpException) {
                if (e.code() in SESSION_ENDING_STATUSES) {
                    endSession()
                    return null
                }
                throw e
            }
        start(set)
        return set.accessToken
    }

    private fun endSession() {
        clear()
        _expired.tryEmit(Unit)
    }

    private fun current(): StoredTokens? {
        if (!loaded) {
            synchronized(this) {
                if (!loaded) {
                    tokens = store.load()
                    loaded = true
                }
            }
        }
        return tokens
    }
}
