package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.data.datasource.local.AnalyticsLocalDataSource
import com.ssajudn.bareuang.domain.repository.AnalyticsData
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Clock

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val local: AnalyticsLocalDataSource
) : AnalyticsRepository {

    override suspend fun getAnalytics(clock: Clock): Result<AnalyticsData> = local.getAnalytics(clock)

    override suspend fun getCashflowAnalytics(clock: Clock): Result<List<CashflowDataPoint>> =
        local.getCashflowAnalytics(clock)

    override suspend fun getNetWorthAnalytics(clock: Clock): Result<List<NetWorthDataPoint>> =
        local.getNetWorthAnalytics(clock)
}
