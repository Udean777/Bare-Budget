package com.ssajudn.barebudget.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.error.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import javax.inject.Inject

data class BudgetUiState(
    val currentLimit: Long = 0L,
    val rawAmount: String = "",
    val parsedAmount: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isLocked: Boolean get() = currentLimit > 0
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBudget()
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMonthlyBudget()
                .onSuccess { existing ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLimit = existing,
                        rawAmount = if (existing > 0) existing.toString() else "",
                        parsedAmount = existing
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun onAmountChange(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(12)
        val parsed = digitsOnly.toLongOrNull() ?: 0L
        _uiState.value = _uiState.value.copy(
            rawAmount = digitsOnly,
            parsedAmount = parsed
        )
    }

    fun saveBudget() {
        val state = _uiState.value
        if (state.isLocked) {
            _uiState.value = state.copy(errorMessage = "Budget bulan ini sudah terkunci. Hanya bisa diubah bulan depan.")
            return
        }
        if (state.parsedAmount <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid budget amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            repository.setBudget(state.parsedAmount)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    _operation.value = OperationState.Success()
                    viewModelScope.launch { _effect.send(UiEffect.PopBackStack) }
                }
                .onFailure { error ->
                    val msg = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Failed to set budget"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                    _operation.value = OperationState.Error(msg)
                    viewModelScope.launch { _effect.send(UiEffect.ShowSnackbar(msg)) }
                }
        }
    }
}
