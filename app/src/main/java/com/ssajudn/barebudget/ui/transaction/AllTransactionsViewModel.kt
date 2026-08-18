package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.Transaction
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AllTransactionsUiState {
    object Loading : AllTransactionsUiState
    data class Success(
        val transactions: List<Transaction>,
        val selectedCategory: TransactionCategory?,
        val searchQuery: String
    ) : AllTransactionsUiState
    data class Error(val message: String) : AllTransactionsUiState
}

class AllTransactionsViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllTransactionsUiState>(AllTransactionsUiState.Loading)
    val uiState: StateFlow<AllTransactionsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var allFetchedTransactions: List<Transaction> = emptyList()
    private var currentSelectedCategory: TransactionCategory? = null
    private var currentSearchQuery: String = ""

    init {
        loadTransactions()
    }

    fun loadTransactions(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is AllTransactionsUiState.Success) {
                _uiState.value = AllTransactionsUiState.Loading
            }

            val categoryQuery = currentSelectedCategory?.name
            repository.getTransactions(category = categoryQuery, limit = 100)
                .onSuccess { list ->
                    allFetchedTransactions = list
                    applyFilters()
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _isRefreshing.value = false
                    if (_uiState.value !is AllTransactionsUiState.Success) {
                        _uiState.value = AllTransactionsUiState.Error(error.localizedMessage ?: "Failed to load transactions")
                    }
                }
        }
    }

    fun filterByCategory(category: TransactionCategory?) {
        currentSelectedCategory = category
        loadTransactions()
    }

    fun onSearchQueryChange(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = if (currentSearchQuery.isBlank()) {
            allFetchedTransactions
        } else {
            allFetchedTransactions.filter { tx ->
                (tx.merchant?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                (tx.notes?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                tx.category.displayName.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        _uiState.value = AllTransactionsUiState.Success(
            transactions = filtered,
            selectedCategory = currentSelectedCategory,
            searchQuery = currentSearchQuery
        )
    }
}
