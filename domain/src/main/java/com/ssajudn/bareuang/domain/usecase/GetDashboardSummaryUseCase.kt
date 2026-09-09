package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.BudgetPeriod
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.model.RunwayStatus
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** Orchestrates dashboard data retrieval and delegates each calculation to a focused use case. */
class GetDashboardSummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val dueBillRepository: DueBillRepository,
    private val dailyPacingPreferences: DailyPacingPreferencesPort,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<DashboardSummary> = try {
        val today = LocalDate.now(clock)
        val month = YearMonth.from(today)
        val period = BudgetPeriod(
            monthYear = month.toString(),
            todayIso = today.toString(),
            daysPassed = today.dayOfMonth,
            daysInMonth = month.lengthOfMonth(),
        )
        val monthlyBudget = budgetRepository.getMonthlyBudget(period.monthYear).getOrElse { return Result.failure(it) }
        val dashboardTransactions = transactionRepository.getDashboardTransactions(period.monthYear, period.todayIso)
            .getOrElse { return Result.failure(it) }
        val totalSpent = dashboardTransactions.totalSpent
        val runway = CalculateBudgetRunwayUseCase(monthlyBudget, totalSpent, period)
        val todaySpent = dashboardTransactions.todaySpent
        val pacing = CalculateDailyPacingUseCase(
            monthlyBudget = monthlyBudget,
            remainingBudget = runway.remainingBudget,
            todaySpent = todaySpent,
            period = period,
            customTarget = dailyPacingPreferences.customTarget.value,
        )
        val wallets = walletRepository.getWallets().getOrElse { return Result.failure(it) }
        val bills = dueBillRepository.getDueBills().getOrElse { return Result.failure(it) }
        val billSummary = GetOutstandingBillsSummaryUseCase(bills)

        Result.success(
            DashboardSummary(
                monthlyBudget = monthlyBudget,
                totalSpent = totalSpent,
                remainingBudget = runway.remainingBudget,
                daysPassed = period.daysPassed,
                daysInMonth = period.daysInMonth,
                averageDailySpend = runway.averageDailySpend,
                estimatedDeathDay = runway.estimatedDeathDay,
                topCategories = dashboardTransactions.topCategories,
                unpaidDueBillsSum = billSummary.unpaidTotal,
                netWorth = CalculateNetWorthUseCase(wallets),
                recentTransactions = dashboardTransactions.recentTransactions,
                recurringTransactions = dashboardTransactions.recurringTransactions,
                dailyAllowance = pacing.allowance,
                todaySpent = pacing.spent,
                remainingToday = pacing.remaining,
                remainingDays = period.remainingDays,
                runwayStatus = runway.status,
                dailyPacingStatus = pacing,
            ),
        )
    } catch (e: ArithmeticException) {
        Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
    } catch (e: Exception) {
        Result.failure(AppException.UnknownError(cause = e))
    }

}
