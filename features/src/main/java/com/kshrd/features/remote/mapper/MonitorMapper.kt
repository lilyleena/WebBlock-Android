package com.kshrd.features.remote.mapper

import com.kshrd.features.monitor.domain.model.Deploy
import com.kshrd.features.monitor.domain.model.LogCategory
import com.kshrd.features.monitor.domain.model.LogEntry
import com.kshrd.features.monitor.domain.model.LogOutcome
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.model.Plan
import com.kshrd.features.monitor.domain.model.SiteHealth
import com.kshrd.features.monitor.domain.model.UsageKind
import com.kshrd.features.monitor.domain.model.UsageMeter
import com.kshrd.features.remote.dto.ActivityEventDto
import com.kshrd.features.remote.dto.DeployStatsDto
import com.kshrd.features.remote.dto.PlanUsageDto
import com.kshrd.features.remote.dto.ReleaseDto
import com.kshrd.features.remote.dto.SiteStatusDto
import com.kshrd.features.remote.dto.UsageMetricDto

private const val SUCCEEDED = "SUCCEEDED"
private const val MILLIS_PER_HOUR = 3_600_000L
private const val HOURS_PER_DAY = 24L

/** The answers the Monitor screen combines. Any part but the plan may be missing: that part shows "—" or nothing. */
data class MonitorParts(
    val plan: PlanTier,
    val status: SiteStatusDto? = null,
    val stats: DeployStatsDto? = null,
    /** Finished releases, newest first. */
    val releases: List<ReleaseDto> = emptyList(),
    val usage: PlanUsageDto? = null,
    val activity: List<ActivityEventDto> = emptyList(),
)

/** The date texts, from the device's locale (core-ui's formatters in the app, fixed text in tests). */
interface MonitorFormat {
    /** "2 days ago". */
    fun relative(epochMillis: Long): String

    /** "Deploy 10 · 18 Oct, 2:15 PM". */
    fun deployDetail(
        number: Int,
        epochMillis: Long,
    ): String

    /** "1 Nov". */
    fun dayMonth(epochMillis: Long): String
}

/**
 * docs/API-MAPPING.md 2.6. Used amounts, limits and labels come from Payment's `metrics[]` (a missing limit falls back
 * to [PlanTier]). The free trial has `deploys` instead of `redeploys`. The photos meter exists only with a `photos`
 * metric.
 */
fun MonitorParts.toMonitorData(
    nowMillis: Long,
    format: MonitorFormat,
): MonitorData {
    val metrics = usage?.metrics.orEmpty().associateBy { it.key }
    val products = metrics["products"]
    val redeploys = metrics["redeploys"] ?: metrics["deploys"]
    val websites = metrics["websites"]
    val photos = metrics["photos"]
    val uptime = status?.uptimePercent?.toDoubleOrNull()
    val latest = releases.firstOrNull()
    val latestAt = latest?.let { parseApiTime(it.finishedAt) ?: parseApiTime(it.startedAt) }
    return MonitorData(
        health = if (status?.online == true) SiteHealth.Online else SiteHealth.Offline,
        plan =
            Plan(
                name = plan.title,
                logsEnabled = plan.showsActivityLog,
                maxProducts = products?.limit ?: plan.products,
                maxPhotos = photos?.limit ?: 0,
                maxRedeploys = redeploys?.limit ?: plan.redeploys,
                maxWebsites = websites?.limit ?: plan.websites,
            ),
        uptimePercent = uptime,
        uptimeCheckedAtMillis = if (uptime == null) null else parseApiTime(status?.checkedAt),
        deploys = releases.mapIndexed { index, release -> release.toDeploy(liveFor(releases, index, nowMillis)) },
        usage = usageMeters(products, photos, plan),
        logs = activity.map { it.toLogEntry() },
        lastDeployAgo = latestAt?.let(format::relative) ?: "—",
        lastDeployDetail = if (latest != null && latestAt != null) format.deployDetail(latest.number, latestAt) else "",
        redeploysUsed = redeploys?.used ?: 0,
        websitesUsed = websites?.used ?: 0,
        resetsOn = parseApiTime(usage?.resetsAt)?.let(format::dayMonth) ?: "—",
        succeededTotal = stats?.succeeded,
        failedTotal = stats?.failed,
        redeploysLabel = redeploys?.label,
        websitesLabel = websites?.label,
    )
}

private fun usageMeters(
    products: UsageMetricDto?,
    photos: UsageMetricDto?,
    plan: PlanTier,
): List<UsageMeter> =
    listOfNotNull(
        UsageMeter(UsageKind.Products, used = products?.used ?: 0, limit = products?.limit ?: plan.products, label = products?.label),
        photos?.let { UsageMeter(UsageKind.Photos, used = it.used ?: 0, limit = it.limit ?: 0, label = it.label) },
    )

private fun ReleaseDto.toDeploy(liveFor: String?): Deploy =
    Deploy(
        number = number,
        atMillis = parseApiTime(startedAt) ?: 0L,
        durationSeconds = durationSeconds ?: 0,
        succeeded = status == SUCCEEDED,
        startedBy = startedBy?.fullName ?: "—",
        liveFor = liveFor,
    )

private fun ActivityEventDto.toLogEntry(): LogEntry =
    LogEntry(
        id = id,
        title = title,
        category =
            when (type) {
                "DEPLOY" -> LogCategory.Deploys
                "PRODUCT" -> LogCategory.Products
                else -> LogCategory.Business
            },
        atMillis = parseApiTime(occurredAt) ?: 0L,
        outcome =
            when (outcome) {
                "FAILED" -> LogOutcome.Failed
                SUCCEEDED -> LogOutcome.Success
                else -> LogOutcome.Info
            },
        detail = detail,
    )

/**
 * How long a release was live: from its promotion to the next newer successful release's, or until now with
 * "(current)" for the live one. `null` for a failed release (it never went live). [releases] is newest first.
 */
internal fun liveFor(
    releases: List<ReleaseDto>,
    index: Int,
    nowMillis: Long,
): String? {
    val release = releases[index]
    if (release.status != SUCCEEDED) return null
    val isLive = release.state == "LIVE"
    val start = liveSince(release)
    val end = if (isLive) nowMillis else releases.subList(0, index).lastOrNull { it.status == SUCCEEDED }?.let(::liveSince)
    return when {
        start == null || end == null -> "—"
        isLive -> "${span(start, end)} (current)"
        else -> span(start, end)
    }
}

private fun liveSince(release: ReleaseDto): Long? = parseApiTime(release.promotedAt) ?: parseApiTime(release.finishedAt)

/** "1 day", "3 days", "5 hours", "Under an hour". */
internal fun span(
    startMillis: Long,
    endMillis: Long,
): String {
    val hours = (endMillis - startMillis).coerceAtLeast(0L) / MILLIS_PER_HOUR
    val days = hours / HOURS_PER_DAY
    return when {
        days == 1L -> "1 day"
        days > 1L -> "$days days"
        hours == 1L -> "1 hour"
        hours > 1L -> "$hours hours"
        else -> "Under an hour"
    }
}
