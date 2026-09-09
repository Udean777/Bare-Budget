package com.ssajudn.bareuang.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
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
    private val _operation =
        MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


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
                    android.util.Log.e("TxDetail", "load failed", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _operation.value = OperationState.Loading
            transactionRepository.deleteTransaction(transactionId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
                    _operation.value = OperationState.Success()
                    viewModelScope.launch { _effect.send(UiEffect.PopBackStack) }
                }
                .onFailure { error ->
                    android.util.Log.e("TxDetail", "delete failed", error)
                    val ui = UiText.Res(R.string.error_generic)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    _operation.value = OperationState.Error("", ui)
                    viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(ui)) }
                }
        }
    }
}
