package com.kshrd.features.remote

import com.kshrd.core.auth.RefreshTokenRequest
import com.kshrd.core.auth.SessionApi
import com.kshrd.core.auth.SessionManager
import com.kshrd.core.auth.StoredTokens
import com.kshrd.core.auth.TokenSetDto
import com.kshrd.core.auth.TokenStore
import com.kshrd.features.remote.api.NotificationApi
import com.kshrd.features.remote.api.PaymentApi
import com.kshrd.features.remote.api.ProductApi
import com.kshrd.features.remote.api.ProjectApi
import com.kshrd.features.remote.api.ProvisioningApi
import com.kshrd.features.remote.api.UserApi
import com.kshrd.features.remote.dto.ActivityEventDto
import com.kshrd.features.remote.dto.AppSignInRequest
import com.kshrd.features.remote.dto.BusinessInfoBody
import com.kshrd.features.remote.dto.BusinessInfoDto
import com.kshrd.features.remote.dto.CategoryRequest
import com.kshrd.features.remote.dto.CodeSentDto
import com.kshrd.features.remote.dto.DeployStatsDto
import com.kshrd.features.remote.dto.EmailRequest
import com.kshrd.features.remote.dto.MediaAssetDto
import com.kshrd.features.remote.dto.NewPasswordRequest
import com.kshrd.features.remote.dto.NotificationDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.PlanUsageDto
import com.kshrd.features.remote.dto.ProductDto
import com.kshrd.features.remote.dto.ProfileDto
import com.kshrd.features.remote.dto.ReadUpdate
import com.kshrd.features.remote.dto.ReleaseDto
import com.kshrd.features.remote.dto.SignInRequest
import com.kshrd.features.remote.dto.SiteStatusDto
import com.kshrd.features.remote.dto.VerificationDto
import com.kshrd.features.remote.dto.VerifyCodeRequest
import com.kshrd.features.remote.dto.WebsiteDto
import com.kshrd.features.remote.mapper.MonitorFormat
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** A non-2xx answer with a problem body, as Retrofit throws it. */
fun httpError(
    status: Int,
    type: String = "http-$status",
): HttpException =
    HttpException(
        Response.error<Any>(
            status,
            """{"type":"urn:webblock:problem:$type","title":"t","status":$status}""".toResponseBody(),
        ),
    )

fun <T> page(vararg items: T) = PageDto(items.toList(), 0, items.size, items.size.toLong())

fun tokenSet(roles: List<String> = listOf("USER")) =
    TokenSetDto("access", "refresh", 3600, 2_592_000, "Bearer", "u1", "owner@example.com", roles)

object FixedFormat : MonitorFormat {
    override fun relative(epochMillis: Long) = "ago"

    override fun deployDetail(
        number: Int,
        epochMillis: Long,
    ) = "Deploy $number"

    override fun dayMonth(epochMillis: Long) = "day"
}

class MemoryTokenStore : TokenStore {
    var tokens: StoredTokens? = null

    override fun load() = tokens

    override fun save(tokens: StoredTokens) {
        this.tokens = tokens
    }

    override fun clear() {
        tokens = null
    }
}

class NoRefreshApi : SessionApi {
    override suspend fun refresh(body: RefreshTokenRequest): TokenSetDto = throw httpError(401, "session-expired")
}

fun sessionManager() = SessionManager(MemoryTokenStore(), NoRefreshApi())

class FakeUserApi : UserApi {
    val calls = mutableListOf<String>()
    var signInAnswer: () -> TokenSetDto = { tokenSet() }
    var profile = ProfileDto("u1", "Sok Dara", "owner@example.com", plan = "PRO")
    var lastNewPassword: NewPasswordRequest? = null

    override suspend fun signIn(body: SignInRequest): TokenSetDto {
        calls += "signIn stay=${body.stayLoggedIn}"
        return signInAnswer()
    }

    var googleRequest: AppSignInRequest? = null
    var googleAnswer: () -> TokenSetDto = { throw httpError(404, "account-not-found") }

    override suspend fun signInWithGoogle(body: AppSignInRequest): TokenSetDto {
        googleRequest = body
        return googleAnswer()
    }

    override suspend fun signOut(body: RefreshTokenRequest) {
        calls += "signOut ${body.refreshToken}"
    }

    override suspend fun me(): ProfileDto = profile.also { calls += "me" }

    override suspend fun requestPasswordReset(body: EmailRequest) = CodeSentDto(600, 60)

    override suspend fun verifyResetCode(body: VerifyCodeRequest) = VerificationDto("token-for-${body.email}", 600)

