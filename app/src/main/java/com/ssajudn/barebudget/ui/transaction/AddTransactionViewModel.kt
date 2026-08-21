package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.error.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val selectedToWalletId: String? = null,
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

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            val result = walletRepository.getWallets()
            if (result.isSuccess) {
                val wallets = result.getOrNull() ?: emptyList()
                val defaultWallet = wallets.firstOrNull()?.id
                val defaultToWallet = wallets.getOrNull(1)?.id ?: defaultWallet
                _uiState.value = _uiState.value.copy(
                    wallets = wallets,
                    selectedWalletId = defaultWallet,
                    selectedToWalletId = defaultToWallet
                )
            }
        }
    }

    fun onTransactionTypeChange(type: TransactionType) {
        val newCategory = when (type) {
            TransactionType.INCOME -> TransactionCategory.SALARY
            TransactionType.TRANSFER -> TransactionCategory.TRANSFER
            TransactionType.EXPENSE -> TransactionCategory.FOOD
        }
        _uiState.value = _uiState.value.copy(
            transactionType = type,
            selectedCategory = newCategory
        )
    }

    fun onWalletChange(walletId: String) {
        _uiState.value = _uiState.value.copy(selectedWalletId = walletId)
    }

    fun onToWalletChange(walletId: String) {
        _uiState.value = _uiState.value.copy(selectedToWalletId = walletId)
    }

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

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.parsedAmount <= 0) {
            _uiState.value = state.copy(errorMessage = "Tolong masukkan jumlah nominal yang valid")
            return
        }
        if (state.selectedWalletId == null) {
            _uiState.value = state.copy(errorMessage = "Tolong pilih dompet terlebih dahulu")
            return
        }

        if (state.transactionType == TransactionType.TRANSFER) {
            if (state.selectedToWalletId == null) {
                _uiState.value = state.copy(errorMessage = "Tolong pilih dompet tujuan transfer")
                return
            }
            if (state.selectedWalletId == state.selectedToWalletId) {
                _uiState.value = state.copy(errorMessage = "Dompet asal dan dompet tujuan tidak boleh sama")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val sourceWalletName = state.wallets.find { it.id == state.selectedWalletId }?.name ?: "Dompet"
            val targetWalletName = state.wallets.find { it.id == state.selectedToWalletId }?.name ?: "Dompet"

            val defaultMerchant = if (state.transactionType == TransactionType.TRANSFER) {
                "Transfer $sourceWalletName ke $targetWalletName"
            } else {
                state.selectedCategory.displayName
            }

            val request = CreateTransactionRequest(
                amount = state.parsedAmount,
                type = state.transactionType,
                walletId = state.selectedWalletId,
                toWalletId = if (state.transactionType == TransactionType.TRANSFER) state.selectedToWalletId else null,
                category = state.selectedCategory,
                merchant = state.merchant.ifBlank { defaultMerchant },
                date = state.date,
                notes = state.notes
            )

            transactionRepository.createTransaction(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Failed to save transaction"
                    )
                }
        }
    }
}
