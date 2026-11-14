package com.kshrd.features.auth.data.google

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** The public Keycloak client the iOS and Android apps share (G1). */
const val GOOGLE_CLIENT_ID = "webblock-ios"

/** AppAuth-style custom-scheme redirect, the same as iOS; Keycloak's `webblock-ios` client lists it. */
const val GOOGLE_REDIRECT_URI = "com.webblock.app:/oauth2redirect"

private const val VERIFIER_BYTES = 32
private const val STATE_BYTES = 32

/** A browser sign-in that isn't back after this long is dropped. */
private const val PENDING_LIFETIME_MILLIS = 10 * 60 * 1000L

/** What a redirect back from the browser means. */
sealed interface GoogleCallback {
    /** The authorization code for `POST /api/user/v2/sessions/google`, with this sign-in's PKCE verifier. */
    data class Code(
        val code: String,
        val codeVerifier: String,
    ) : GoogleCallback

    /** The owner closed or refused the Google page: no message. */
    data object Cancelled : GoogleCallback

    data class Failed(
        val message: String,
    ) : GoogleCallback
}

/** RFC 7636 PKCE with S256, and the `state` value: random, Base64url without padding. */
object Pkce {
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    /** 32 random bytes: a 43-character verifier. */
    fun newVerifier(random: SecureRandom): String = encoder.encodeToString(ByteArray(VERIFIER_BYTES).also(random::nextBytes))

    fun challenge(verifier: String): String =
        encoder.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))

    fun newState(random: SecureRandom): String = encoder.encodeToString(ByteArray(STATE_BYTES).also(random::nextBytes))
}

/** Keycloak's authorize URL, the same parameters as iOS, with `kc_idp_hint=google` so Google opens directly. */
fun googleAuthorizeUrl(
    realmUrl: String,
    codeChallenge: String,
    state: String,
): String {
    val query =
        listOf(
            "client_id" to GOOGLE_CLIENT_ID,
            "response_type" to "code",
            "redirect_uri" to GOOGLE_REDIRECT_URI,
            "scope" to "openid email profile offline_access",
            "code_challenge" to codeChallenge,
            "code_challenge_method" to "S256",
            "kc_idp_hint" to "google",
            "state" to state,
        ).joinToString("&") { (key, value) -> "$key=${encode(value)}" }
    return "${realmUrl.trimEnd('/')}/protocol/openid-connect/auth?$query"
}

/**
 * One "Continue with Google" at a time. Another app could register the same custom scheme, so a redirect is
 * accepted only while a sign-in this app started is pending, only with exactly its `state`, and only once; the PKCE
 * verifier never leaves the app, so a stolen code is useless anyway.
 */
class GoogleSignInFlow(
    private val random: SecureRandom = SecureRandom(),
    private val now: () -> Long = System::currentTimeMillis,
) {
    private class Pending(
        val state: String,
        val codeVerifier: String,
        val startedAtMillis: Long,
    )

    @Volatile
    private var pending: Pending? = null

    /** Starts a sign-in (replacing an unfinished one) and returns the URL to open in the browser. */
    fun start(realmUrl: String): String {
        val verifier = Pkce.newVerifier(random)
        val state = Pkce.newState(random)
        pending = Pending(state, verifier, now())
        return googleAuthorizeUrl(realmUrl, Pkce.challenge(verifier), state)
    }

    /**
     * The meaning of a redirect, or `null` to ignore it: nothing pending, not our redirect, or a `state` that isn't
     * this sign-in's (the pending sign-in stays, so a forged redirect can't cancel the real one).
     */
    @Synchronized
    fun handleRedirect(uri: String): GoogleCallback? {
        val current = pending ?: return null
        val params = queryOf(uri)?.takeIf { isOurRedirect(uri) } ?: return null
        if (params["state"] != current.state) return null
        pending = null
        val code = params["code"]
        val error = params["error"]
        return when {
            now() - current.startedAtMillis > PENDING_LIFETIME_MILLIS -> GoogleCallback.Failed(TOO_SLOW)
            error == "access_denied" -> GoogleCallback.Cancelled
            error != null -> GoogleCallback.Failed(params["error_description"] ?: DID_NOT_FINISH)
            code.isNullOrEmpty() -> GoogleCallback.Failed(DID_NOT_FINISH)
            else -> GoogleCallback.Code(code, current.codeVerifier)
        }
    }

    private companion object {
        const val TOO_SLOW = "Google sign-in took too long. Try again."
        const val DID_NOT_FINISH = "Google sign-in didn't finish. Try again."
    }
}

private val REDIRECT = URI(GOOGLE_REDIRECT_URI)

/** Exactly `com.webblock.app:/oauth2redirect` (scheme and path), whatever the query. */
private fun isOurRedirect(uri: String): Boolean =
    runCatching { URI(uri) }.getOrNull()?.let { it.scheme == REDIRECT.scheme && it.path == REDIRECT.path } == true

private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

/** The decoded query of a redirect, or `null` when it can't be parsed. */
internal fun queryOf(uri: String): Map<String, String>? =
    runCatching {
        URI(uri).rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.associate { part ->
            URLDecoder.decode(part.substringBefore('='), "UTF-8") to URLDecoder.decode(part.substringAfter('=', ""), "UTF-8")
        }
    }.getOrNull()
