package com.ssajudn.bareuang.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort
import com.ssajudn.bareuang.domain.model.CreateDueBillRequest
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.UpdateDueBillRequest
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect

sealed interface DueBillsUiState {
    object Loading : DueBillsUiState
    data class Success(val bills: List<DueBill>) : DueBillsUiState
    data class Error(val message: String, val uiText: UiText = UiText.Res(R.string.bills_load_error)) : DueBillsUiState
}

@HiltViewModel
class DueBillsViewModel @Inject constructor(
    private val repository: DueBillRepository,
    private val walletRepository: WalletRepository,
    private val reminderScheduler: BillReminderSchedulerPort
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow(DueBillStatus.UNPAID)
    val selectedStatus: StateFlow<DueBillStatus> = _selectedStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val wallets: StateFlow<List<Wallet>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DueBillsUiState> = combine(
        repository.observeDueBills(),
        _selectedStatus,
        _searchQuery
    ) { bills, status, query ->
        var filtered = bills.filter { it.status == status }
        if (query.isNotBlank()) {
            filtered = filtered.filter { bill ->
                bill.providerName.contains(query, ignoreCase = true) ||
                    (bill.notes?.contains(query, ignoreCase = true) == true)
            }
        }
        filtered
    }.map<List<DueBill>, DueBillsUiState> { DueBillsUiState.Success(it) }
        .catch { e -> android.util.Log.e("Bills", "observe failed", e); emit(DueBillsUiState.Error("", UiText.Res(R.string.bills_load_error))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DueBillsUiState.Loading)

    init {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query.take(100)
    }

    fun setFilterStatus(status: DueBillStatus) {
        _selectedStatus.value = status
    }

    fun loadWallets() {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun loadDueBills(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            walletRepository.getWallets()
            repository.getDueBills(_selectedStatus.value.name)
            _isRefreshing.value = false
        }
    }

    fun addDueBill(providerName: String, providerIconUrl: String?, totalAmount: Long, dueDate: String, isRecurring: Boolean = false, recurringInterval: RecurringInterval = RecurringInterval.NONE, notes: String = "") {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = repository.createDueBill(CreateDueBillRequest(providerName, providerIconUrl, totalAmount, dueDate, isRecurring, recurringInterval, notes))
            val ui = UiText.Res(R.string.bills_error_create)
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error("", ui)
            if (r.isSuccess) {
                reminderScheduler.runNow()
                _effect.send(UiEffect.PopBackStack)
            } else _effect.send(UiEffect.ShowSnackbarRes(ui))
        }
    }

    fun updateDueBill(id: String, providerName: String, providerIconUrl: String?, totalAmount: Long, dueDate: String, isRecurring: Boolean = false, recurringInterval: RecurringInterval = RecurringInterval.NONE, notes: String = "") {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = repository.updateDueBill(id, UpdateDueBillRequest(providerName, providerIconUrl, totalAmount, dueDate, isRecurring, recurringInterval, notes))
            val ui = UiText.Res(R.string.bills_error_update)
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error("", ui)
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbarRes(ui)) else reminderScheduler.runNow()
        }
    }

    fun payBill(bill: DueBill, walletId: String) {
        viewModelScope.launch {
            val bid = bill.id
            if (bid != null) {
                _operation.value = OperationState.Loading
                val result = repository.updateDueBillStatus(bid, DueBillStatus.PAID, walletId)
                if (result.isSuccess && bill.isRecurring && bill.recurringInterval != RecurringInterval.NONE) {
                    val nextDueDate = DateUtils.calculateNextDueDate(bill.dueDate, bill.recurringInterval.name)
                    repository.createDueBill(CreateDueBillRequest(bill.providerName, providerIconUrl = bill.providerIconUrl, totalAmount = bill.totalAmount, dueDate = nextDueDate, isRecurring = true, recurringInterval = bill.recurringInterval, notes = bill.notes ?: ""))
                }
                _operation.value = if (result.isSuccess) OperationState.Success() else OperationState.Error("", UiText.Res(R.string.bills_error_insufficient))
                if (result.isFailure) _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.bills_error_insufficient))) else reminderScheduler.runNow()
            }
        }
    }

    fun markBillAsUnpaid(bill: DueBill) {
        viewModelScope.launch {
            val bid = bill.id
            if (bid != null) {
                val r = repository.updateDueBillStatus(bid, DueBillStatus.UNPAID)
                if (r.isFailure) _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.bills_error_update))) else reminderScheduler.runNow()
            }
        }
    }

    fun toggleBillStatus(bill: DueBill, walletId: String? = null) {
        if (bill.status == DueBillStatus.UNPAID && walletId != null) payBill(bill, walletId)
        else if (bill.status == DueBillStatus.PAID) markBillAsUnpaid(bill)
    }

    fun deleteBill(id: String) {
        viewModelScope.launch {
            val r = repository.deleteDueBill(id)
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.bills_error_delete))) else reminderScheduler.runNow()
        }
    }
}
