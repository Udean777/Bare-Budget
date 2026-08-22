package com.ssajudn.barebudget.ui.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import javax.inject.Inject

data class WalletsUiState(
    val wallets: List<Wallet> = emptyList(),
    val netWorth: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletsViewModel @Inject constructor(
    private val repository: WalletRepository
) : ViewModel() {
    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    val uiState: StateFlow<WalletsUiState> = repository.observeWallets()
        .map { wallets -> WalletsUiState(wallets = wallets, netWorth = wallets.sumOf { it.balance }) }
        .catch { e -> emit(WalletsUiState(error = e.message ?: "Gagal memuat dompet")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletsUiState(isLoading = true))

    // ponytail: one-shot for default-wallet creation + remote refresh (Flow is source of truth)
    init {
        viewModelScope.launch { repository.getWallets() }
    }

    fun loadWallets() {
        viewModelScope.launch { repository.getWallets() }
    }

    fun addWallet(name: String, startingBalance: Long, colorHex: String) {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = repository.createWallet(CreateWalletRequest(name, startingBalance, colorHex, "account_balance_wallet"))
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error(r.exceptionOrNull()?.message ?: "Gagal")
            if (r.isSuccess) _effect.send(UiEffect.PopBackStack) else _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch {
            val r = repository.deleteWallet(id)
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }
}
