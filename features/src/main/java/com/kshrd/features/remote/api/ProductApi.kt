package com.kshrd.features.remote.api

import com.kshrd.features.remote.dto.CategoryRequest
import com.kshrd.features.remote.dto.MediaAssetDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.ProductDto
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Project v2 products, categories and photos (docs/API-MAPPING.md 2.4). Product bodies are a `JsonObject`
 * (`productBody()`), because `imageUrl` has three meanings: absent keeps the photo, `null` removes it, a URL sets it.
 */
interface ProductApi {
    @GET("api/project/v2/projects/{projectId}/products")
    suspend fun products(
        @Path("projectId") projectId: String,
        @Query("page") page: Int,
        @Query("size") size: Int = ProjectApi.PROJECT_PAGE_SIZE,
        @Query("sort") sort: String = "createdAt,desc",
    ): PageDto<ProductDto>

    /** A bare `string[]`, in the owner's order. */
    @GET("api/project/v2/projects/{projectId}/product-categories")
    suspend fun categories(
        @Path("projectId") projectId: String,
    ): List<String>

    /** 409 `CATEGORY_NAME_TAKEN` means it already exists, which is fine. */
    @POST("api/project/v2/projects/{projectId}/product-categories")
    suspend fun addCategory(
        @Path("projectId") projectId: String,
        @Body body: CategoryRequest,
    )

    @POST("api/project/v2/projects/{projectId}/products")
    suspend fun createProduct(
        @Path("projectId") projectId: String,
        @Body body: JsonObject,
    ): ProductDto

    /** 409: changed elsewhere since it was loaded. */
    @PUT("api/project/v2/projects/{projectId}/products/{productId}")
    suspend fun updateProduct(
        @Path("projectId") projectId: String,
        @Path("productId") productId: String,
        @Body body: JsonObject,
    ): ProductDto

    @DELETE("api/project/v2/projects/{projectId}/products/{productId}")
    suspend fun deleteProduct(
        @Path("projectId") projectId: String,
        @Path("productId") productId: String,
    ): ProductDto

    /** The website's image library (every upload, used or not). */
    @GET("api/project/v2/projects/{projectId}/media")
    suspend fun media(
        @Path("projectId") projectId: String,
        @Query("page") page: Int,
        @Query("size") size: Int = ProjectApi.PROJECT_PAGE_SIZE,
    ): PageDto<MediaAssetDto>

    /** Deletes an image nothing uses: 204, 409 `MEDIA_IN_USE` while a product, the logo, a page or a deploy uses it. */
    @DELETE("api/project/v2/projects/{projectId}/media/{mediaId}")
    suspend fun deleteMedia(
        @Path("projectId") projectId: String,
        @Path("mediaId") mediaId: String,
    )

    /** JPEG or PNG, 2 MiB at most. 422 `STORAGE_LIMIT_REACHED` or `PHOTO_LIMIT_REACHED` when over the plan's limit. */
    @Multipart
    @POST("api/project/v2/projects/{projectId}/media")
    suspend fun uploadMedia(
        @Path("projectId") projectId: String,
        @Part file: MultipartBody.Part,
    ): MediaAssetDto
}
