package com.ssajudn.barebudget.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.error.userMessage
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: Transaction? = null,
    val walletName: String? = null,
    val toWalletName: String? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository.getTransactions()
                .onSuccess { transactions ->
                    val found = transactions.find { it.id == transactionId }
                    var wName: String? = null
                    var toWName: String? = null
                    if (found != null) {
                        walletRepository.getWallets().onSuccess { wallets ->
                            if (found.walletId != null) {
                                wName = wallets.find { it.id == found.walletId }?.name
                            }
                            if (found.toWalletId != null) {
                                toWName = wallets.find { it.id == found.toWalletId }?.name
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transaction = found,
                        walletName = wName,
                        toWalletName = toWName
                    )
                }
                .onFailure { error ->
                    val message = (error as? AppException)?.userMessage()
                        ?: (error.localizedMessage ?: "Failed to load transaction")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository.deleteTransaction(transactionId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                }
                .onFailure { error ->
                    val message = (error as? AppException)?.userMessage()
                        ?: (error.localizedMessage ?: "Failed to delete transaction")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
        }
    }
}
