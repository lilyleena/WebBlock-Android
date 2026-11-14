package com.kshrd.features.remote.dto

import kotlinx.serialization.Serializable

// Provisioning v2 (`/api/provisioning/v2`) and Payment v2 (`/api/payment/v2`) wire types, contracts dev bdb5390.

@Serializable
data class SiteStatusDto(
    val websiteId: String? = null,
    val online: Boolean = false,
    /** Last 30 days, a decimal string ("99.98"); `null` without a check (G2). */
    val uptimePercent: String? = null,
    val checkedAt: String? = null,
)

@Serializable
data class DeployStatsDto(
    val total: Int = 0,
    val succeeded: Int = 0,
    val failed: Int = 0,
)

@Serializable
data class PersonDto(
    val fullName: String,
)

@Serializable
data class ReleaseDto(
    val id: String,
    val number: Int,
    val startedBy: PersonDto? = null,
    val startedAt: String,
    val finishedAt: String? = null,
    val durationSeconds: Int? = null,
    /** `SUCCEEDED`, `FAILED`; `null` while running. */
    val status: String? = null,
    /** `LIVE`, `REPLACED`, `FAILED`, … */
    val state: String? = null,
    val promotedAt: String? = null,
)

@Serializable
data class UsageMetricDto(
    /** `websites`, `deploys`, `redeploys`, `generations`, `products`, and `photos` once Payment sends it. */
    val key: String,
    val label: String? = null,
    val used: Int? = null,
    /** `null` = unlimited. */
    val limit: Int? = null,
)

@Serializable
data class PlanUsageDto(
    val plan: String? = null,
    val resetsAt: String? = null,
    val metrics: List<UsageMetricDto> = emptyList(),
)
