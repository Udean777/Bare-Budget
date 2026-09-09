package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import com.ssajudn.bareuang.domain.repository.AnalyticsData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock

private class AnalyticsRepo(
    private val cashflow: Result<List<CashflowDataPoint>>
) : AnalyticsRepository {
    override suspend fun getAnalytics(clock: Clock) = Result.success(AnalyticsData(cashflow = cashflow.getOrNull().orEmpty(), netWorth = emptyList()))
    override suspend fun getCashflowAnalytics(clock: Clock) = cashflow
    override suspend fun getNetWorthAnalytics(clock: Clock) = Result.success(emptyList<NetWorthDataPoint>())
}

class GetCashflowAnalyticsUseCaseTest {
    @Test
    fun `cashflow delegates to repository projection`() = runTest {
        val repo = AnalyticsRepo(Result.success(listOf(CashflowDataPoint("2026-09", "Sep", 0L, 600L))))
        val result = GetCashflowAnalyticsUseCase(repo, Clock.systemUTC())()

        assertTrue(result.isSuccess)
        assertEquals(600L, result.getOrThrow().last().expense)
    }

    @Test
    fun `cashflow propagates repository failure`() = runTest {
        val failure = AppException.DataException("database unavailable")
        val result = GetCashflowAnalyticsUseCase(AnalyticsRepo(Result.failure(failure)), Clock.systemUTC())()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }
}
