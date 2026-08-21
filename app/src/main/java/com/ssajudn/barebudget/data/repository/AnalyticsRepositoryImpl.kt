package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint
import com.ssajudn.barebudget.data.datasource.local.AnalyticsLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.AnalyticsRemoteDataSource
import com.ssajudn.barebudget.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val local: AnalyticsLocalDataSource,
    private val remote: AnalyticsRemoteDataSource,
    private val sessionManager: UserSessionManager
) : AnalyticsRepository {

    override suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>> =
        if (sessionManager.isGuestMode) local.getCashflowAnalytics()
        else remote.getCashflowAnalytics()

    override suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>> =
        if (sessionManager.isGuestMode) local.getNetWorthAnalytics()
        else remote.getNetWorthAnalytics()
}
