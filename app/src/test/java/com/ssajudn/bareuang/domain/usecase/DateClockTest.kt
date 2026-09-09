package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.domain.utils.DateUtils
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DateClockTest {
    private val jakartaClock = Clock.fixed(
        Instant.parse("2024-02-29T18:00:00Z"),
        ZoneId.of("Asia/Jakarta"),
    )

    @Test
    fun `clock uses explicit jakarta date across utc boundary`() {
        assertEquals("2024-03-01", DateUtils.getCurrentDateISO(jakartaClock))
        assertEquals("2024-03", DateUtils.getCurrentMonthYear(jakartaClock))
    }

    @Test
    fun `goal date calculations accept fixed reference date`() {
        val goal = Goal(name = "Trip", targetAmount = 1_000L, currentAmount = 500L, targetDate = "2024-03-31")
        assertEquals(30, goal.daysLeftUntilTarget(LocalDate.of(2024, 3, 1)))
        assertEquals(500L to 16L, goal.suggestedSavingsPace(LocalDate.of(2024, 3, 1)))
    }

    @Test
    fun `clock handles year boundary and future past due dates`() {
        val newYearClock = Clock.fixed(
            Instant.parse("2025-01-01T00:00:00Z"),
            ZoneId.of("Asia/Jakarta"),
        )
        assertEquals("2025-01", DateUtils.getCurrentMonthYear(newYearClock))
        assertEquals(2L, DateUtils.getDaysUntilDue("2025-01-03", newYearClock))
        assertEquals(-1L, DateUtils.getDaysUntilDue("2024-12-31", newYearClock))
    }
}
