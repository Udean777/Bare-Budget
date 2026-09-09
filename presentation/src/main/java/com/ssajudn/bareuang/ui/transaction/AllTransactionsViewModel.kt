package com.ssajudn.bareuang.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
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
    data class Success(
        val transactions: List<Transaction>,
        val wallets: List<Wallet>,
        val selectedCategory: TransactionCategory?,
        val selectedType: TransactionType?,
        val selectedWalletId: String?,
        val searchQuery: String,
        val filteredExpenseTotal: Long,
        val filteredIncomeTotal: Long,
    ) : AllTransactionsUiState

    data class Error(val message: String) : AllTransactionsUiState
}

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<TransactionCategory?>(null)
    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    private val _selectedWalletId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AllTransactionsUiState> = combine(
        repository.observeTransactions(),
        walletRepository.observeWallets(),
        _selectedCategory,
        _selectedType,
        _selectedWalletId,
        _searchQuery
    ) { args: Array<Any?> ->
        val all = (args[0] as List<Transaction>).filter { !it.isRecurringParent }
        val wallets = args[1] as List<Wallet>
        val cat = args[2] as? TransactionCategory
        val type = args[3] as? TransactionType
        val walletId = args[4] as? String
        val query = args[5] as String

        var filtered = all
        if (cat != null) {
            filtered = filtered.filter { it.category == cat }
        }
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }
        if (walletId != null) {
            filtered = filtered.filter { it.walletId == walletId || it.toWalletId == walletId }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter { tx ->
                (tx.merchant?.contains(query, ignoreCase = true) == true) ||
                        (tx.notes?.contains(query, ignoreCase = true) == true) ||
                        tx.category.name.contains(query, ignoreCase = true)
            }
        }

        val expenseTotal =
            filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val incomeTotal = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        AllTransactionsUiState.Success(
            transactions = filtered,
            wallets = wallets,
            selectedCategory = cat,
            selectedType = type,
            selectedWalletId = walletId,
            searchQuery = query,
            filteredExpenseTotal = expenseTotal,
            filteredIncomeTotal = incomeTotal,
        ) as AllTransactionsUiState
    }.catch { e ->
        android.util.Log.e(
            "AllTx",
            "observe failed",
            e
        ); emit(AllTransactionsUiState.Error(""))
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AllTransactionsUiState.Loading
        )

    fun loadTransactions() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getTransactions(limit = 100)
            walletRepository.getWallets()
            _isRefreshing.value = false
        }
    }

    fun applyFilters(
        category: TransactionCategory?,
        type: TransactionType?,
        walletId: String?
    ) {
        _selectedCategory.value = category
        _selectedType.value = type
        _selectedWalletId.value = walletId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query.take(100)
    }
}
