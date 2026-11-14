package com.kshrd.features.remote

import com.kshrd.core.api.ApiJson
import com.kshrd.core.auth.TokenSetDto
import com.kshrd.features.remote.dto.BusinessInfoBody
import com.kshrd.features.remote.dto.NotificationDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.ProductDto
import com.kshrd.features.remote.dto.SignInRequest
import com.kshrd.features.remote.dto.SiteStatusDto
import com.kshrd.features.remote.dto.allPages
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The wire types read the contract's JSON (contracts dev bdb5390) and write what the services expect. */
class DtoDecodingTest {
    @Test
    fun `sign-in asks for the long session`() {
        val json = ApiJson.encodeToString(SignInRequest.serializer(), SignInRequest("owner@example.com", "Secret123"))

        assertEquals("""{"email":"owner@example.com","password":"Secret123","stayLoggedIn":true}""", json)
    }

    @Test
    fun `a TokenSet with refreshExpiresIn 0 and extra fields is read`() {
        val set =
            ApiJson.decodeFromString(
                TokenSetDto.serializer(),
                """{"accessToken":"a","refreshToken":"r","expiresIn":3600,"refreshExpiresIn":0,"tokenType":"Bearer",
                   "userId":"u","email":"e@example.com","roles":["USER"],"scope":"openid offline_access"}""",
            )

        assertEquals(0L, set.refreshExpiresIn)
        assertEquals(listOf("USER"), set.roles)
    }

    @Test
    fun `a page of products with a hidden price`() {
        val page =
            ApiJson.decodeFromString(
                PageDto.serializer(ProductDto.serializer()),
                """{"items":[{"id":"p1","websiteId":"w1","name":"Tulips","description":"","category":"Flowers","price":null,
                   "imageUrl":null,"version":3,"createdAt":"2026-11-01T10:00:00Z","updatedAt":"2026-11-02T10:00:00Z"}],
                   "page":0,"size":200,"totalItems":1}""",
            )

        assertNull(page.items.single().price)
        assertEquals(3L, page.items.single().version)
    }

    @Test
    fun `a notification without target, from a server older than G5`() {
        val notification =
            ApiJson.decodeFromString(
                NotificationDto.serializer(),
                """{"id":"2xYz_9","title":"Petal & Stem","message":"Deploy 4 is live","kind":"SUCCESS",
                   "websiteId":"w1","createdAt":"2026-11-14T03:00:00Z","read":false}""",
            )

        assertNull(notification.target)
        assertNull(notification.detail)
    }

    @Test
    fun `site status before the uptime check`() {
        val status =
            ApiJson.decodeFromString(
                SiteStatusDto.serializer(),
                """{"websiteId":"w1","online":true,"uptimePercent":null,"checkedAt":null}""",
            )

        assertNull(status.uptimePercent)
    }

    @Test
    fun `an empty maps link is sent as null`() {
        val body = BusinessInfoBody("About", "012", "a@b.c", "Street", null, emptyList())

        val json = ApiJson.encodeToString(BusinessInfoBody.serializer(), body)

        assertEquals("""{"description":"About","phone":"012","email":"a@b.c","address":"Street","mapsUrl":null,"hours":[]}""", json)
    }

    @Test
    fun `every page is read until totalItems`() =
        runTest {
            val pages = listOf(PageDto(listOf(1, 2), 0, 2, 3), PageDto(listOf(3), 1, 2, 3))
            val requested = mutableListOf<Int>()

            val items = allPages { page -> pages[page].also { requested += page } }

            assertEquals(listOf(1, 2, 3), items)
            assertEquals(listOf(0, 1), requested)
        }
}
