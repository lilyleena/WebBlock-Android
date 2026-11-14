package com.kshrd.features.remote.dto

import kotlinx.serialization.Serializable

// Project v2 (`/api/project/v2`) wire types, contracts dev bdb5390.

@Serializable
data class WebsiteDto(
    val id: String,
    val name: String,
    val businessType: String? = null,
    /** `LIVE`, `DRAFT`, `SUSPENDED`. */
    val status: String,
    val domain: String? = null,
    val thumbnailUrl: String? = null,
    /** The business logo (G4). */
    val logoUrl: String? = null,
)

@Serializable
data class OpeningHourDto(
    /** `MONDAY` … `SUNDAY`. */
    val day: String,
    val open: Boolean,
    /** "HH:mm". */
    val opensAt: String,
    val closesAt: String,
)

@Serializable
data class BusinessInfoDto(
    val websiteId: String? = null,
    val description: String = "",
    val logoUrl: String? = null,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val mapsUrl: String? = null,
    val hours: List<OpeningHourDto> = emptyList(),
    val updatedAt: String? = null,
)

/** `PUT /business-info`. `logoUrl` is left out, so the logo is kept (the app doesn't edit it). */
@Serializable
data class BusinessInfoBody(
    val description: String,
    val phone: String,
    val email: String,
    val address: String,
    /** Sent as `null` when empty. */
    val mapsUrl: String?,
    val hours: List<OpeningHourDto>,
)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "",
    /** Decimal string ("6.00"); `null` is a hidden price (G7). */
    val price: String? = null,
    val imageUrl: String? = null,
    val version: Long? = null,
)

@Serializable
data class CategoryRequest(
    val name: String,
)

@Serializable
data class MediaAssetDto(
    val id: String,
    val url: String,
    val name: String? = null,
)

@Serializable
data class ActivityEventDto(
    val id: String,
    /** `DEPLOY`, `PRODUCT`, `BUSINESS_INFO`. */
    val type: String,
    /** `SUCCEEDED`, `FAILED`, `CHANGED`. */
    val outcome: String? = null,
    val title: String,
    val detail: String? = null,
    val occurredAt: String,
)
