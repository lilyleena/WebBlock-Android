package com.kshrd.features.monitor.domain.model

import androidx.compose.runtime.Immutable

enum class SiteHealth { Online, Draft, Offline }

/**
 * One plan allowance, from Payment's `metrics[].limit` (decision D1): a number, or [Unlimited] when Payment sends
 * `limit: null` (an admin removed the limit). A meter Payment didn't send has no [UsageLimit] at all (`null`).
 */
sealed interface UsageLimit {
    @Immutable
    data class Limited(
        val value: Int,
    ) : UsageLimit

    data object Unlimited : UsageLimit
}

/**
 * What the owner's plan allows. Activity logs are shown to everyone in the iOS design.
 * A `null` limit means Payment didn't send that meter: the screen shows "/ —".
 */
@Immutable
data class Plan(
    val name: String,
    val logsEnabled: Boolean,
    val maxProducts: UsageLimit? = null,
    val maxRedeploys: UsageLimit? = null,
    val maxWebsites: UsageLimit? = null,
)

@Immutable
data class Deploy(
    val number: Int,
    val atMillis: Long,
    val durationSeconds: Int,
    val succeeded: Boolean,
    val startedBy: String = "",
    /** How long this deploy was live ("3 days"); null when it never went live. */
    val liveFor: String? = null,
)

enum class UsageKind { Products, Photos }

@Immutable
data class UsageMeter(
    val kind: UsageKind,
    val used: Int,
    /** `null`: Payment didn't send this meter's limit. */
    val limit: UsageLimit?,
    /** Payment's title for the meter; `null` keeps the app's own. */
    val label: String? = null,
    /** Payment could not count it (`used: null`): the screen shows "—" or no bar instead of 0. */
    val isUsedUnknown: Boolean = false,
) {
    /** Share of the limit used; 0 (an empty bar) when unlimited, unknown or without a limit. */
    val fraction: Float get() = usedFraction(used, limit, isUsedUnknown)
}

/** Share of [limit] used, for a bar: 0 when unlimited, not sent, unknown or a zero limit. */
fun usedFraction(
    used: Int,
    limit: UsageLimit?,
    isUsedUnknown: Boolean = false,
): Float =
    when {
        isUsedUnknown || limit !is UsageLimit.Limited || limit.value <= 0 -> 0f
        else -> (used.toFloat() / limit.value).coerceIn(0f, 1f)
    }

enum class LogCategory { Deploys, Products, Business }

enum class LogOutcome { Success, Failed, Info }

@Immutable
data class LogEntry(
    val id: String,
    val title: String,
    val category: LogCategory,
    val atMillis: Long,
    val outcome: LogOutcome,
    /** Error text shown when a failed entry is expanded. */
    val detail: String? = null,
)

@Immutable
data class MonitorData(
    val health: SiteHealth,
    val plan: Plan,
    /** Percent over the last 30 days; null when the site has never been live. */
    val uptimePercent: Double?,
    /** Newest first. */
    val deploys: List<Deploy>,
    val usage: List<UsageMeter>,
    val logs: List<LogEntry>,
    /** "2 days ago". */
    val lastDeployAgo: String = "",
    /** "Deploy 10 · 18 Oct, 2:15 PM". */
    val lastDeployDetail: String = "",
    val redeploysUsed: Int = 0,
    val websitesUsed: Int = 0,
    /** "1 Nov": when the plan allowance resets. */
    val resetsOn: String = "",
    /** Totals over the whole history, when more deploys exist than [deploys] lists. */
    val succeededTotal: Int? = null,
    val failedTotal: Int? = null,
    /** When the uptime was last checked (G2); `null` without a check. */
    val uptimeCheckedAtMillis: Long? = null,
    /** Payment's titles for the redeploys and websites meters; `null` keeps the app's own. */
    val redeploysLabel: String? = null,
    val websitesLabel: String? = null,
) {
    val lastDeploy: Deploy? get() = deploys.firstOrNull()
    val succeededCount: Int get() = succeededTotal ?: deploys.count { it.succeeded }
    val failedCount: Int get() = failedTotal ?: (deploys.size - deploys.count { it.succeeded })
    val totalDeploys: Int get() = succeededCount + failedCount

    /** Whole-number percent of deploys that succeeded; null with no deploys. */
    val successPercent: Int? get() = if (totalDeploys == 0) null else succeededCount * PERCENT / totalDeploys
    val failedPercent: Int? get() = successPercent?.let { PERCENT - it }

    /** Mean build time in seconds; null with no deploys. */
    val averageDurationSeconds: Int? get() = if (deploys.isEmpty()) null else deploys.sumOf { it.durationSeconds } / deploys.size

    /** The seven newest deploys, oldest first, for the dot timeline. */
    val recentTimeline: List<Deploy> get() = deploys.take(TIMELINE_SIZE).reversed()

    /** Result of deploy 1..total: listed deploys use their real result, older ones count as succeeded. */
    val activityResults: List<Boolean>
        get() = (1..totalDeploys.coerceAtLeast(1)).map { n -> deploys.firstOrNull { it.number == n }?.succeeded ?: true }

    private companion object {
        const val PERCENT = 100
        const val TIMELINE_SIZE = 7
    }
}
