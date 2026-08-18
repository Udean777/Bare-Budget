package com.ssajudn.barebudget.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CreateDueBillRequest
import com.ssajudn.barebudget.data.model.DueBill
import com.ssajudn.barebudget.data.model.DueBillStatus
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

    init {
        loadDueBills()
    }

    fun loadDueBills(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is DueBillsUiState.Success) {
                _uiState.value = DueBillsUiState.Loading
            }

            repository.getDueBills()
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

    fun addDueBill(providerName: String, totalAmount: Long, dueDate: String, notes: String = "") {
        viewModelScope.launch {
            val request = CreateDueBillRequest(
                providerName = providerName,
                totalAmount = totalAmount,
                dueDate = dueDate,
                notes = notes
            )
            repository.createDueBill(request)
                .onSuccess {
                    loadDueBills()
                }
        }
    }

    fun toggleBillStatus(bill: DueBill) {
        val nextStatus = if (bill.status == DueBillStatus.UNPAID) DueBillStatus.PAID else DueBillStatus.UNPAID
        viewModelScope.launch {
            if (bill.id != null) {
                repository.updateDueBillStatus(bill.id, nextStatus)
                    .onSuccess {
                        loadDueBills()
                    }
            }
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
