package com.ssajudn.bareuang.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.usecase.GetAnalyticsSummaryUseCase
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint

enum class AnalyticsTab(val resId: Int) {
    CASHFLOW(R.string.analytics_tab_cashflow),
    NET_WORTH(R.string.analytics_tab_networth),
    CATEGORIES(R.string.analytics_tab_categories)
}

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
        val totalIncome: Long,
        val netWorth: Long,
        val monthlyBudget: Long,
        val dailyAverage: Long,
        val topSpendingCategory: CategoryBreakdownItem?,
        val categories: List<CategoryBreakdownItem>,
        val savageStreakDays: Int,
        val cashflowTrend: List<CashflowDataPoint>,
        val netWorthTrend: List<NetWorthDataPoint>,
        val selectedTab: AnalyticsTab = AnalyticsTab.CASHFLOW
    ) : AnalyticsUiState

    data class Error(val message: UiText) : AnalyticsUiState
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsSummary: GetAnalyticsSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun selectTab(tab: AnalyticsTab) {
        val current = _uiState.value
        if (current is AnalyticsUiState.Success) {
            _uiState.value = current.copy(selectedTab = tab)
        }
    }

    fun loadAnalyticsData(isPullToRefresh: Boolean = false) {
        if (isPullToRefresh && _isRefreshing.value) return
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is AnalyticsUiState.Success) {
                _uiState.value = AnalyticsUiState.Loading
            }

            val result = getAnalyticsSummary()
            if (result.isFailure) {
                _isRefreshing.value = false
                if (_uiState.value !is AnalyticsUiState.Success) {
                    val cause = result.exceptionOrNull()
                    android.util.Log.e("Analytics", "load failed", cause)
                    _uiState.value = AnalyticsUiState.Error(
                        UiText.Res(R.string.analytics_error_message)
                    )
                }
                return@launch
            }

            if (result.isSuccess) {
                val analytics = result.getOrNull()!!
                val summary = analytics.dashboard
                val cashflow = analytics.cashflowTrend
                val netWorthTrend = analytics.netWorthTrend

                val totalSpent = summary.totalSpent
                val totalIncome = analytics.totalIncome
                val netWorth = summary.netWorth
                val topCategoriesRaw = analytics.categories

                val breakdownItems = topCategoriesRaw.map { catSummary ->
                    val pct = if (totalSpent > 0) ((catSummary.total.toFloat() / totalSpent.toFloat()).coerceIn(0f, 1f)) else 0f
                    CategoryBreakdownItem(
                        category = catSummary.category,
                        totalAmount = catSummary.total,
                        transactionCount = catSummary.count,
                        percentage = pct
                    )
                }.sortedByDescending { it.totalAmount }

                val topCat = breakdownItems.firstOrNull()
                val prevTab = (_uiState.value as? AnalyticsUiState.Success)?.selectedTab ?: AnalyticsTab.CASHFLOW

                _uiState.value = AnalyticsUiState.Success(
                    totalSpent = totalSpent,
                    totalIncome = totalIncome,
                    netWorth = netWorth,
                    monthlyBudget = summary.monthlyBudget,
                    dailyAverage = summary.averageDailySpend,
                    topSpendingCategory = topCat,
                    categories = breakdownItems,
                    savageStreakDays = analytics.savageStreakDays,
                    cashflowTrend = cashflow,
                    netWorthTrend = netWorthTrend,
                    selectedTab = prevTab
                )
                _isRefreshing.value = false
            }
        }
    }
}
