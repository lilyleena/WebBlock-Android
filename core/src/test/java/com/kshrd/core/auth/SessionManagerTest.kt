package com.kshrd.core.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SessionManagerTest {
    private class MemoryStore(
        var tokens: StoredTokens? = null,
    ) : TokenStore {
        override fun load(): StoredTokens? = tokens

        override fun save(tokens: StoredTokens) {
            this.tokens = tokens
        }

        override fun clear() {
            tokens = null
        }
    }

    private class FakeSessionApi : SessionApi {
        val calls = mutableListOf<String>()
        var answer: suspend (String) -> TokenSetDto = { tokenSet("access-new", "refresh-new") }

        override suspend fun refresh(body: RefreshTokenRequest): TokenSetDto {
            calls += body.refreshToken
            return answer(body.refreshToken)
        }
    }

    private var now = 1_000_000L
    private val store = MemoryStore()
    private val api = FakeSessionApi()
    private val session = SessionManager(store, api) { now }

    @Test
    fun `a fresh session hands out its token without a refresh`() =
        runTest {
            session.start(tokenSet("access-1", "refresh-1"))

            assertEquals("access-1", session.accessToken())
            assertTrue(api.calls.isEmpty())
        }

    @Test
    fun `a token that is due is renewed first, and the rotated refresh token is kept`() =
        runTest {
            session.start(tokenSet("access-1", "refresh-1", refreshExpiresIn = 3600))
            now += 2_900_000

            assertEquals("access-new", session.accessToken())
            assertEquals(listOf("refresh-1"), api.calls)
            assertEquals("refresh-new", store.tokens?.refreshToken)
        }

    @Test
    fun `parallel 401s share one refresh`() =
        runTest {
            session.start(tokenSet("access-1", "refresh-1"))
            val gate = CompletableDeferred<Unit>()
            api.answer = {
                gate.await()
                tokenSet("access-2", "refresh-2")
            }

            val first = async { session.refreshAfterUnauthorized("access-1") }
            val second = async { session.refreshAfterUnauthorized("access-1") }
            gate.complete(Unit)

            assertEquals("access-2", first.await())
            assertEquals("access-2", second.await())
            assertEquals(listOf("refresh-1"), api.calls)
        }

    @Test
    fun `a refused refresh ends the session and says so`() =
        runTest {
            session.start(tokenSet("access-1", "refresh-1"))
            api.answer = { throw HttpException(Response.error<TokenSetDto>(401, "".toResponseBody())) }
            val expired = async(start = CoroutineStart.UNDISPATCHED) { session.expired.first() }

            assertNull(session.refreshAfterUnauthorized("access-1"))
            expired.await()
            assertFalse(session.hasSession())
            assertNull(store.tokens)
        }

    @Test
    fun `no network keeps the session and the still-valid token`() =
        runTest {
            session.start(tokenSet("access-1", "refresh-1", refreshExpiresIn = 3600))
            now += 2_900_000
            api.answer = { throw IOException("offline") }

            assertEquals("access-1", session.accessToken())
            assertTrue(session.hasSession())
        }

    @Test
    fun `the stored session is read on first use`() =
        runTest {
            store.tokens = StoredTokens("stored-access", "stored-refresh", now, now + 3_600_000, null)
            val restored = SessionManager(store, api) { now }

            assertTrue(restored.hasSession())
            assertEquals("stored-refresh", restored.refreshToken())
            assertEquals("stored-access", restored.accessToken())
        }

    @Test
    fun `sign-out forgets the session`() {
        session.start(tokenSet("access-1", "refresh-1"))

        session.clear()

        assertFalse(session.hasSession())
        assertNull(store.tokens)
    }

    private companion object {
        fun tokenSet(
            access: String,
            refresh: String,
            refreshExpiresIn: Long = 2_592_000,
        ) = TokenSetDto(access, refresh, 3600, refreshExpiresIn, "Bearer", "user-1", "owner@example.com", listOf("USER"))
    }
}
