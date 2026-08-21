package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.CategorySummary
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.ssajudn.barebudget.domain.error.AppException
import javax.inject.Inject

/**
 * Pure domain logic — menghitung dashboard dari data repo.
 * Menggantikan `LocalBudgetRepository.getDashboardSummary()` (SRP).
 * Dependency: domain ports, bukan Room/Retrofit langsung.
 */
class GetDashboardSummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val dueBillRepository: DueBillRepository
) {
    suspend operator fun invoke(): Result<DashboardSummary> {
        return try {
            val now = Calendar.getInstance()
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysPassed = now.get(Calendar.DAY_OF_MONTH)

            val monthlyBudget = budgetRepository.getMonthlyBudget(monthYear).getOrDefault(0L)

            val allTx = transactionRepository.getTransactions(limit = 500).getOrDefault(emptyList())
            val currentMonthTx = allTx.filter { it.date.startsWith(monthYear) }

            val expensesTx = currentMonthTx.filter { it.type == TransactionType.EXPENSE && it.category != com.ssajudn.barebudget.domain.model.TransactionCategory.BILLS }
            val totalSpent = expensesTx.sumOf { it.amount }

            val remainingBudget = monthlyBudget - totalSpent
            val avgDaily = if (daysPassed > 0) totalSpent / daysPassed else 0L

            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            val currentNetWorth = wallets.sumOf { it.balance }

            var estimatedDeathDay = daysInMonth
            var runwayMsg: String

            if (monthlyBudget <= 0) {
                runwayMsg = "Monthly budget not set yet. Tap here to set your target budget."
            } else if (remainingBudget <= 0) {
                estimatedDeathDay = daysPassed
                runwayMsg = "CRITICAL: You have exhausted your budget for this month! Stop all non-essential spending."
            } else if (avgDaily <= 0) {
                estimatedDeathDay = daysInMonth
                runwayMsg = "GREAT: No expenses recorded yet this month. Keep it up!"
            } else {
                val daysRemainingFromRunway = (remainingBudget / avgDaily).toInt()
                val calculatedDeathDay = daysPassed + daysRemainingFromRunway
                estimatedDeathDay = calculatedDeathDay.coerceAtMost(daysInMonth)
                runwayMsg = if (calculatedDeathDay < daysInMonth) {
                    "WARNING: At your current burn rate, your money runs out on day $calculatedDeathDay ($daysRemainingFromRunway days left)!"
                } else {
                    "HEALTHY: Your financial runway is safe until the end of the month."
                }
            }

            val catMap = currentMonthTx.groupBy { it.category }
            val topCategories = catMap.map { (cat, list) ->
                CategorySummary(
                    category = cat,
                    total = list.sumOf { it.amount },
                    count = list.size.toLong()
                )
            }.sortedByDescending { it.total }

            val allBills = dueBillRepository.getDueBills().getOrDefault(emptyList())
            val unpaidSum = allBills.filter { it.status == DueBillStatus.UNPAID }.sumOf { it.totalAmount }

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
                recentTransactions = currentMonthTx
                    .reversed()
                    .sortedByDescending { it.date }
                    .take(5)
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(AppException.UnknownError(e.message, e))
        }
    }
}
