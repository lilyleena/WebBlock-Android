package com.kshrd.features.remote.dto

import kotlinx.serialization.Serializable

// Notification v2 (`/api/notification/v2`) wire types, contracts dev bdb5390.

@Serializable
data class NotificationDto(
    /** A Knock id, not a UUID. */
    val id: String,
    val title: String,
    val message: String,
    /** `SUCCESS`, `WARNING`, `INFO`. */
    val kind: String? = null,
    val websiteId: String? = null,
    val createdAt: String,
    val read: Boolean = false,
    /** G5. */
    val detail: String? = null,
    /** G5; absent on a server older than Notification's G5 deploy. */
    val target: NotificationTargetDto? = null,
)

@Serializable
data class NotificationTargetDto(
    val websiteId: String? = null,
    /** `MONITOR`, `PRODUCTS`, `BUSINESS_INFO`, or `null` (no button). */
    val tab: String? = null,
    val workflow: String? = null,
)

/** `PATCH /me/notifications` and `/me/notifications/{id}`: only `{"read": true}`. */
@Serializable
data class ReadUpdate(
    val read: Boolean = true,
)
