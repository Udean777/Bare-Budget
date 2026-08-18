package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CreateTransactionRequest
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.data.repository.BudgetRepository
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val rawAmount: String = "",
    val parsedAmount: Long = 0L,
    val merchant: String = "",
    val selectedCategory: TransactionCategory = TransactionCategory.FOOD,
    val date: String = DateUtils.getCurrentDateISO(),
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AddTransactionViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(12) // Limit up to hundreds of billions
        val parsed = digitsOnly.toLongOrNull() ?: 0L
        _uiState.value = _uiState.value.copy(
            rawAmount = digitsOnly,
            parsedAmount = parsed
        )
    }

    fun onMerchantChange(merchant: String) {
        _uiState.value = _uiState.value.copy(merchant = merchant)
    }

    fun onCategoryChange(category: TransactionCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.parsedAmount <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter an amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val request = CreateTransactionRequest(
                amount = state.parsedAmount,
                category = state.selectedCategory,
                merchant = state.merchant.ifBlank { state.selectedCategory.displayName },
                date = state.date,
                notes = state.notes
            )

            repository.createTransaction(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to save transaction"
                    )
                }
        }
    }
}
