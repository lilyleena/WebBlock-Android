package com.kshrd.features.remote

import com.kshrd.features.monitor.domain.model.LogCategory
import com.kshrd.features.monitor.domain.model.LogOutcome
import com.kshrd.features.monitor.domain.model.SiteHealth
import com.kshrd.features.monitor.domain.model.UsageKind
import com.kshrd.features.monitor.domain.model.UsageLimit
import com.kshrd.features.remote.dto.ActivityEventDto
import com.kshrd.features.remote.dto.DeployStatsDto
import com.kshrd.features.remote.dto.PersonDto
import com.kshrd.features.remote.dto.PlanUsageDto
import com.kshrd.features.remote.dto.ReleaseDto
import com.kshrd.features.remote.dto.SiteStatusDto
import com.kshrd.features.remote.dto.UsageMetricDto
import com.kshrd.features.remote.mapper.MonitorFormat
import com.kshrd.features.remote.mapper.MonitorParts
import com.kshrd.features.remote.mapper.PlanTier
import com.kshrd.features.remote.mapper.parseApiTime
import com.kshrd.features.remote.mapper.toMonitorData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonitorMapperTest {
    private val now = checkNotNull(parseApiTime("2026-11-14T12:00:00Z"))

    private val format =
        object : MonitorFormat {
            override fun relative(epochMillis: Long) = "ago:$epochMillis"

            override fun deployDetail(
                number: Int,
                epochMillis: Long,
            ) = "Deploy $number"

            override fun dayMonth(epochMillis: Long) = "day:$epochMillis"
        }

    private val releases =
        listOf(
            release(3, "SUCCEEDED", "LIVE", promotedAt = "2026-11-12T12:00:00Z"),
            release(2, "FAILED", "FAILED", promotedAt = null),
            release(1, "SUCCEEDED", "REPLACED", promotedAt = "2026-11-11T12:00:00Z"),
        )

    @Test
    fun `limits and labels come from Payment, the photos meter only with a photos metric`() {
        val usage =
            PlanUsageDto(
                plan = "PLUS",
                resetsAt = "2026-12-01T00:00:00Z",
                metrics =
                    listOf(
                        UsageMetricDto("websites", "Draft websites", used = 2, limit = 20),
                        UsageMetricDto("products", "Products", used = 7, limit = 30),
                        UsageMetricDto("redeploys", "Redeploys", used = 1, limit = 5),
                    ),
            )

        val data = MonitorParts(PlanTier.Plus, usage = usage).toMonitorData(now, format)

        assertEquals(listOf(UsageKind.Products), data.usage.map { it.kind })
        assertEquals(UsageLimit.Limited(30), data.usage.single().limit)
        assertEquals(UsageLimit.Limited(20), data.plan.maxWebsites)
        assertEquals("Draft websites", data.websitesLabel)
        assertEquals(2, data.websitesUsed)
        assertEquals("day:${parseApiTime("2026-12-01T00:00:00Z")}", data.resetsOn)
    }

    @Test
    fun `a photos metric shows the meter with its own limit`() {
        val usage = PlanUsageDto(metrics = listOf(UsageMetricDto("photos", "Photos", used = 12, limit = 30)))

        val photos = MonitorParts(PlanTier.FreeTrial, usage = usage).toMonitorData(now, format).usage.single { it.kind == UsageKind.Photos }

        assertEquals(12, photos.used)
        assertEquals(UsageLimit.Limited(30), photos.limit)
        assertEquals("Photos", photos.label)
        assertEquals(false, photos.isUsedUnknown)
    }

    @Test
    fun `photos not counted yet are unknown, not zero`() {
        val metric = UsageMetricDto("photos", "Photos per website", cardLabel = "Photos", used = null, limit = 30)
        val usage = PlanUsageDto(metrics = listOf(metric))

        val photos = MonitorParts(PlanTier.Plus, usage = usage).toMonitorData(now, format).usage.single { it.kind == UsageKind.Photos }

        assertEquals(true, photos.isUsedUnknown)
        assertEquals(0f, photos.fraction)
        assertEquals(UsageLimit.Limited(30), photos.limit)
        assertEquals("Photos per website", photos.label)
    }

    @Test
    fun `the trial counts deploys, and a null limit is unlimited`() {
        val usage =
            PlanUsageDto(
                metrics =
                    listOf(
                        UsageMetricDto("deploys", "Deploys", used = 1, limit = 1),
                        UsageMetricDto("products", used = null, limit = null),
                    ),
            )

        val data = MonitorParts(PlanTier.FreeTrial, usage = usage).toMonitorData(now, format)

        assertEquals(1, data.redeploysUsed)
        assertEquals(UsageLimit.Limited(1), data.plan.maxRedeploys)
        assertEquals("Deploys", data.redeploysLabel)
        assertEquals(true, data.usage.single().isUsedUnknown)
        assertEquals(UsageLimit.Unlimited, data.usage.single().limit)
        assertEquals(UsageLimit.Unlimited, data.plan.maxProducts)
        assertEquals(0f, data.usage.single().fraction)
    }

    @Test
    fun `no usage at all shows dashes, not a crash`() {
        val data = MonitorParts(PlanTier.Pro).toMonitorData(now, format)

        assertEquals(SiteHealth.Offline, data.health)
        assertNull(data.uptimePercent)
        assertEquals("—", data.resetsOn)
        assertEquals("—", data.lastDeployAgo)
        // Payment sent nothing: no limit is invented ("/ —" on screen).
        assertNull(data.plan.maxProducts)
        assertNull(data.plan.maxRedeploys)
        assertNull(data.plan.maxWebsites)
        assertNull(data.usage.single().limit)
        assertEquals(true, data.usage.single().isUsedUnknown)
    }

    @Test
    fun `uptime is shown with its check time, and neither without a check`() {
        val checked = SiteStatusDto(online = true, uptimePercent = "99.98", checkedAt = "2026-11-14T11:55:00Z")
        val unchecked = SiteStatusDto(online = true, uptimePercent = null, checkedAt = "2026-11-14T11:55:00Z")

        val withUptime = MonitorParts(PlanTier.Plus, status = checked).toMonitorData(now, format)
        val withoutUptime = MonitorParts(PlanTier.Plus, status = unchecked).toMonitorData(now, format)

        assertEquals(SiteHealth.Online, withUptime.health)
        assertEquals(99.98, checkNotNull(withUptime.uptimePercent), 0.0)
        assertEquals(parseApiTime("2026-11-14T11:55:00Z"), withUptime.uptimeCheckedAtMillis)
        assertNull(withoutUptime.uptimePercent)
        assertNull(withoutUptime.uptimeCheckedAtMillis)
    }

    @Test
    fun `deploys say how long each was live`() {
        val data = MonitorParts(PlanTier.Plus, stats = DeployStatsDto(12, 10, 2), releases = releases).toMonitorData(now, format)

        assertEquals(listOf(3, 2, 1), data.deploys.map { it.number })
        assertEquals("2 days (current)", data.deploys[0].liveFor)
        assertNull(data.deploys[1].liveFor)
        assertEquals("1 day", data.deploys[2].liveFor)
        assertEquals("Maya Sok", data.deploys[0].startedBy)
        assertEquals(10, data.succeededCount)
        assertEquals(2, data.failedCount)
        assertEquals("Deploy 3", data.lastDeployDetail)
    }

    @Test
    fun `activity events become log entries`() {
        val activity =
            listOf(
                ActivityEventDto("a1", "DEPLOY", "FAILED", "Deploy 2 failed", "Build error", "2026-11-12T10:00:00Z"),
                ActivityEventDto("a2", "BUSINESS_INFO", "CHANGED", "Opening hours changed", null, "2026-11-12T11:00:00Z"),
            )

        val logs = MonitorParts(PlanTier.Pro, activity = activity).toMonitorData(now, format).logs

        assertEquals(listOf(LogCategory.Deploys, LogCategory.Business), logs.map { it.category })
        assertEquals(listOf(LogOutcome.Failed, LogOutcome.Info), logs.map { it.outcome })
        assertEquals("Build error", logs.first().detail)
    }

    private fun release(
        number: Int,
        status: String,
        state: String,
        promotedAt: String?,
    ) = ReleaseDto(
        id = "r$number",
        number = number,
        startedBy = PersonDto("Maya Sok"),
        startedAt = "2026-11-1${number}T11:58:00Z",
        finishedAt = "2026-11-1${number}T12:00:00Z",
        durationSeconds = 101,
        status = status,
        state = state,
        promotedAt = promotedAt,
    )
}
