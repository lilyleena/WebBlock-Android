package com.kshrd.features.business

import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.presentation.components.OpenStatus
import com.kshrd.features.business.presentation.components.openStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class OpenStatusTest {
    // Weekdays 9:00–18:00, weekend closed. 2026-11-11 is a Wednesday; 2026-11-14 a Saturday.
    private val hours = DayOfWeek.entries.map { DayHours(it, it.value <= 5, 9 * 60, 18 * 60) }

    private fun at(
        day: Int,
        hour: Int,
        minute: Int = 0,
    ) = LocalDateTime.of(2026, 11, day, hour, minute)

    @Test
    fun `open during the day, next change is closing time`() {
        assertEquals(OpenStatus(isOpen = true, nextChangeMinutes = 18 * 60), openStatus(hours, at(11, 12)))
    }

    @Test
    fun `before opening today, next change is today's opening`() {
        assertEquals(OpenStatus(isOpen = false, nextChangeMinutes = 9 * 60), openStatus(hours, at(11, 7, 30)))
    }

    @Test
    fun `closing time itself counts as closed`() {
        assertEquals(false, openStatus(hours, at(11, 18)).isOpen)
    }

    @Test
    fun `weekend points to the next open day`() {
        assertEquals(OpenStatus(isOpen = false, nextChangeMinutes = 9 * 60), openStatus(hours, at(14, 12)))
    }

    @Test
    fun `never open has no next change`() {
        val closed = DayOfWeek.entries.map { DayHours(it, false, 0, 0) }
        assertEquals(OpenStatus(isOpen = false, nextChangeMinutes = null), openStatus(closed, at(11, 12)))
    }
}
