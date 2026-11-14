package com.kshrd.core.auth

import kotlinx.serialization.Serializable

private const val MILLIS_PER_SECOND = 1000L

/** Renew this long before the access token runs out. */
private const val ACCESS_MARGIN_MILLIS = 30_000L

/** Renew at this share of the refresh token's own life (the realm's SSO idle is shorter than the access token's). */
private const val REFRESH_SHARE = 0.8

/** The signed-in session's tokens. Never logged; only written to the encrypted [TokenStore]. */
@Serializable
data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val issuedAtMillis: Long,
    val accessExpiresAtMillis: Long,
    /** `null` when unknown (`refreshExpiresIn` ≤ 0): the session lasts until a refresh is refused. */
    val refreshExpiresAtMillis: Long?,
) {
    /** The refresh token may still work (always, when its expiry is unknown). */
    fun canRefresh(nowMillis: Long): Boolean = refreshExpiresAtMillis?.let { it > nowMillis } ?: true

    /** The access token can still be sent as it is. */
    fun isAccessValid(nowMillis: Long): Boolean = accessExpiresAtMillis > nowMillis

    /**
     * Renew early: 30 s before the access token runs out, or at 80 % of the refresh token's life when that is known.
     * With an unknown refresh expiry only the access token's expiry (or a 401) triggers a refresh.
     */
    fun needsRefresh(nowMillis: Long): Boolean {
        if (accessExpiresAtMillis - nowMillis < ACCESS_MARGIN_MILLIS) return true
        val refreshExpiresAt = refreshExpiresAtMillis ?: return false
        val renewAt = issuedAtMillis + ((refreshExpiresAt - issuedAtMillis) * REFRESH_SHARE).toLong()
        return nowMillis >= renewAt
    }

    companion object {
        fun from(
            set: TokenSetDto,
            nowMillis: Long,
        ): StoredTokens =
            StoredTokens(
                accessToken = set.accessToken,
                refreshToken = set.refreshToken,
                issuedAtMillis = nowMillis,
                accessExpiresAtMillis = nowMillis + set.expiresIn * MILLIS_PER_SECOND,
                refreshExpiresAtMillis = if (set.refreshExpiresIn > 0) nowMillis + set.refreshExpiresIn * MILLIS_PER_SECOND else null,
            )
    }
}
