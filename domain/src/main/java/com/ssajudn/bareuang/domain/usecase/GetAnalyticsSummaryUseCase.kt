package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CategorySummary
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import javax.inject.Inject
import java.time.Clock

/** Single domain entry point for the analytics screen. */
data class AnalyticsSummary(
    val dashboard: DashboardSummary,
    val cashflowTrend: List<CashflowDataPoint>,
    val netWorthTrend: List<NetWorthDataPoint>,
    val categories: List<CategorySummary>,
    val totalIncome: Long,
    val savageStreakDays: Int,
)

class GetAnalyticsSummaryUseCase @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val transactionRepository: TransactionRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val calculateSavageStreak: CalculateSavageStreakUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<AnalyticsSummary> {
        val dashboard = getDashboardSummary().getOrElse { return Result.failure(it) }
        val transactions = transactionRepository.getAllTransactions().getOrElse { return Result.failure(it) }
        val analyticsData = analyticsRepository.getAnalytics(clock).getOrElse { return Result.failure(it) }

        return Result.success(
            AnalyticsSummary(
                dashboard = dashboard,
                cashflowTrend = analyticsData.cashflow,
                netWorthTrend = analyticsData.netWorth,
                categories = dashboard.topCategories.orEmpty(),
                totalIncome = analyticsData.cashflow.lastOrNull()?.income ?: 0L,
                savageStreakDays = calculateSavageStreak(transactions),
            )
        )
    }
}
