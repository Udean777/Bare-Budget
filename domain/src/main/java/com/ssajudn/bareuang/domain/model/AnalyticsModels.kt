package com.ssajudn.bareuang.domain.model

data class CategorySummary(
    val category: TransactionCategory,
    val total: Long,
    val count: Long
)

/** Bounded dashboard transaction data; avoids loading the complete transaction table. */
data class DashboardTransactionData(
    val totalSpent: Long,
    val todaySpent: Long,
    val topCategories: List<CategorySummary>,
    val recentTransactions: List<Transaction>,
    val recurringTransactions: List<Transaction>,
)

data class DashboardSummary(
    val monthlyBudget: Long,
    val totalSpent: Long,
    val remainingBudget: Long,
    val daysPassed: Int,
    val daysInMonth: Int,
    val averageDailySpend: Long,
    val estimatedDeathDay: Int,
    val topCategories: List<CategorySummary>?,
    val unpaidDueBillsSum: Long,
    val netWorth: Long = 0L,
    val recentTransactions: List<Transaction>?,
    val recurringTransactions: List<Transaction> = emptyList(),
    val dailyAllowance: Long = 0L,
    val todaySpent: Long = 0L,
    val remainingToday: Long = 0L,
    val remainingDays: Int = 0,
    val runwayStatus: RunwayStatus = RunwayStatus.BudgetNotSet,
    val dailyPacingStatus: DailyPacingStatus = DailyPacingStatus(
        allowance = 0L,
        spent = 0L,
        remaining = 0L,
        isCustom = false,
    ),
) {
    val isDailyExceeded: Boolean get() = monthlyBudget > 0 && remainingToday < 0
    val dailyProgress: Float get() = if (dailyAllowance > 0) (todaySpent.toFloat() / dailyAllowance).coerceIn(0f, 1f) else 0f
}

data class CashflowDataPoint(
    val month: String,
    val label: String,
    val income: Long,
    val expense: Long
)

data class NetWorthDataPoint(
    val month: String,
    val label: String,
    val netWorth: Long
)
