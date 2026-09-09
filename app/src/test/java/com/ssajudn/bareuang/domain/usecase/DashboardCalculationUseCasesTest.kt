package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.BudgetPeriod
import com.ssajudn.bareuang.domain.model.RunwayStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardCalculationUseCasesTest {
    private val period = BudgetPeriod(
        monthYear = "2026-02",
        todayIso = "2026-02-28",
        daysPassed = 28,
        daysInMonth = 28,
    )

    @Test
    fun `zero budget reports not set`() {
        val result = CalculateBudgetRunwayUseCase(0L, 0L, period)
        assertEquals(RunwayStatus.BudgetNotSet, result.status)
    }

    @Test
    fun `exhausted budget reports exhausted`() {
        val result = CalculateBudgetRunwayUseCase(100L, 100L, period)
        assertEquals(RunwayStatus.Exhausted, result.status)
        assertEquals(0L, result.remainingBudget)
    }

    @Test
    fun `no spending reports no spending`() {
        val result = CalculateBudgetRunwayUseCase(100L, 0L, period)
        assertEquals(RunwayStatus.NoSpending, result.status)
    }

    @Test
    fun `automatic pacing uses remaining days`() {
        val result = CalculateDailyPacingUseCase(
            1_000L,
            500L,
            100L,
            period.copy(daysPassed = 10),
            null,
        )
        assertEquals(26L, result.allowance)
        assertEquals(-74L, result.remaining)
        assertTrue(!result.isCustom)
    }

    @Test
    fun `custom pacing overrides automatic target and reports exceeded`() {
        val result = CalculateDailyPacingUseCase(1_000L, 500L, 600L, period, 500L)
        assertEquals(500L, result.allowance)
        assertEquals(-100L, result.remaining)
        assertTrue(result.isCustom)
        assertTrue(result.isExceeded)
    }

    @Test
    fun `runway supports months with different day counts`() = runTest {
        val january = period.copy(monthYear = "2026-01", daysPassed = 31, daysInMonth = 31)
        val february = period.copy(monthYear = "2026-02", daysPassed = 28, daysInMonth = 28)
        assertEquals(1, january.remainingDays)
        assertEquals(1, february.remainingDays)
    }
}
