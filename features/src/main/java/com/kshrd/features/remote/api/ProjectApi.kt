package com.kshrd.features.remote.api

import com.kshrd.features.remote.dto.ActivityEventDto
import com.kshrd.features.remote.dto.BusinessInfoBody
import com.kshrd.features.remote.dto.BusinessInfoDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.WebsiteDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Project v2 websites, business information and activity (docs/API-MAPPING.md 2.2, 2.5, 2.6). */
interface ProjectApi {
    @GET("api/project/v2/projects")
    suspend fun websites(
        @Query("page") page: Int,
        @Query("size") size: Int = PROJECT_PAGE_SIZE,
    ): PageDto<WebsiteDto>

    @GET("api/project/v2/projects/{projectId}")
    suspend fun website(
        @Path("projectId") projectId: String,
    ): WebsiteDto

    @GET("api/project/v2/projects/{projectId}/business-info")
    suspend fun businessInfo(
        @Path("projectId") projectId: String,
    ): BusinessInfoDto

    @PUT("api/project/v2/projects/{projectId}/business-info")
    suspend fun saveBusinessInfo(
        @Path("projectId") projectId: String,
        @Body body: BusinessInfoBody,
    ): BusinessInfoDto

    /** Pro and Ultra only: 403 on the other plans, which the app shows as an empty log. */
    @GET("api/project/v2/projects/{projectId}/activity")
    suspend fun activity(
        @Path("projectId") projectId: String,
        @Query("size") size: Int = MONITOR_LIST_SIZE,
    ): PageDto<ActivityEventDto>

    companion object {
        /** Project's largest page. */
        const val PROJECT_PAGE_SIZE = 200

        /** Monitor lists show the newest 20. */
        const val MONITOR_LIST_SIZE = 20
    }
}
