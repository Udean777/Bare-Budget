package com.ssajudn.barebudget.ui.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CreateWalletRequest
import com.ssajudn.barebudget.data.model.Wallet
import com.ssajudn.barebudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletsUiState(
    val wallets: List<Wallet> = emptyList(),
    val netWorth: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WalletsViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletsUiState())
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getWallets()
            if (result.isSuccess) {
                val wallets = result.getOrNull() ?: emptyList()
                val netWorth = wallets.sumOf { it.balance }
                _uiState.value = _uiState.value.copy(
                    wallets = wallets,
                    netWorth = netWorth,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Gagal memuat dompet"
                )
            }
        }
    }

    fun addWallet(name: String, startingBalance: Long, colorHex: String) {
        viewModelScope.launch {
            val req = CreateWalletRequest(
                name = name,
                balance = startingBalance,
                colorHex = colorHex,
                iconName = "account_balance_wallet"
            )
            repository.createWallet(req)
            loadWallets()
        }
    }

    fun deleteWallet(id: String) {
        viewModelScope.launch {
            repository.deleteWallet(id)
            loadWallets()
        }
    }
}
