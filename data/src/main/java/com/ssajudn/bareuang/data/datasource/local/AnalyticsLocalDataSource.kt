package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsData
import com.ssajudn.bareuang.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val clock: Clock,
) {

    suspend fun getAnalytics(referenceClock: Clock = clock): Result<AnalyticsData> = withContext(Dispatchers.IO) {
        try {
            val cashflow = getCashflowAnalytics(referenceClock).getOrElse { return@withContext Result.failure(it) }
            val wallets = db.walletDao().getAllWallets()
            val currentNetWorth = wallets.fold(0L) { total, wallet -> Math.addExact(total, wallet.balance) }
            val points = ArrayList<NetWorthDataPoint>(cashflow.size)
            var runningNetWorth = currentNetWorth
            for (i in cashflow.indices.reversed()) {
                points.add(
                    0,
                    NetWorthDataPoint(
                        month = cashflow[i].month,
                        label = cashflow[i].label,
                        netWorth = runningNetWorth
                    )
                )
                runningNetWorth = Math.subtractExact(
                    runningNetWorth,
                    Math.subtractExact(cashflow[i].income, cashflow[i].expense)
                )
            }
            Result.success(AnalyticsData(cashflow = cashflow, netWorth = points))
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun getCashflowAnalytics(referenceClock: Clock = clock): Result<List<CashflowDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val points = mutableListOf<CashflowDataPoint>()
            val currentMonth = YearMonth.from(LocalDate.now(referenceClock))
            val fromMonth = currentMonth.minusMonths(5)
            val fromDate = fromMonth.toString() + "-01"
            val toDate = currentMonth.plusMonths(1).toString() + "-01"
            val labelFormat = DateTimeFormatter.ofPattern("MMM", Locale("id", "ID"))
            val rows = db.transactionDao().getCashflowByMonth(fromDate, toDate).associateBy { it.month }

            for (i in 5 downTo 0) {
                val month = currentMonth.minusMonths(i.toLong())
                val monthKey = month.toString()
                val label = month.atDay(1).format(labelFormat)
                val row = rows[monthKey]

                points.add(
                    CashflowDataPoint(
                        month = monthKey,
                        label = label,
                        income = row?.income ?: 0L,
                        expense = row?.expense ?: 0L
                    )
                )
            }

            Result.success(points)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun getNetWorthAnalytics(referenceClock: Clock = clock): Result<List<NetWorthDataPoint>> =
        getAnalytics(referenceClock).map { it.netWorth }
}
