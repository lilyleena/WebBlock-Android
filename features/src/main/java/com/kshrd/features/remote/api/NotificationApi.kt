package com.kshrd.features.remote.api

import com.kshrd.features.remote.dto.NotificationDto
import com.kshrd.features.remote.dto.PageDto
import com.kshrd.features.remote.dto.ReadUpdate
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/** Notification v2, the bell (docs/API-MAPPING.md 2.3). */
interface NotificationApi {
    @GET("api/notification/v2/me/notifications")
    suspend fun notifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = BELL_SIZE,
    ): PageDto<NotificationDto>

    /** Marks every notification read: 204. */
    @PATCH("api/notification/v2/me/notifications")
    suspend fun markAllRead(
        @Body body: ReadUpdate = ReadUpdate(),
    )

    /** G6. 404 = not in the caller's feed. */
    @PATCH("api/notification/v2/me/notifications/{notificationId}")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
        @Body body: ReadUpdate = ReadUpdate(),
    ): NotificationDto

    companion object {
        const val BELL_SIZE = 50
    }
}
