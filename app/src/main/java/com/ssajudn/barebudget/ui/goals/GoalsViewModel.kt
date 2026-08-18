package com.ssajudn.barebudget.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.CreateGoalRequest
import com.ssajudn.barebudget.data.model.Goal
import com.ssajudn.barebudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GoalsUiState {
    object Loading : GoalsUiState
    data class Success(val goals: List<Goal>) : GoalsUiState
    data class Error(val message: String) : GoalsUiState
}

class GoalsViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoalsUiState>(GoalsUiState.Loading)
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is GoalsUiState.Success) {
                _uiState.value = GoalsUiState.Loading
            }

            repository.getGoals()
                .onSuccess { goals ->
                    _uiState.value = GoalsUiState.Success(goals)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _isRefreshing.value = false
                    if (_uiState.value !is GoalsUiState.Success) {
                        _uiState.value = GoalsUiState.Error(error.localizedMessage ?: "Failed to load savings goals")
                    }
                }
        }
    }

    fun addGoal(
        name: String,
        targetAmount: Long,
        targetDate: String = "",
        colorHex: String = "#4E73DF",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val request = CreateGoalRequest(
                name = name,
                targetAmount = targetAmount,
                targetDate = targetDate,
                colorHex = colorHex,
                notes = notes
            )
            repository.createGoal(request)
                .onSuccess {
                    loadGoals()
                }
        }
    }

    fun depositToGoal(id: String, amount: Long) {
        viewModelScope.launch {
            repository.depositToGoal(id, amount)
                .onSuccess {
                    loadGoals()
                }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
                .onSuccess {
                    loadGoals()
                }
        }
    }
}
