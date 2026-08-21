package com.ssajudn.barebudget.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.UpdateDueBillRequest
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.model.RecurringInterval

sealed interface DueBillsUiState {
    object Loading : DueBillsUiState
    data class Success(val bills: List<DueBill>) : DueBillsUiState
    data class Error(val message: String) : DueBillsUiState
}

@HiltViewModel
class DueBillsViewModel @Inject constructor(
    private val repository: DueBillRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow<DueBillStatus?>(null)
    val selectedStatus: StateFlow<DueBillStatus?> = _selectedStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val wallets: StateFlow<List<Wallet>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DueBillsUiState> = combine(
        repository.observeDueBills(),
        _selectedStatus
    ) { bills, status ->
        if (status == null) bills else bills.filter { it.status == status }
    }.map<List<DueBill>, DueBillsUiState> { DueBillsUiState.Success(it) }
        .catch { e -> emit(DueBillsUiState.Error(e.message ?: "Failed to fetch due bills")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DueBillsUiState.Loading)

    init {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun setFilterStatus(status: DueBillStatus?) {
        _selectedStatus.value = status
    }

    fun loadWallets() {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun loadDueBills(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            walletRepository.getWallets()
            repository.getDueBills(_selectedStatus.value?.name)
            _isRefreshing.value = false
        }
    }

    fun addDueBill(providerName: String, providerIconUrl: String?, totalAmount: Long, dueDate: String, isRecurring: Boolean = false, recurringInterval: RecurringInterval = RecurringInterval.NONE, notes: String = "") {
        viewModelScope.launch {
            repository.createDueBill(CreateDueBillRequest(providerName, providerIconUrl, totalAmount, dueDate, isRecurring, recurringInterval, notes))
        }
    }

    fun updateDueBill(id: String, providerName: String, providerIconUrl: String?, totalAmount: Long, dueDate: String, isRecurring: Boolean = false, recurringInterval: RecurringInterval = RecurringInterval.NONE, notes: String = "") {
        viewModelScope.launch {
            repository.updateDueBill(id, UpdateDueBillRequest(providerName, providerIconUrl, totalAmount, dueDate, isRecurring, recurringInterval, notes))
        }
    }

    fun payBill(bill: DueBill, walletId: String) {
        viewModelScope.launch {
            if (bill.id != null) {
                repository.updateDueBillStatus(bill.id, DueBillStatus.PAID, walletId)
                if (bill.isRecurring && bill.recurringInterval != RecurringInterval.NONE) {
                    val nextDueDate = DateUtils.calculateNextDueDate(bill.dueDate, bill.recurringInterval.name)
                    repository.createDueBill(CreateDueBillRequest(bill.providerName, totalAmount = bill.totalAmount, dueDate = nextDueDate, isRecurring = true, recurringInterval = bill.recurringInterval, notes = bill.notes ?: ""))
                }
            }
        }
    }

    fun markBillAsUnpaid(bill: DueBill) {
        viewModelScope.launch {
            if (bill.id != null) repository.updateDueBillStatus(bill.id, DueBillStatus.UNPAID)
        }
    }

    fun toggleBillStatus(bill: DueBill, walletId: String? = null) {
        if (bill.status == DueBillStatus.UNPAID && walletId != null) payBill(bill, walletId)
        else if (bill.status == DueBillStatus.PAID) markBillAsUnpaid(bill)
    }

    fun deleteBill(id: String) {
        viewModelScope.launch { repository.deleteDueBill(id) }
    }
}
