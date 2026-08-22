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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
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
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.observeGoals(),
        _searchQuery
    ) { goals, query ->
        var filtered = goals
        if (query.isNotBlank()) {
            filtered = filtered.filter { goal ->
                goal.name.contains(query, ignoreCase = true) ||
                    (goal.notes?.contains(query, ignoreCase = true) == true)
            }
        }
        GoalsUiState.Success(filtered) as GoalsUiState
    }.catch { e -> emit(GoalsUiState.Error(e.message ?: "Failed to load savings goals")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState.Loading)

    val wallets: StateFlow<List<Wallet>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

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
            _operation.value = OperationState.Loading
            val r = repository.createGoal(CreateGoalRequest(name, targetAmount, targetDate, colorHex, notes))
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error(r.exceptionOrNull()?.message ?: "Gagal")
            if (r.isSuccess) _effect.send(UiEffect.PopBackStack) else _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }

    fun updateGoal(id: String, name: String, targetAmount: Long, targetDate: String = "", colorHex: String = "#4E73DF", notes: String = "") {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = repository.updateGoal(id, UpdateGoalRequest(name, targetAmount, targetDate, colorHex, notes))
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error(r.exceptionOrNull()?.message ?: "Gagal")
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }

    fun depositToGoal(id: String, amount: Long, walletId: String) {
        viewModelScope.launch {
            _operation.value = OperationState.Loading
            val r = repository.depositToGoal(id, amount, walletId)
            _operation.value = if (r.isSuccess) OperationState.Success() else OperationState.Error(r.exceptionOrNull()?.message ?: "Gagal")
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            val r = repository.deleteGoal(id)
            if (r.isFailure) _effect.send(UiEffect.ShowSnackbar(r.exceptionOrNull()?.message ?: "Gagal"))
        }
    }
}
