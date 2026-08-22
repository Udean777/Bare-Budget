package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AllTransactionsUiState {
    object Loading : AllTransactionsUiState
    data class Success(val transactions: List<Transaction>, val selectedCategory: TransactionCategory?, val searchQuery: String) : AllTransactionsUiState
    data class Error(val message: String) : AllTransactionsUiState
}

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<TransactionCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<AllTransactionsUiState> = combine(
        repository.observeTransactions(),
        _selectedCategory,
        _searchQuery
    ) { all, cat, query ->
        var filtered = if (cat == null) all else all.filter { it.category == cat }
        if (query.isNotBlank()) {
            filtered = filtered.filter { tx ->
                (tx.merchant?.contains(query, ignoreCase = true) == true) ||
                    (tx.notes?.contains(query, ignoreCase = true) == true) ||
                    tx.category.displayName.contains(query, ignoreCase = true)
            }
        }
        AllTransactionsUiState.Success(filtered, cat, query) as AllTransactionsUiState
    }.catch { e -> emit(AllTransactionsUiState.Error(e.message ?: "Failed to load transactions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AllTransactionsUiState.Loading)

    fun loadTransactions(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getTransactions(limit = 100)
            _isRefreshing.value = false
        }
    }

    fun filterByCategory(category: TransactionCategory?) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
