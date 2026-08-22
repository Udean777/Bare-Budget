package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.utils.DateUtils
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
    val isSuccess: Boolean = false,
    val isBudgetMissing: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
        loadBudgetStatus()
    }

    // ponytail: snapshot untuk banner saja; save selalu cek ulang langsung ke repository
    private fun loadBudgetStatus() {
        viewModelScope.launch {
            val budget = budgetRepository.getMonthlyBudget("")
            _uiState.value = _uiState.value.copy(isBudgetMissing = budget.getOrDefault(0L) <= 0L)
        }
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
        val currentState = _uiState.value
        var targetWalletId = currentState.selectedToWalletId

        // When switching to transfer, ensure destination is not identical to source if multiple wallets exist
        if (type == TransactionType.TRANSFER && currentState.selectedWalletId != null) {
            if (targetWalletId == null || targetWalletId == currentState.selectedWalletId) {
                val alternate = currentState.wallets.firstOrNull { it.id != currentState.selectedWalletId }?.id
                if (alternate != null) {
                    targetWalletId = alternate
                }
            }
        }

        _uiState.value = currentState.copy(
            transactionType = type,
            selectedCategory = newCategory,
            selectedToWalletId = targetWalletId
        )
    }

    fun onWalletChange(walletId: String) {
        val currentState = _uiState.value
        var newToWalletId = currentState.selectedToWalletId

        // Smart switch: if selected source matches destination in transfer mode, switch destination
        if (currentState.transactionType == TransactionType.TRANSFER && walletId == currentState.selectedToWalletId) {
            val previousSource = currentState.selectedWalletId
            val alternateWalletId = if (previousSource != null && previousSource != walletId && currentState.wallets.any { it.id == previousSource }) {
                previousSource
            } else {
                currentState.wallets.firstOrNull { it.id != walletId }?.id
            }
            if (alternateWalletId != null) {
                newToWalletId = alternateWalletId
            }
        }

        _uiState.value = currentState.copy(
            selectedWalletId = walletId,
            selectedToWalletId = newToWalletId
        )
    }

    fun onToWalletChange(walletId: String) {
        val currentState = _uiState.value
        var newSourceWalletId = currentState.selectedWalletId

        // Smart switch: if selected destination matches source in transfer mode, switch source
        if (walletId == currentState.selectedWalletId) {
            val previousDestination = currentState.selectedToWalletId
            val alternateWalletId = if (previousDestination != null && previousDestination != walletId && currentState.wallets.any { it.id == previousDestination }) {
                previousDestination
            } else {
                currentState.wallets.firstOrNull { it.id != walletId }?.id
            }
            if (alternateWalletId != null) {
                newSourceWalletId = alternateWalletId
            }
        }

        _uiState.value = currentState.copy(
            selectedWalletId = newSourceWalletId,
            selectedToWalletId = walletId
        )
    }

    fun swapWallets() {
        val currentState = _uiState.value
        val source = currentState.selectedWalletId
        val target = currentState.selectedToWalletId
        if (source != null && target != null && source != target) {
            _uiState.value = currentState.copy(
                selectedWalletId = target,
                selectedToWalletId = source
            )
        }
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
            _operation.value = OperationState.Loading

            // Gerbang budget: income/expense wajib punya budget bulan berjalan; transfer bebas
            if (state.transactionType != TransactionType.TRANSFER) {
                val monthlyBudget = budgetRepository.getMonthlyBudget("").getOrDefault(0L)
                if (monthlyBudget <= 0L) {
                    val msg = "Setup anggaran bulan ini terlebih dahulu"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                    _operation.value = OperationState.Error(msg)
                    _effect.send(UiEffect.ShowSnackbar(msg))
                    _effect.send(UiEffect.Navigate(com.ssajudn.barebudget.ui.navigation.Screen.Budget.route))
                    return@launch
                }
            }

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
                    _operation.value = OperationState.Success()
                    viewModelScope.launch { _effect.send(UiEffect.PopBackStack) }
                }
                .onFailure { error ->
                    val msg = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Failed to save transaction"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                    _operation.value = OperationState.Error(msg)
                    viewModelScope.launch { _effect.send(UiEffect.ShowSnackbar(msg)) }
                }
        }
    }
}
