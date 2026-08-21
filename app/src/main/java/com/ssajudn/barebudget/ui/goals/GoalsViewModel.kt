package com.ssajudn.barebudget.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.repository.GoalRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.model.UpdateGoalRequest

sealed interface GoalsUiState {
    object Loading : GoalsUiState
    data class Success(val goals: List<Goal>) : GoalsUiState
    data class Error(val message: String) : GoalsUiState
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = repository.observeGoals()
        .map<List<Goal>, GoalsUiState> { GoalsUiState.Success(it) }
        .catch { e -> emit(GoalsUiState.Error(e.message ?: "Failed to load savings goals")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState.Loading)

    val wallets: StateFlow<List<Wallet>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun loadWallets() {
        viewModelScope.launch { walletRepository.getWallets() }
    }

    fun loadGoals(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getGoals()
            walletRepository.getWallets()
            _isRefreshing.value = false
        }
    }

    fun addGoal(name: String, targetAmount: Long, targetDate: String = "", colorHex: String = "#4E73DF", notes: String = "") {
        viewModelScope.launch {
            repository.createGoal(CreateGoalRequest(name, targetAmount, targetDate, colorHex, notes))
        }
    }

    fun updateGoal(id: String, name: String, targetAmount: Long, targetDate: String = "", colorHex: String = "#4E73DF", notes: String = "") {
        viewModelScope.launch {
            repository.updateGoal(id, UpdateGoalRequest(name, targetAmount, targetDate, colorHex, notes))
        }
    }

    fun depositToGoal(id: String, amount: Long, walletId: String) {
        viewModelScope.launch { repository.depositToGoal(id, amount, walletId) }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { repository.deleteGoal(id) }
    }
}
