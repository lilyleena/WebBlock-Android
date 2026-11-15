package com.kshrd.features.remote

import com.kshrd.core.api.ApiMessages
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.SessionOwner
import com.kshrd.core.session.SessionStore
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.remote.dto.MediaAssetDto
import com.kshrd.features.remote.dto.NotificationDto
import com.kshrd.features.remote.dto.ProductDto
import com.kshrd.features.remote.dto.SiteStatusDto
import com.kshrd.features.remote.dto.WebsiteDto
import com.kshrd.features.remote.repository.ApiAuthRepository
import com.kshrd.features.remote.repository.ApiBusinessRepository
import com.kshrd.features.remote.repository.ApiMonitorRepository
import com.kshrd.features.remote.repository.ApiNotificationsRepository
import com.kshrd.features.remote.repository.ApiProductRepository
import com.kshrd.features.remote.repository.ApiWebsiteRepository
import com.kshrd.features.remote.repository.EncodedPhoto
import com.kshrd.features.remote.repository.PhotoEncoder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.DayOfWeek

class RepositoryTest {
    // Auth

    @Test
    fun `sign-in asks for the long session and loads the owner with the plan`() =
        runTest {
            val api = FakeUserApi()
            val session = sessionManager()

            val result = ApiAuthRepository(api, session).signIn("owner@example.com", "Secret123").first()

            assertEquals(DataState.Success(SessionOwner("u1", "Sok Dara", "owner@example.com", "PRO")), result)
            assertEquals(listOf("signIn stay=true", "me"), api.calls)
            assertTrue(session.hasSession())
        }

    @Test
    fun `an admin-only account is signed out and told the app is for owners`() =
        runTest {
            val api = FakeUserApi().apply { signInAnswer = { tokenSet(roles = listOf("ADMIN")) } }
            val session = sessionManager()

            val result = ApiAuthRepository(api, session).signIn("admin@example.com", "Secret123").first()

            assertEquals("This app is for website owners. Admins use the WebBlock website.", (result as DataState.Error).message)
            assertFalse(session.hasSession())
            assertTrue("signOut refresh" in api.calls)
        }

    @Test
    fun `wrong credentials show the owner's sentence`() =
        runTest {
            val api = FakeUserApi().apply { signInAnswer = { throw httpError(401, "invalid-credentials") } }

            val result = ApiAuthRepository(api, sessionManager()).signIn("owner@example.com", "nope").first()

            assertEquals(401, (result as DataState.Error).code)
            assertEquals("The email or password is wrong.", result.message)
        }

    @Test
    fun `Google sends the code, the verifier and the redirect, and starts the session`() =
        runTest {
            val api = FakeUserApi().apply { googleAnswer = { tokenSet() } }
            val session = sessionManager()

            val result = ApiAuthRepository(api, session).completeGoogleSignIn("code-1", "verifier-1").first()

            assertTrue(result is DataState.Success)
            assertEquals("com.webblock.app:/oauth2redirect", api.googleRequest?.redirectUri)
            assertEquals("verifier-1", api.googleRequest?.codeVerifier)
            assertTrue(session.hasSession())
        }

    @Test
    fun `an unknown Google identity is told to create the account on the website`() =
        runTest {
            val result = ApiAuthRepository(FakeUserApi(), sessionManager()).completeGoogleSignIn("code-1", "verifier-1").first()

            assertEquals(404, (result as DataState.Error).code)
            assertEquals("Create your account on the WebBlock website first, then sign in here.", result.message)
        }

    @Test
    fun `the reset confirms with the token from the verification`() =
        runTest {
            val api = FakeUserApi()
            val repository = ApiAuthRepository(api, sessionManager())

            repository.verifyResetCode("owner@example.com", "123456").first()
            val result = repository.resetPassword("owner@example.com", "NewSecret1").first()

            assertEquals(DataState.Success(Unit), result)
            assertEquals("token-for-owner@example.com", api.lastNewPassword?.verificationToken)
            assertEquals("NewSecret1", api.lastNewPassword?.confirmPassword)
        }

    @Test
    fun `a reset without a verification starts again`() =
        runTest {
            val result = ApiAuthRepository(FakeUserApi(), sessionManager()).resetPassword("owner@example.com", "NewSecret1").first()

            assertEquals("The reset has expired. Start again.", (result as DataState.Error).message)
        }

    @Test
    fun `sign-out revokes the refresh token and forgets the session`() =
        runTest {
            val api = FakeUserApi()
            val session = sessionManager()
            val repository = ApiAuthRepository(api, session)
            repository.signIn("owner@example.com", "Secret123").first()

            repository.signOut()

            assertTrue("signOut refresh" in api.calls)
            assertFalse(session.hasSession())
        }

    // Websites

