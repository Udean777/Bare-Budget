package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.repository.AnalyticsRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val transactions = db.transactionDao().getAllTransactions()
            val points = mutableListOf<CashflowDataPoint>()
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val labelFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                val monthKey = monthFormat.format(cal.time)
                val label = labelFormat.format(cal.time)

                val monthTxs = transactions.filter { it.date.startsWith(monthKey) }
                val income = monthTxs
                    .filter { it.type == TransactionType.INCOME.name }
                    .sumOf { it.amount }
                val expense = monthTxs
                    .filter { it.type == TransactionType.EXPENSE.name || it.type.isBlank() }
                    .sumOf { it.amount }

                points.add(
                    CashflowDataPoint(
                        month = monthKey,
                        label = label,
                        income = income,
                        expense = expense
                    )
                )
            }

            Result.success(points)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val wallets = db.walletDao().getAllWallets()
            val currentNetWorth = wallets.sumOf { it.balance }
            val cashflowResult = getCashflowAnalytics()
            val cashflow = cashflowResult.getOrDefault(emptyList())

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
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }
}