package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.NetWorthDataPoint
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.domain.error.AppException
import javax.inject.Inject

class GetNetWorthAnalyticsUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val cashflowUseCase: GetCashflowAnalyticsUseCase
) {
    suspend operator fun invoke(): Result<List<NetWorthDataPoint>> {
        return try {
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            val currentNetWorth = wallets.sumOf { it.balance }
            val cashflow = cashflowUseCase().getOrDefault(emptyList())

            val points = ArrayList<NetWorthDataPoint>(cashflow.size)
            for (i in cashflow.indices) {
                points.add(NetWorthDataPoint("", "", 0L))
            }
            var runningNetWorth = currentNetWorth
            for (i in cashflow.indices.reversed()) {
                points[i] = NetWorthDataPoint(
                    month = cashflow[i].month,
                    label = cashflow[i].label,
                    netWorth = runningNetWorth
                )
                val netChange = cashflow[i].income - cashflow[i].expense
                runningNetWorth -= netChange
            }
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(AppException.UnknownError(e.message, e))
        }
    }
}
