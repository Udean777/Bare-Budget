package com.ssajudn.bareuang.ui.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.OcrConsentPort
import com.ssajudn.bareuang.domain.port.ReceiptAiPort
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.domain.port.NetworkMonitorPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val isProcessing: Boolean = false,
    val rawText: String? = null,
    // editable fields
    val merchant: String = "",
    val amount: String = "", // digits only
    val parsedAmount: Long = 0L,
    val category: TransactionCategory = TransactionCategory.SHOPPING,
    val date: String = DateUtils.getCurrentDateISO(),
    val isSaving: Boolean = false,
    val pendingDailyOverride: Boolean = false,
    val pendingDailyMessage: String? = null,
    val isOnline: Boolean = true,
    val hasOcrConsent: Boolean = false,
    val showOcrConsent: Boolean = false,
)

@HiltViewModel
class OcrScanViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val receiptAiService: ReceiptAiPort,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase,
    private val networkMonitor: NetworkMonitorPort,
    private val ocrConsentPreferences: OcrConsentPort,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OcrUiState(
            isOnline = networkMonitor.isOnline(),
            hasOcrConsent = ocrConsentPreferences.hasCurrentConsent,
        )
    )
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            networkMonitor.observeIsOnline().collect { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }
        viewModelScope.launch {
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                wallets = wallets,
                selectedWalletId = wallets.firstOrNull()?.id
            )
            walletRepository.observeWallets().collect { list ->
                _uiState.value = _uiState.value.copy(wallets = list)
            }
        }
    }

    fun onWalletSelected(id: String) { _uiState.value = _uiState.value.copy(selectedWalletId = id) }
    fun onMerchantChange(v: String) { _uiState.value = _uiState.value.copy(merchant = v.take(100)) }
    fun onCategoryChange(c: TransactionCategory) { _uiState.value = _uiState.value.copy(category = c) }
    fun onDateChange(d: String) { _uiState.value = _uiState.value.copy(date = d) }
    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }.take(12)
        _uiState.value = _uiState.value.copy(amount = digits, parsedAmount = digits.toLongOrNull() ?: 0L)
    }

    fun requestOcrConsent() {
        if (!ocrConsentPreferences.hasCurrentConsent) {
            _uiState.value = _uiState.value.copy(showOcrConsent = true)
        } else {
            _uiState.value = _uiState.value.copy(hasOcrConsent = true)
        }
    }

    fun acceptOcrConsent() {
        ocrConsentPreferences.grantCurrentConsent()
        _uiState.value = _uiState.value.copy(hasOcrConsent = true, showOcrConsent = false)
    }

    fun dismissOcrConsent() {
        _uiState.value = _uiState.value.copy(showOcrConsent = false)
    }

    fun processImage(uri: Uri) {
        // Defense in depth: picker/camera callbacks must not upload without consent.
        if (!ocrConsentPreferences.hasCurrentConsent) {
            _uiState.value = _uiState.value.copy(hasOcrConsent = false, showOcrConsent = true)
            return
        }
        if (!networkMonitor.isOnline()) {
            viewModelScope.launch {
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_no_internet)))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, rawText = null)
            val result = receiptAiService.parseReceiptImage(uri.toString())
            result.onSuccess { ai ->
                val cat = runCatching { TransactionCategory.valueOf(ai.category) }.getOrDefault(TransactionCategory.SHOPPING)
                val aiDate = ai.date.takeIf {
                    runCatching { java.time.LocalDate.parse(it) }.isSuccess
                } ?: _uiState.value.date
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    rawText = ai.rawText.ifBlank { ai.items.joinToString("\n") },
                    merchant = ai.merchant,
                    amount = if (ai.total > 0) ai.total.toString() else "",
                    parsedAmount = ai.total,
                    category = cat,
                    date = aiDate,
                )
            }.onFailure { e ->
                android.util.Log.e("Ocr", "AI parse failed", e)
                _uiState.value = _uiState.value.copy(isProcessing = false)
                val res = when (e) {
                    is com.ssajudn.bareuang.domain.error.AppException.NetworkException ->
                        UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_no_internet)
                    is com.ssajudn.bareuang.domain.error.AppException ->
                        UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_generic)
                    else -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_generic)
                }
                _effect.send(UiEffect.ShowSnackbarRes(res))
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.selectedWalletId.isNullOrBlank()) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required))) }; return
        }
        if (s.parsedAmount <= 0) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_invalid_amount))) }; return
        }
        viewModelScope.launch {
            if (!hasMonthlyBudget()) {
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_budget_required)))
                return@launch
            }
            val dailyCheck = checkDailyBudget(s.parsedAmount, s.date, com.ssajudn.bareuang.utils.CurrencyFormatter.getActiveCurrency(), s.category)
            if (dailyCheck.isFailure) {
                val msg = dailyCheck.exceptionOrNull()?.message ?: ""
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    pendingDailyOverride = true,
                    pendingDailyMessage = msg.ifBlank { null }
                )
                return@launch
            }
            performCreate(onSuccess)
        }
    }

    fun confirmDailyOverride(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, pendingDailyMessage = null)
        viewModelScope.launch { performCreate(onSuccess) }
    }

    fun dismissDailyOverride() {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, pendingDailyMessage = null, isSaving = false)
    }

    private suspend fun performCreate(onSuccess: () -> Unit) {
        val s = _uiState.value
        val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == s.selectedWalletId }
        if (wallet != null && wallet.balance < s.parsedAmount) {
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_insufficient_balance, listOf(com.ssajudn.bareuang.utils.CurrencyFormatter.formatRupiah(wallet.balance)))))
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true)
        val req = CreateTransactionRequest(
            amount = s.parsedAmount,
            type = TransactionType.EXPENSE,
            category = s.category,
            merchant = s.merchant.ifBlank { s.category.name },
            date = s.date,
            walletId = s.selectedWalletId
        )
        val res = transactionRepository.createTransaction(req)
        res.onSuccess {
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_save_success)))
            onSuccess()
        }.onFailure { e ->
            android.util.Log.e("Ocr", "save failed", e)
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_save)))
        }
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(rawText = null, merchant = "", amount = "", parsedAmount = 0L)
    }
}