    @Test
    fun `a live site with hosting offline gets the badge, and never deployed counts as offline`() =
        runTest {
            val project = FakeProjectApi()
            project.websites += WebsiteDto("w1", "Petal", status = "LIVE", logoUrl = "https://cdn/logo.png")
            project.websites += WebsiteDto("w2", "Bean", status = "LIVE")
            project.websites += WebsiteDto("w3", "Draft", status = "DRAFT")
            val provisioning = FakeProvisioningApi()
            provisioning.status["w1"] = { SiteStatusDto(online = false) }

            val sites = (ApiWebsiteRepository(project, provisioning).getWebsites("ignored").first() as DataState.Success).data

            assertEquals(listOf(true, true, false), sites.map { it.isHostingOffline })
            assertEquals("https://cdn/logo.png", sites[0].logoUrl)
        }

    @Test
    fun `no answer from Provisioning is no badge, and a missing logo comes from business info`() =
        runTest {
            val project = FakeProjectApi()
            project.websites += WebsiteDto("w1", "Petal", status = "LIVE", logoUrl = null)
            project.business = project.business.copy(logoUrl = "https://cdn/biz-logo.png")
            val provisioning = FakeProvisioningApi()
            provisioning.status["w1"] = { throw IOException("offline") }

            val site = (ApiWebsiteRepository(project, provisioning).getWebsites("ignored").first() as DataState.Success).data.single()

            assertFalse(site.isHostingOffline)
            assertEquals("https://cdn/biz-logo.png", site.logoUrl)
        }

    // Products

    private val noPhotos =
        object : PhotoEncoder {
            override suspend fun encode(sourceUri: String) = EncodedPhoto(ByteArray(1), "image/jpeg", "photo.jpg")
        }

    private fun product(
        id: String,
        imageUrl: String = "",
        version: Long? = 3,
        hidden: Boolean = false,
    ) = Product(id, "Tulips", "Flowers", if (hidden) 0.0 else 6.0, "", imageUrl, version, hidden)

    @Test
    fun `a new product is created with POST, a hidden price as null`() =
        runTest {
            val api = FakeProductApi()
            val repository = ApiProductRepository(api, noPhotos)

            repository.saveProduct("w1", product("local-id", version = null, hidden = true)).toList()

            assertEquals(JsonNull, api.created?.get("price"))
            assertNull(api.created?.get("version"))
            assertFalse("imageUrl" in api.created!!)
        }

    @Test
    fun `an edit keeps the photo with PUT and its version`() =
        runTest {
            val api = FakeProductApi()
            api.products += ProductDto("p1", "Tulips", category = "Flowers", price = "6.00", imageUrl = "https://cdn/a.jpg", version = 3)
            val repository = ApiProductRepository(api, noPhotos)
            repository.observeCatalog("w1").first()

            repository.saveProduct("w1", product("p1", imageUrl = "https://cdn/a.jpg")).toList()

            assertEquals(JsonPrimitive(3L), api.updated?.get("version"))
            assertFalse("imageUrl" in api.updated!!)
            assertTrue(api.deletedMedia.isEmpty())
        }

    @Test
    fun `a replaced photo is deleted from the library afterwards`() =
        runTest {
            val api = FakeProductApi()
            api.products += ProductDto("p1", "Tulips", category = "Flowers", price = "6.00", imageUrl = "https://cdn/old.jpg", version = 3)
            api.media += MediaAssetDto("m-old", "https://cdn/old.jpg")
            val repository = ApiProductRepository(api, noPhotos)
            repository.observeCatalog("w1").first()

            repository.saveProduct("w1", product("p1", imageUrl = "https://cdn/new.jpg")).toList()

            assertEquals(JsonPrimitive("https://cdn/new.jpg"), api.updated?.get("imageUrl"))
            assertEquals(listOf("m-old"), api.deletedMedia)
        }

    @Test
    fun `a removed photo is sent as null`() =
        runTest {
            val api = FakeProductApi()
            api.products += ProductDto("p1", "Tulips", category = "Flowers", price = "6.00", imageUrl = "https://cdn/old.jpg", version = 3)
            val repository = ApiProductRepository(api, noPhotos)
            repository.observeCatalog("w1").first()

            repository.saveProduct("w1", product("p1", imageUrl = "")).toList()

            assertEquals(JsonNull, api.updated?.get("imageUrl"))
        }

    @Test
    fun `a 409 on save says the product changed somewhere else`() =
        runTest {
            val api = FakeProductApi()
            api.products += ProductDto("p1", "Tulips", version = 3)
            api.updateError = httpError(409, "product-version-conflict")
            val repository = ApiProductRepository(api, noPhotos)
            repository.observeCatalog("w1").first()

            val result = repository.saveProduct("w1", product("p1")).toList().last()

            assertEquals(ApiMessages.CHANGED_ELSEWHERE, (result as DataState.Error).message)
        }

    @Test
    fun `an existing category is fine`() =
        runTest {
            val api = FakeProductApi().apply { categoryError = httpError(409, "category-name-taken") }

            val result = ApiProductRepository(api, noPhotos).addCategory("w1", "Flowers").first()

            assertEquals(DataState.Success("Flowers"), result)
        }

    @Test
    fun `the storage limit has its own message`() =
        runTest {
            val full =
                object : PhotoEncoder {
                    override suspend fun encode(sourceUri: String): EncodedPhoto = throw httpError(422, "storage-limit-reached")
                }

            val result = ApiProductRepository(FakeProductApi(), full).uploadPhoto("w1", "content://photo").first()

            assertTrue((result as DataState.Error).message!!.startsWith("Your website's photo storage is full"))
        }

