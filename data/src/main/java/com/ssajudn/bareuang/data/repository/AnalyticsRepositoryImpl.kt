package com.ssajudn.bareuang.data.repository

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
}
