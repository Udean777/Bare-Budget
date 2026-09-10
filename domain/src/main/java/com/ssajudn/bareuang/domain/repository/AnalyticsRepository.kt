package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import java.time.Clock

interface AnalyticsRepository {
    suspend fun getAnalytics(clock: Clock = Clock.systemUTC()): Result<AnalyticsData>
}

data class AnalyticsData(
    val cashflow: List<CashflowDataPoint>,
    val netWorth: List<NetWorthDataPoint>,
)
