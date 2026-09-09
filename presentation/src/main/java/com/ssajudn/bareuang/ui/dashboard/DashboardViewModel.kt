package com.ssajudn.bareuang.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.usecase.GetDashboardSummaryUseCase
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: UiText) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val themePreferences: ThemePreferencesPort
) : ViewModel() {

    val darkMode get() = themePreferences.darkMode

    fun setDarkMode(mode: AppThemeDarkMode) = themePreferences.setDarkMode(mode)

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullToRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }

            getDashboardSummary()
                .onSuccess { summary ->
                    _uiState.value = DashboardUiState.Success(summary)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    android.util.Log.e("Dashboard", "load failed", error)
                    _isRefreshing.value = false
                    if (_uiState.value !is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Error(UiText.Res(R.string.dashboard_load_error_message))
                    }
                }
        }
    }
}
