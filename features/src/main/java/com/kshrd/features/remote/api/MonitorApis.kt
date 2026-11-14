package com.kshrd.features.remote.api

import com.kshrd.features.remote.dto.DeployStatsDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.PlanUsageDto
import com.kshrd.features.remote.dto.ReleaseDto
import com.kshrd.features.remote.dto.SiteStatusDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Provisioning v2: hosting state, uptime and deploy history (docs/API-MAPPING.md 2.2, 2.6). */
interface ProvisioningApi {
    /** 404 `deployment-not-found`: never deployed (offline). */
    @GET("api/provisioning/v2/projects/{projectId}/status")
    suspend fun status(
        @Path("projectId") projectId: String,
    ): SiteStatusDto

    @GET("api/provisioning/v2/projects/{projectId}/releases/stats")
    suspend fun releaseStats(
        @Path("projectId") projectId: String,
    ): DeployStatsDto

    /** Finished releases only, newest first. */
    @GET("api/provisioning/v2/projects/{projectId}/releases")
    suspend fun releases(
        @Path("projectId") projectId: String,
        @Query("size") size: Int = ProjectApi.MONITOR_LIST_SIZE,
    ): PageDto<ReleaseDto>
}

/** Payment v2: the plan's usage, limits and labels (docs/API-MAPPING.md 2.6). */
interface PaymentApi {
    @GET("api/payment/v2/projects/{projectId}/usage")
    suspend fun usage(
        @Path("projectId") projectId: String,
    ): PlanUsageDto
}
