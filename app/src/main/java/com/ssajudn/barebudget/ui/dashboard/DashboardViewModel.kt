package com.ssajudn.barebudget.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.model.DashboardSummary
import com.ssajudn.barebudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(
    private val repository: BudgetRepository = BudgetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            // Only show full screen loading if there's no data yet (initial load)
            // This prevents UI flickering / blinking on screen resume
            if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }
            repository.getDashboardSummary()
                .onSuccess { summary ->
                    _uiState.value = DashboardUiState.Success(summary)
                }
                .onFailure { error ->
                    if (_uiState.value !is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Error(error.localizedMessage ?: "Failed to load dashboard data")
                    }
                }
        }
    }

    fun updateBudget(newBudget: Long) {
        viewModelScope.launch {
            repository.setBudget(newBudget)
                .onSuccess {
                    loadDashboardData()
                }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
                .onSuccess {
                    loadDashboardData()
                }
        }
    }
}
