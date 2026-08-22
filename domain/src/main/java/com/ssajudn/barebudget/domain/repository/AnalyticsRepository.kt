package com.ssajudn.barebudget.domain.repository

import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint

interface AnalyticsRepository {
    suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>>
    suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>>
}
