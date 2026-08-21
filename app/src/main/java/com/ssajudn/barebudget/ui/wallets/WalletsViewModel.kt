package com.ssajudn.barebudget.ui.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
            repository.createWallet(CreateWalletRequest(name, startingBalance, colorHex, "account_balance_wallet"))
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch { repository.deleteWallet(id) }
    }
}
