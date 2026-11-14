package com.kshrd.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoredTokensTest {
    private val issuedAt = 1_000_000L

    private fun tokens(
        expiresIn: Long,
        refreshExpiresIn: Long,
    ) = StoredTokens.from(
        TokenSetDto("access", "refresh", expiresIn, refreshExpiresIn, "Bearer", "user-1", "owner@example.com", listOf("USER")),
        issuedAt,
    )

    @Test
    fun `refreshes at 80 percent of a known refresh life`() {
        // Online session: access 3600 s, SSO idle 3600 s (60 min).
        val stored = tokens(expiresIn = 3600, refreshExpiresIn = 3600)

        assertFalse(stored.needsRefresh(issuedAt + 2_879_000))
        assertTrue(stored.needsRefresh(issuedAt + 2_880_000))
    }

    @Test
    fun `refreshes 30 seconds before the access token runs out when that comes first`() {
        val stored = tokens(expiresIn = 300, refreshExpiresIn = 2_592_000)

        assertFalse(stored.needsRefresh(issuedAt + 269_000))
        assertTrue(stored.needsRefresh(issuedAt + 271_000))
    }

    @Test
    fun `an unknown refresh expiry keeps the session and refreshes only near the access expiry`() {
        val stored = tokens(expiresIn = 3600, refreshExpiresIn = 0)

        assertNull(stored.refreshExpiresAtMillis)
        assertTrue(stored.canRefresh(issuedAt + 365L * 24 * 3_600_000))
        assertFalse(stored.needsRefresh(issuedAt + 3_000_000))
        assertTrue(stored.needsRefresh(issuedAt + 3_571_000))
    }

    @Test
    fun `a negative refresh expiry is unknown too`() {
        assertNull(tokens(expiresIn = 3600, refreshExpiresIn = -1).refreshExpiresAtMillis)
    }

    @Test
    fun `a known refresh expiry ends canRefresh`() {
        val stored = tokens(expiresIn = 3600, refreshExpiresIn = 1800)

        assertEquals(issuedAt + 1_800_000, stored.refreshExpiresAtMillis)
        assertTrue(stored.canRefresh(issuedAt + 1_799_000))
        assertFalse(stored.canRefresh(issuedAt + 1_800_000))
    }
}
