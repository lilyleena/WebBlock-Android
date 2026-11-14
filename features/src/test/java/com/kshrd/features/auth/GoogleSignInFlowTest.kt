package com.kshrd.features.auth

import com.kshrd.features.auth.data.google.GoogleCallback
import com.kshrd.features.auth.data.google.GoogleSignInFlow
import com.kshrd.features.auth.data.google.Pkce
import com.kshrd.features.auth.data.google.googleAuthorizeUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI
import java.net.URLDecoder
import java.security.SecureRandom

class GoogleSignInFlowTest {
    private val realm = "https://wb-auth.roa-dev.digital/realms/webblock"
    private var now = 1_000_000L
    private val flow = GoogleSignInFlow(SecureRandom(), now = { now })

    private fun paramsOf(url: String): Map<String, String> =
        URI(url).rawQuery.split('&').associate { part ->
            part.substringBefore('=') to URLDecoder.decode(part.substringAfter('='), "UTF-8")
        }

    @Test
    fun `PKCE S256 matches the RFC 7636 example`() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", Pkce.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
    }

    @Test
    fun `a verifier is 43 Base64url characters and never repeats`() {
        val random = SecureRandom()
        val first = Pkce.newVerifier(random)

        assertTrue(Regex("^[A-Za-z0-9_-]{43}$").matches(first))
        assertNotEquals(first, Pkce.newVerifier(random))
    }

    @Test
    fun `the authorize URL has the same parameters as iOS`() {
        val url = googleAuthorizeUrl(realm, codeChallenge = "challenge", state = "state-1")

        assertTrue(url.startsWith("$realm/protocol/openid-connect/auth?"))
        assertEquals(
            mapOf(
                "client_id" to "webblock-ios",
                "response_type" to "code",
                "redirect_uri" to "com.webblock.app:/oauth2redirect",
                "scope" to "openid email profile offline_access",
                "code_challenge" to "challenge",
                "code_challenge_method" to "S256",
                "kc_idp_hint" to "google",
                "state" to "state-1",
            ),
            paramsOf(url),
        )
        assertTrue("scope=openid%20email%20profile%20offline_access" in url)
    }

    @Test
    fun `the redirect with the right state gives the code and this sign-in's verifier`() {
        val params = paramsOf(flow.start(realm))

        val callback = flow.handleRedirect("com.webblock.app:/oauth2redirect?state=${params["state"]}&code=abc%2F123&session_state=x")

        val code = callback as GoogleCallback.Code
        assertEquals("abc/123", code.code)
        assertEquals(params["code_challenge"], Pkce.challenge(code.codeVerifier))
    }

    @Test
    fun `a redirect nobody asked for is ignored`() {
        assertNull(flow.handleRedirect("com.webblock.app:/oauth2redirect?state=s&code=c"))
    }

    @Test
    fun `a wrong state is ignored and does not cancel the real sign-in`() {
        val state = paramsOf(flow.start(realm))["state"]

        assertNull(flow.handleRedirect("com.webblock.app:/oauth2redirect?state=forged&code=evil"))
        assertTrue(flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$state&code=good") is GoogleCallback.Code)
    }

    @Test
    fun `the answer is accepted once`() {
        val state = paramsOf(flow.start(realm))["state"]
        val redirect = "com.webblock.app:/oauth2redirect?state=$state&code=c"

        assertTrue(flow.handleRedirect(redirect) is GoogleCallback.Code)
        assertNull(flow.handleRedirect(redirect))
    }

    @Test
    fun `another scheme or path is ignored`() {
        val state = paramsOf(flow.start(realm))["state"]

        assertNull(flow.handleRedirect("com.webblock.app:/other?state=$state&code=c"))
        assertNull(flow.handleRedirect("https://evil.example/oauth2redirect?state=$state&code=c"))
    }

    @Test
    fun `a refusal is a silent cancel, other errors are shown`() {
        val first = paramsOf(flow.start(realm))["state"]
        assertEquals(GoogleCallback.Cancelled, flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$first&error=access_denied"))

        val second = paramsOf(flow.start(realm))["state"]
        val failed = flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$second&error=server_error&error_description=Try+later")
        assertEquals(GoogleCallback.Failed("Try later"), failed)
    }

    @Test
    fun `an answer after ten minutes is too late`() {
        val state = paramsOf(flow.start(realm))["state"]
        now += 10 * 60 * 1000L + 1

        assertEquals(
            GoogleCallback.Failed("Google sign-in took too long. Try again."),
            flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$state&code=c"),
        )
    }

    @Test
    fun `a new sign-in replaces an unfinished one`() {
        val old = paramsOf(flow.start(realm))["state"]
        val new = paramsOf(flow.start(realm))["state"]

        assertNull(flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$old&code=c"))
        assertTrue(flow.handleRedirect("com.webblock.app:/oauth2redirect?state=$new&code=c") is GoogleCallback.Code)
    }
}
