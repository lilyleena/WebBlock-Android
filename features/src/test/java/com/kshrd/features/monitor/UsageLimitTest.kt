package com.kshrd.features.monitor

import com.kshrd.features.monitor.domain.model.UsageLimit
import com.kshrd.features.monitor.domain.model.usedFraction
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageLimitTest {
    @Test
    fun `a limited meter fills its bar, capped at full`() {
        assertEquals(0.5f, usedFraction(15, UsageLimit.Limited(30)))
        assertEquals(1f, usedFraction(40, UsageLimit.Limited(30)))
    }

    @Test
    fun `unlimited, not sent, unknown and zero limits leave the bar empty`() {
        assertEquals(0f, usedFraction(12, UsageLimit.Unlimited))
        assertEquals(0f, usedFraction(12, null))
        assertEquals(0f, usedFraction(12, UsageLimit.Limited(30), isUsedUnknown = true))
        assertEquals(0f, usedFraction(0, UsageLimit.Limited(0)))
    }
}