    // Business

    @Test
    fun `saving the hours sends the current details with them`() =
        runTest {
            val project = FakeProjectApi()
            project.websites += WebsiteDto("w1", "Petal", status = "LIVE")
            val repository = ApiBusinessRepository(project)
            repository.observeBusiness("w1").first()

            repository.updateHours("w1", listOf(DayHours(DayOfWeek.MONDAY, true, 480, 1020))).first()

            assertEquals("About", project.savedBody?.description)
            assertEquals("08:00", project.savedBody?.hours?.first()?.opensAt)
            assertEquals(7, project.savedBody?.hours?.size)
        }

    // Monitor

    @Test
    fun `the activity log is asked for only on Pro and Ultra, and a failed part still shows the rest`() =
        runTest {
            val project = FakeProjectApi().apply { websites += WebsiteDto("w1", "Petal", status = "LIVE") }
            val payment = FakePaymentApi().apply { usage = { throw httpError(503) } }
            val store = SessionStore()
            store.signIn(SessionOwner("u1", "Sok Dara", "owner@example.com", plan = "PLUS"))

            val plus = ApiMonitorRepository(project, FakeProvisioningApi(), payment, store, FixedFormat).observeMonitor("w1").first()
            store.signIn(SessionOwner("u1", "Sok Dara", "owner@example.com", plan = "PRO"))
            val pro = ApiMonitorRepository(project, FakeProvisioningApi(), payment, store, FixedFormat).observeMonitor("w1").first()

            assertTrue(plus is DataState.Success)
            assertEquals(1, project.activityCalls)
            assertEquals(1, (pro as DataState.Success).data.logs.size)
        }

    // Notifications

    @Test
    fun `an item opened from the list and its detail is marked read with one PATCH, a read one with none`() =
        runTest {
            val api = FakeNotificationApi()
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            api.items += NotificationDto("k2", "Petal", "Old", websiteId = "w1", createdAt = "2026-11-13T03:00:00Z", read = true)
            val repository = ApiNotificationsRepository(api, FixedFormat)
            repository.observeNotifications().first()

            repository.markRead("k1")
            repository.markRead("k1")
            repository.markRead("k2")

            assertEquals(listOf("k1"), api.marked)
        }

    @Test
    fun `an item marked read stays read when a list loaded before the PATCH landed comes back`() =
        runTest {
            val api = FakeNotificationApi()
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            val repository = ApiNotificationsRepository(api, FixedFormat)
            repository.observeNotifications().first()

            repository.markRead("k1")
            // The server hasn't applied it yet (the fake keeps read=false): the detail screen's reload still sends it unread.
            val reloaded = (repository.observeNotifications().first() as DataState.Success).data

            assertFalse(reloaded.single().unread)
            assertEquals(0, repository.unreadCount.first())
        }

    @Test
    fun `mark all keeps every item read across a reload`() =
        runTest {
            val api = FakeNotificationApi()
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            api.items += NotificationDto("k2", "Petal", "Old", websiteId = "w1", createdAt = "2026-11-13T03:00:00Z", read = false)
            val repository = ApiNotificationsRepository(api, FixedFormat)
            repository.observeNotifications().first()

            repository.markAllRead()
            val reloaded = (repository.observeNotifications().first() as DataState.Success).data

            assertEquals(listOf(false, false), reloaded.map { it.unread })
        }

    @Test
    fun `after a failed PATCH a reload shows the server's unread state and opening it sends the PATCH again`() =
        runTest {
            val api = FakeNotificationApi().apply { markFails = true }
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            val repository = ApiNotificationsRepository(api, FixedFormat)
            repository.observeNotifications().first()

            repository.markRead("k1")
            val reloaded = (repository.observeNotifications().first() as DataState.Success).data
            repository.markRead("k1")

            assertTrue(reloaded.single().unread)
            assertEquals(2, api.markAttempts)
        }

    @Test
    fun `after a failed mark-all a reload shows the server's unread state`() =
        runTest {
            val api = FakeNotificationApi().apply { markAllFails = true }
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            api.items += NotificationDto("k2", "Petal", "Old", websiteId = "w1", createdAt = "2026-11-13T03:00:00Z", read = false)
            val repository = ApiNotificationsRepository(api, FixedFormat)
            repository.observeNotifications().first()

            repository.markAllRead()
            val reloaded = (repository.observeNotifications().first() as DataState.Success).data

            assertEquals(listOf(true, true), reloaded.map { it.unread })
        }

    @Test
    fun `opening a notification marks it read on screen even when the PATCH fails`() =
        runTest {
            val api = FakeNotificationApi().apply { markFails = true }
            api.items += NotificationDto("k1", "Petal", "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z", read = false)
            val repository = ApiNotificationsRepository(api, FixedFormat)

            assertEquals(1, repository.unreadCount.first())
            repository.markRead("k1")

            assertEquals(0, repository.unreadCount.first())
            val list = (repository.observeNotifications().first() as DataState.Success).data
            assertEquals("ago", list.single().timeText)
        }
}
