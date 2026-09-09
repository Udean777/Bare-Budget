package com.ssajudn.bareuang.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.CreateTransactionUseCase
import com.ssajudn.bareuang.domain.usecase.ValidateTransactionUseCase
import com.ssajudn.bareuang.domain.usecase.TransactionValidationError
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.utils.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
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
    val isRecurring: Boolean = false,
    val recurringInterval: com.ssajudn.bareuang.domain.model.RecurringInterval = com.ssajudn.bareuang.domain.model.RecurringInterval.MONTHLY,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationError: AddTransactionError? = null,
    val isSuccess: Boolean = false,
    val isBudgetMissing: Boolean = false,
    val categoryBudgets: List<com.ssajudn.bareuang.domain.model.CategoryBudget> = emptyList(),
    // Soft daily-budget nudge: when today's allowance is exceeded we prompt the
    // user instead of silently blocking, letting them choose to save anyway.
    val pendingDailyOverride: Boolean = false,
    val pendingDailyMessage: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val budgetRepository: BudgetRepository,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val validateTransaction: ValidateTransactionUseCase
) : ViewModel() {
    private val _operation =
        kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
        loadBudgetStatus()
        observeCategoryBudgets()
    }

    private fun observeCategoryBudgets() {
        viewModelScope.launch {
            budgetRepository.getCategoryBudgets("").collect { list ->
                _uiState.value = _uiState.value.copy(categoryBudgets = list)
            }
        }
    }

    private fun loadBudgetStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBudgetMissing = !hasMonthlyBudget())
        }
    }

    private fun loadWallets() {
        viewModelScope.launch {
            val initial = walletRepository.getWallets().getOrNull()
            if (!initial.isNullOrEmpty()) {
                val defaultWallet = initial.firstOrNull()?.id
                val defaultToWallet = initial.getOrNull(1)?.id ?: defaultWallet
                _uiState.value = _uiState.value.copy(
                    wallets = initial,
                    selectedWalletId = defaultWallet,
                    selectedToWalletId = defaultToWallet
                )
            }
            walletRepository.observeWallets().collect { wallets ->
                if (wallets.isNotEmpty()) {
                    val currentSelected = _uiState.value.selectedWalletId
                    val defaultWallet =
                        if (wallets.any { it.id == currentSelected }) currentSelected else wallets.firstOrNull()?.id
                    val currentSelectedTo = _uiState.value.selectedToWalletId
                    val defaultToWallet =
                        if (wallets.any { it.id == currentSelectedTo }) currentSelectedTo else (wallets.getOrNull(
                            1
                        )?.id ?: defaultWallet)
                    _uiState.value = _uiState.value.copy(
                        wallets = wallets,
                        selectedWalletId = defaultWallet,
                        selectedToWalletId = defaultToWallet
                    )
                }
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
                val alternate =
                    currentState.wallets.firstOrNull { it.id != currentState.selectedWalletId }?.id
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
            val alternateWalletId =
                if (previousSource != null && previousSource != walletId && currentState.wallets.any { it.id == previousSource }) {
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
            val alternateWalletId =
                if (previousDestination != null && previousDestination != walletId && currentState.wallets.any { it.id == previousDestination }) {
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
        _uiState.value = _uiState.value.copy(merchant = merchant.take(100))
    }

    fun onCategoryChange(category: TransactionCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes.take(500))
    }

    fun onRecurringChange(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    fun onRecurringIntervalChange(interval: com.ssajudn.bareuang.domain.model.RecurringInterval) {
        _uiState.value = _uiState.value.copy(recurringInterval = interval)
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.isLoading || _operation.value is OperationState.Loading) return
        val validation = validateTransaction(
            type = state.transactionType,
            amount = state.parsedAmount,
            sourceWalletId = state.selectedWalletId,
            targetWalletId = state.selectedToWalletId,
            wallets = state.wallets
        )
        if (validation != null) {
            val error = validation.toUiError()
            val sourceBalance =
                state.wallets.firstOrNull { it.id == state.selectedWalletId }?.balance
            val ui = error.toUiText(
                if (error == AddTransactionError.INSUFFICIENT_BALANCE) sourceBalance?.let(
                    CurrencyFormatter::formatRupiah
                ) else null
            )
            _uiState.value = state.copy(errorMessage = null, validationError = error)
            _operation.value = OperationState.Error("", ui)
            _effect.trySend(UiEffect.ShowSnackbarRes(ui))
            return
        }

        viewModelScope.launch {
            _uiState.value =
                state.copy(isLoading = true, errorMessage = null, validationError = null)
            _operation.value = OperationState.Loading

            if (state.transactionType != TransactionType.TRANSFER && !hasMonthlyBudget()) {
                val e = AddTransactionError.BUDGET_REQUIRED
                val ui = e.toUiText()
                _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = null, validationError = e)
                _operation.value = OperationState.Error("", ui)
                _effect.trySend(UiEffect.ShowSnackbarRes(ui))
                _effect.trySend(UiEffect.Navigate(com.ssajudn.bareuang.ui.navigation.Screen.Budget.route))
                return@launch
            }

            if (state.transactionType == TransactionType.EXPENSE) {
                val dailyCheck = checkDailyBudget(
                    state.parsedAmount,
                    state.date,
                    CurrencyFormatter.getActiveCurrency(),
                    state.selectedCategory
                )
                if (dailyCheck.isFailure) {
                    val msg = dailyCheck.exceptionOrNull()?.message ?: ""
                    // Soft nudge: prompt the user for confirmation instead of blocking outright.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = msg,
                        pendingDailyOverride = true,
                        pendingDailyMessage = msg.ifBlank { null }
                    )
                    return@launch
                }
            }

            performCreate()
        }
    }

    /** Proceed after the user accepts a daily-budget override prompt. */
    fun confirmDailyOverride() {
        if (_uiState.value.isLoading) return
        _uiState.value =
            _uiState.value.copy(pendingDailyOverride = false, pendingDailyMessage = null)
        viewModelScope.launch { performCreate() }
    }

    /** Cancel a daily-budget override prompt. */
    fun dismissDailyOverride() {
        _uiState.value = _uiState.value.copy(
            pendingDailyOverride = false,
            pendingDailyMessage = null,
            isLoading = false,
            errorMessage = null,
            validationError = null
        )
        _operation.value = OperationState.Idle
    }

    private suspend fun performCreate() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true)
        _operation.value = OperationState.Loading

        val sourceWalletName = state.wallets.find { it.id == state.selectedWalletId }?.name ?: ""
        val targetWalletName = state.wallets.find { it.id == state.selectedToWalletId }?.name ?: ""

        val defaultMerchant = if (state.transactionType == TransactionType.TRANSFER) {
            if (sourceWalletName.isNotBlank() && targetWalletName.isNotBlank()) "$sourceWalletName \u2192 $targetWalletName" else state.selectedCategory.name
        } else {
            state.selectedCategory.name
        }

        val request = CreateTransactionRequest(
            amount = state.parsedAmount,
            type = state.transactionType,
            walletId = state.selectedWalletId,
            toWalletId = if (state.transactionType == TransactionType.TRANSFER) state.selectedToWalletId else null,
            category = state.selectedCategory,
            merchant = state.merchant.ifBlank { defaultMerchant },
            date = state.date,
            notes = state.notes,
            recurringInterval = if (state.isRecurring) state.recurringInterval else com.ssajudn.bareuang.domain.model.RecurringInterval.NONE
        )

        createTransaction(request)
            .onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _operation.value = OperationState.Success()
            }
            .onFailure { error ->
                android.util.Log.e("AddTx", "save failed", error)
                val ui = (error as? AppException)?.toUiText()
                    ?: UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "",
                    validationError = AddTransactionError.SAVE_FAILED
                )
                _operation.value = OperationState.Error(error.message ?: "", ui)
                _effect.trySend(UiEffect.ShowSnackbarRes(ui))
            }
    }
}
