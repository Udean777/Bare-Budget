package com.ssajudn.barebudget.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CategorySummary
import com.ssajudn.barebudget.data.model.Transaction
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryBreakdownItem(
    val category: TransactionCategory,
    val totalAmount: Long,
    val transactionCount: Long,
    val percentage: Float // 0.0f - 1.0f
)

sealed interface AnalyticsUiState {
    object Loading : AnalyticsUiState
    data class Success(
        val totalSpent: Long,
        val monthlyBudget: Long,
        val dailyAverage: Long,
        val topSpendingCategory: CategoryBreakdownItem?,
        val categories: List<CategoryBreakdownItem>,
        val savageStreakDays: Int
    ) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

class AnalyticsViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun loadAnalyticsData(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is AnalyticsUiState.Success) {
                _uiState.value = AnalyticsUiState.Loading
            }

            val summaryResult = repository.getDashboardSummary()
            val transactionsResult = repository.getTransactions(limit = 100)

            if (summaryResult.isSuccess) {
                val summary = summaryResult.getOrNull()!!
                val transactions = transactionsResult.getOrDefault(emptyList())

                val total = summary.totalSpent
                val topCategoriesRaw = summary.topCategories ?: emptyList()

                val breakdownItems = topCategoriesRaw.map { catSummary ->
                    val pct = if (total > 0) (catSummary.total.toFloat() / total.toFloat()) else 0f
                    CategoryBreakdownItem(
                        category = catSummary.category,
                        totalAmount = catSummary.total,
                        transactionCount = catSummary.count,
                        percentage = pct
                    )
                }.sortedByDescending { it.totalAmount }

                val topCat = breakdownItems.firstOrNull()
                val streak = calculateSavageStreak(transactions)

                _uiState.value = AnalyticsUiState.Success(
                    totalSpent = total,
                    monthlyBudget = summary.monthlyBudget,
                    dailyAverage = summary.averageDailySpend,
                    topSpendingCategory = topCat,
                    categories = breakdownItems,
                    savageStreakDays = streak
                )
                _isRefreshing.value = false
            } else {
                _isRefreshing.value = false
                if (_uiState.value !is AnalyticsUiState.Success) {
                    _uiState.value = AnalyticsUiState.Error(
                        summaryResult.exceptionOrNull()?.localizedMessage ?: "Failed to load analytics"
                    )
                }
            }
        }
    }

    /**
     * Calculates Savage Streak: Number of consecutive days without F&B / Entertainment expenses.
     */
    private fun calculateSavageStreak(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 3 // Default clean streak if fresh

        val entertainmentOrFoodDates = transactions
            .filter { it.category == TransactionCategory.FOOD || it.category == TransactionCategory.ENTERTAINMENT }
            .mapNotNull { it.date.takeIf { d -> d.length >= 10 }?.substring(0, 10) }
            .toSet()

        // If no food/entertainment logged in past days, user has an active streak
        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        for (i in 0 until 30) {
            val dateStr = sdf.format(calendar.time)
            if (!entertainmentOrFoodDates.contains(dateStr)) {
                streak++
            } else {
                if (i > 0) break // Streak broken
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        return streak.coerceAtLeast(1)
    }
}
