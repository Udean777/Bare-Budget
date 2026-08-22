package com.ssajudn.barebudget.data.datasource.remote

import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.data.datasource.local.AnalyticsLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val local: AnalyticsLocalDataSource
) {

    suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCashflowAnalytics()
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!.data)
            }
        } catch (e: Exception) {
            // fall through to local calculation
        }
        local.getCashflowAnalytics()
    }

    suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getNetWorthAnalytics()
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!.data)
            }
        } catch (e: Exception) {
            // fall through to local calculation
        }
        local.getNetWorthAnalytics()
    }
}