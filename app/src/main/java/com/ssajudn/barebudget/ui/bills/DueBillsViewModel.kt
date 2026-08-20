package com.ssajudn.barebudget.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CreateDueBillRequest
import com.ssajudn.barebudget.data.model.DueBill
import com.ssajudn.barebudget.data.model.DueBillStatus
import com.ssajudn.barebudget.data.model.RecurringInterval
import com.ssajudn.barebudget.data.model.UpdateDueBillRequest
import com.ssajudn.barebudget.data.repository.BudgetRepository
import com.ssajudn.barebudget.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DueBillsUiState {
    object Loading : DueBillsUiState
    data class Success(val bills: List<DueBill>) : DueBillsUiState
    data class Error(val message: String) : DueBillsUiState
}

class DueBillsViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DueBillsUiState>(DueBillsUiState.Loading)
    val uiState: StateFlow<DueBillsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedStatus = MutableStateFlow<DueBillStatus?>(null) // null = ALL
    val selectedStatus: StateFlow<DueBillStatus?> = _selectedStatus.asStateFlow()

    private val _wallets = MutableStateFlow<List<com.ssajudn.barebudget.data.model.Wallet>>(emptyList())
    val wallets: StateFlow<List<com.ssajudn.barebudget.data.model.Wallet>> = _wallets.asStateFlow()

    init {
        loadDueBills()
        loadWallets()
    }

    fun setFilterStatus(status: DueBillStatus?) {
        _selectedStatus.value = status
        loadDueBills()
    }

    fun loadWallets() {
        viewModelScope.launch {
            repository.getWallets().onSuccess {
                _wallets.value = it
            }
        }
    }

    fun loadDueBills(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is DueBillsUiState.Success) {
                _uiState.value = DueBillsUiState.Loading
            }

            loadWallets()
            val currentFilter = _selectedStatus.value

            repository.getDueBills(currentFilter?.name)
                .onSuccess { bills ->
                    _uiState.value = DueBillsUiState.Success(bills)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _isRefreshing.value = false
                    if (_uiState.value !is DueBillsUiState.Success) {
                        _uiState.value = DueBillsUiState.Error(error.localizedMessage ?: "Failed to fetch due bills")
                    }
                }
        }
    }

    fun addDueBill(
        providerName: String,
        providerIconUrl: String?,
        totalAmount: Long,
        dueDate: String,
        isRecurring: Boolean = false,
        recurringInterval: com.ssajudn.barebudget.data.model.RecurringInterval = com.ssajudn.barebudget.data.model.RecurringInterval.NONE,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val request = CreateDueBillRequest(
                providerName = providerName,
                providerIconUrl = providerIconUrl,
                totalAmount = totalAmount,
                dueDate = dueDate,
                isRecurring = isRecurring,
                recurringInterval = recurringInterval,
                notes = notes
            )
            repository.createDueBill(request)
                .onSuccess {
                    loadDueBills()
                }
        }
    }

    fun updateDueBill(
        id: String,
        providerName: String,
        providerIconUrl: String?,
        totalAmount: Long,
        dueDate: String,
        isRecurring: Boolean = false,
        recurringInterval: RecurringInterval = RecurringInterval.NONE,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val request = UpdateDueBillRequest(
                providerName = providerName,
                providerIconUrl = providerIconUrl,
                totalAmount = totalAmount,
                dueDate = dueDate,
                isRecurring = isRecurring,
                recurringInterval = recurringInterval,
                notes = notes
            )
            repository.updateDueBill(id, request)
                .onSuccess {
                    loadDueBills()
                }
        }
    }

    fun payBill(bill: DueBill, walletId: String) {
        viewModelScope.launch {
            if (bill.id != null) {
                repository.updateDueBillStatus(bill.id, DueBillStatus.PAID, walletId)
                    .onSuccess {
                        // Auto-rollover: If marked as PAID and it's a recurring bill, create the next period's bill automatically
                        if (bill.isRecurring && bill.recurringInterval != com.ssajudn.barebudget.data.model.RecurringInterval.NONE) {
                            val nextDueDate = DateUtils.calculateNextDueDate(bill.dueDate, bill.recurringInterval.name)
                            val nextBillRequest = CreateDueBillRequest(
                                providerName = bill.providerName,
                                totalAmount = bill.totalAmount,
                                dueDate = nextDueDate,
                                isRecurring = true,
                                recurringInterval = bill.recurringInterval,
                                notes = bill.notes ?: ""
                            )
                            repository.createDueBill(nextBillRequest)
                        }
                        loadDueBills()
                        loadWallets()
                    }
            }
        }
    }

    fun markBillAsUnpaid(bill: DueBill) {
        viewModelScope.launch {
            if (bill.id != null) {
                repository.updateDueBillStatus(bill.id, DueBillStatus.UNPAID)
                    .onSuccess {
                        loadDueBills()
                    }
            }
        }
    }

    fun toggleBillStatus(bill: DueBill, walletId: String? = null) {
        if (bill.status == DueBillStatus.UNPAID && walletId != null) {
            payBill(bill, walletId)
        } else if (bill.status == DueBillStatus.PAID) {
            markBillAsUnpaid(bill)
        }
    }

    fun deleteBill(id: String) {
        viewModelScope.launch {
            repository.deleteDueBill(id)
                .onSuccess {
                    loadDueBills()
                }
        }
    }
}
