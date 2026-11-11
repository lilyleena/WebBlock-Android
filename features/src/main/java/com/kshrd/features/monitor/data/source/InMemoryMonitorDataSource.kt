package com.kshrd.features.monitor.data.source

import com.kshrd.core.session.SessionStore
import com.kshrd.features.monitor.domain.model.Deploy
import com.kshrd.features.monitor.domain.model.LogCategory
import com.kshrd.features.monitor.domain.model.LogEntry
import com.kshrd.features.monitor.domain.model.LogOutcome
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.model.Plan
import com.kshrd.features.monitor.domain.model.SiteHealth
import com.kshrd.features.monitor.domain.model.UsageKind
import com.kshrd.features.monitor.domain.model.UsageMeter
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors the iOS sample monitor data: the same deploys and activity for every website. As on iOS,
 * the plan follows the signed-in email (starts with "ultra" or "pro"; anything else is Plus).
 */
@Singleton
class InMemoryMonitorDataSource
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        @Suppress("UNUSED_PARAMETER")
        suspend fun monitorFor(websiteId: String): MonitorData {
            delay(LATENCY_MS)
            val plan = planFor(session.owner.value?.email.orEmpty())
            val deploys =
                listOf(
                    deploy(10, at(10, 18, 14, 15), 1 * SECONDS_IN_MINUTE + 41, true, "2 days (current)"),
                    deploy(9, at(10, 15, 10, 2), 1 * SECONDS_IN_MINUTE + 38, true, "3 days"),
                    deploy(8, at(10, 11, 16, 47), 1 * SECONDS_IN_MINUTE + 9, false, null),
                    deploy(7, at(10, 8, 9, 20), 1 * SECONDS_IN_MINUTE + 44, true, "7 days"),
                    deploy(6, at(10, 2, 10, 24), 1 * SECONDS_IN_MINUTE + 52, true, "6 days"),
                    deploy(5, at(9, 28, 16, 10), 1 * SECONDS_IN_MINUTE + 47, false, null),
                    deploy(4, at(9, 21, 9, 2), 1 * SECONDS_IN_MINUTE + 12, true, "11 days"),
                )
            val logs =
                listOf(
                    LogEntry("l1", "Deploy 10 succeeded", LogCategory.Deploys, at(10, 18, 14, 15), LogOutcome.Success),
                    LogEntry("l2", "Product “Lily bunch” added", LogCategory.Products, at(10, 17, 11, 40), LogOutcome.Info),
                    LogEntry("l3", "Deploy 9 succeeded", LogCategory.Deploys, at(10, 15, 10, 2), LogOutcome.Success),
                    LogEntry("l4", "Opening hours updated", LogCategory.Business, at(10, 14, 9, 5), LogOutcome.Info),
                    LogEntry(
                        "l5",
                        "Deploy 8 failed",
                        LogCategory.Deploys,
                        at(10, 11, 16, 48),
                        LogOutcome.Failed,
                        detail = "Image “spring-tulips.jpg” is over 10 MB",
                    ),
                    LogEntry("l6", "Product “Spring tulip bunch” added", LogCategory.Products, at(10, 11, 16, 30), LogOutcome.Info),
                    LogEntry("l7", "Deploy 7 succeeded", LogCategory.Deploys, at(10, 8, 9, 22), LogOutcome.Success),
                )
            return MonitorData(
                health = SiteHealth.Online,
                plan = plan,
                uptimePercent = 99.98,
                deploys = deploys,
                usage =
                    listOf(
                        UsageMeter(UsageKind.Products, 26, plan.maxProducts),
                        UsageMeter(UsageKind.Photos, 41, plan.maxPhotos),
                    ),
                logs = logs,
                lastDeployAgo = "2 days ago",
                lastDeployDetail = "Deploy 10 · 18 Oct, 2:15 PM",
                redeploysUsed = 5,
                websitesUsed = 1,
                resetsOn = "1 Nov",
                succeededTotal = 8,
                failedTotal = 2,
            )
        }

        private fun planFor(email: String): Plan {
            val name = email.lowercase()
            return when {
                name.startsWith("ultra") -> Plan("Ultra", logsEnabled = true, maxProducts = 1000, maxPhotos = 2000, maxRedeploys = 100, maxWebsites = 10)
                name.startsWith("pro") -> Plan("Pro", logsEnabled = true, maxProducts = 200, maxPhotos = 400, maxRedeploys = 30, maxWebsites = 3)
                else -> Plan("Plus", logsEnabled = false, maxProducts = 30, maxPhotos = 60, maxRedeploys = 5, maxWebsites = 1)
            }
        }

        private fun deploy(
            number: Int,
            atMillis: Long,
            seconds: Int,
            succeeded: Boolean,
            liveFor: String?,
        ) = Deploy(number, atMillis, seconds, succeeded, startedBy = "Maya Sok", liveFor = liveFor)

        private fun at(
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
        ): Long = LocalDateTime.of(SAMPLE_YEAR, month, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        private companion object {
            const val LATENCY_MS = 450L
            const val SAMPLE_YEAR = 2026
            const val SECONDS_IN_MINUTE = 60
        }
    }
