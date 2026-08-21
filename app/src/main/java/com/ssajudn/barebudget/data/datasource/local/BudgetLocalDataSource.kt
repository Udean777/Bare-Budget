package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalBudgetEntity
import com.ssajudn.barebudget.domain.model.CategorySummary
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.data.repository.DomainMappers
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {

    @Suppress("DEPRECATION")
    suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            // Calculate dashboard metrics locally from Room DB
            val now = Calendar.getInstance()
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysPassed = now.get(Calendar.DAY_OF_MONTH)

            val localBudget = db.budgetDao().getBudget(monthYear)
            val monthlyBudget = localBudget?.monthlyLimit ?: 0L

            val allTx = db.transactionDao().getAllTransactions()
            val currentMonthTx = allTx.filter { it.date.startsWith(monthYear) }

            // Only count EXPENSE for total spent
            val expensesTx = currentMonthTx.filter {
                DomainMappers.safeTransactionType(it.type) == TransactionType.EXPENSE
            }
            val totalSpent = expensesTx.sumOf { it.amount }

            val remainingBudget = monthlyBudget - totalSpent
            val avgDaily = if (daysPassed > 0) totalSpent / daysPassed else 0L

            // Calculate Net Worth from local Wallets
            val wallets = db.walletDao().getAllWallets()
            val currentNetWorth = wallets.sumOf { it.balance }

            var estimatedDeathDay = daysInMonth
            var runwayMsg: String

            if (monthlyBudget <= 0) {
                runwayMsg = "Monthly budget not set yet. Tap here to set your target budget."
            } else if (remainingBudget <= 0) {
                estimatedDeathDay = daysPassed
                runwayMsg =
                    "CRITICAL: You have exhausted your budget for this month! Stop all non-essential spending."
            } else if (avgDaily <= 0) {
                estimatedDeathDay = daysInMonth
                runwayMsg = "GREAT: No expenses recorded yet this month. Keep it up!"
            } else {
                val daysRemainingFromRunway = (remainingBudget / avgDaily).toInt()
                val calculatedDeathDay = daysPassed + daysRemainingFromRunway
                estimatedDeathDay = calculatedDeathDay.coerceAtMost(daysInMonth)

                if (calculatedDeathDay < daysInMonth) {
                    runwayMsg =
                        "WARNING: At your current burn rate, your money runs out on day $calculatedDeathDay ($daysRemainingFromRunway days left)!"
                } else {
                    runwayMsg = "HEALTHY: Your financial runway is safe until the end of the month."
                }
            }

            // Category breakdown
            val catMap = currentMonthTx.groupBy { it.category }
            val topCategories = catMap.map { (catStr, list) ->
                CategorySummary(
                    category = DomainMappers.safeCategory(catStr),
                    total = list.sumOf { it.amount },
                    count = list.size.toLong()
                )
            }.sortedByDescending { it.total }

            // Due bills
            val allBills = db.dueBillDao().getAllDueBills()
            val unpaidBills = allBills.filter { it.status == DueBillStatus.UNPAID.name }
            val unpaidSum = unpaidBills.sumOf { it.totalAmount }

            val summary = DashboardSummary(
                monthlyBudget = monthlyBudget,
                totalSpent = totalSpent,
                remainingBudget = remainingBudget,
                daysPassed = daysPassed,
                daysInMonth = daysInMonth,
                averageDailySpend = avgDaily,
                estimatedDeathDay = estimatedDeathDay,
                runwayMessage = runwayMsg,
                topCategories = topCategories,
                unpaidDueBillsSum = unpaidSum,
                netWorth = currentNetWorth,
                recentTransactions = currentMonthTx.take(5).map { it.toTransaction() }
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                db.budgetDao().insertBudget(
                    LocalBudgetEntity(
                        monthYear = monthYear,
                        monthlyLimit = monthlyLimit,
                        isSynced = false
                    )
                )
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun getMonthlyBudget(monthYear: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
            } else monthYear
            val budget = db.budgetDao().getBudget(my)
            Result.success(budget?.monthlyLimit ?: 0L)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }
}