    override suspend fun confirmPasswordReset(body: NewPasswordRequest) {
        lastNewPassword = body
    }
}

class FakeProjectApi : ProjectApi {
    val websites = mutableListOf<WebsiteDto>()
    var business = BusinessInfoDto(description = "About", phone = "012", email = "a@b.c", address = "Street", logoUrl = null)
    var savedBody: BusinessInfoBody? = null
    var activityCalls = 0

    override suspend fun websites(
        page: Int,
        size: Int,
    ) = PageDto(if (page == 0) websites.toList() else emptyList(), page, size, websites.size.toLong())

    override suspend fun website(projectId: String) = websites.first { it.id == projectId }

    override suspend fun businessInfo(projectId: String) = business

    override suspend fun saveBusinessInfo(
        projectId: String,
        body: BusinessInfoBody,
    ): BusinessInfoDto {
        savedBody = body
        return business
    }

    override suspend fun activity(
        projectId: String,
        size: Int,
    ): PageDto<ActivityEventDto> {
        activityCalls++
        return page(ActivityEventDto("a1", "PRODUCT", "CHANGED", "Product added", null, "2026-11-12T10:00:00Z"))
    }
}

class FakeProductApi : ProductApi {
    val products = mutableListOf<ProductDto>()
    val media = mutableListOf<MediaAssetDto>()
    val deletedMedia = mutableListOf<String>()
    var created: JsonObject? = null
    var updated: JsonObject? = null
    var updateError: HttpException? = null
    var categoryError: HttpException? = null

    override suspend fun products(
        projectId: String,
        page: Int,
        size: Int,
        sort: String,
    ) = PageDto(if (page == 0) products.toList() else emptyList(), page, size, products.size.toLong())

    override suspend fun categories(projectId: String) = listOf("Flowers")

    override suspend fun addCategory(
        projectId: String,
        body: CategoryRequest,
    ) {
        categoryError?.let { throw it }
    }

    override suspend fun createProduct(
        projectId: String,
        body: JsonObject,
    ): ProductDto {
        created = body
        return ProductDto("new", "Tulips", version = 0)
    }

    override suspend fun updateProduct(
        projectId: String,
        productId: String,
        body: JsonObject,
    ): ProductDto {
        updateError?.let { throw it }
        updated = body
        return products.first { it.id == productId }.copy(imageUrl = body["imageUrl"]?.toString()?.trim('"'))
    }

    override suspend fun deleteProduct(
        projectId: String,
        productId: String,
    ) = products.first { it.id == productId }

    override suspend fun media(
        projectId: String,
        page: Int,
        size: Int,
    ) = PageDto(if (page == 0) media.toList() else emptyList(), page, size, media.size.toLong())

    override suspend fun deleteMedia(
        projectId: String,
        mediaId: String,
    ) {
        deletedMedia += mediaId
    }

    override suspend fun uploadMedia(
        projectId: String,
        file: MultipartBody.Part,
    ) = MediaAssetDto("m-new", "https://cdn/new.jpg", "photo.jpg")
}

class FakeProvisioningApi : ProvisioningApi {
    val status = mutableMapOf<String, () -> SiteStatusDto>()

    override suspend fun status(projectId: String): SiteStatusDto = (status[projectId] ?: { throw httpError(404) })()

    override suspend fun releaseStats(projectId: String) = DeployStatsDto(0, 0, 0)

    override suspend fun releases(
        projectId: String,
        size: Int,
    ): PageDto<ReleaseDto> = page()
}

class FakePaymentApi : PaymentApi {
    var usage: () -> PlanUsageDto = { PlanUsageDto() }

    override suspend fun usage(projectId: String) = usage()
}

class FakeNotificationApi : NotificationApi {
    val items = mutableListOf<NotificationDto>()
    var markFails = false
    var markAllFails = false
    var markAttempts = 0

    /** When set, markRead waits for it (to test a call cancelled while on its way). */
    var markGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    val marked = mutableListOf<String>()

    override suspend fun notifications(
        page: Int,
        size: Int,
    ) = PageDto(items.toList(), 0, size, items.size.toLong())

    override suspend fun markAllRead(body: ReadUpdate) {
        if (markAllFails) throw httpError(503)
        marked += "all"
    }

    override suspend fun markRead(
        notificationId: String,
        body: ReadUpdate,
    ): NotificationDto {
        markAttempts++
        markGate?.await()
        if (markFails) throw httpError(503)
        marked += notificationId
        return items.first { it.id == notificationId }.copy(read = true)
    }
}
