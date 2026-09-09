package com.ssajudn.bareuang.ui.settings
import androidx.compose.material.icons.filled.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.BackupRestorePort
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.port.TourPreferencesPort
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import com.ssajudn.bareuang.domain.port.LocalDataResetPort
import com.ssajudn.bareuang.domain.port.WidgetPreferencesPort
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import com.ssajudn.bareuang.domain.port.BillReminderPreferencesPort
import com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isLocalDataReset: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val errorText: UiText? = null,
    val successText: UiText? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val onboardingState: OnboardingStatePort,
    private val dataResetter: LocalDataResetPort,
    private val backupManager: BackupRestorePort,
    private val themePrefs: ThemePreferencesPort,
    private val tourPrefs: TourPreferencesPort,
    private val widgetPrefs: WidgetPreferencesPort,
    private val currencyPrefs: CurrencyPreferencesPort,
    private val reminderPrefs: BillReminderPreferencesPort,
    private val reminderScheduler: BillReminderSchedulerPort
) : ViewModel() {

    val darkMode get() = themePrefs.darkMode
    val widgetHideBalance get() = widgetPrefs.hideBalance
    val currency get() = currencyPrefs.currency
    val reminderHour get() = reminderPrefs.reminderHour()
    val reminderMinute get() = reminderPrefs.reminderMinute()

    fun setDarkMode(mode: AppThemeDarkMode) = themePrefs.setDarkMode(mode)

    fun setCurrency(currency: com.ssajudn.bareuang.domain.model.AppCurrency) = currencyPrefs.setCurrency(currency)

    fun setHideBalance(hidden: Boolean) = widgetPrefs.setHideBalance(hidden)

    fun setReminderTime(hour: Int, minute: Int) {
        reminderPrefs.setReminderTime(hour, minute)
        reminderScheduler.scheduleDailyAt(hour, minute)
    }

    fun resetTour() = tourPrefs.resetTour()

    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.exportBackup(uri.toString())
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val ui = UiText.Res(R.string.settings_backup_success_msg)
                _uiState.value = _uiState.value.copy(successMessage = null, successText = ui)
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.settings_backup_success_snack)))
            } else {
                android.util.Log.e("Settings", "export failed", result.exceptionOrNull())
                val ui = UiText.Res(R.string.settings_error_backup)
                _uiState.value = _uiState.value.copy(errorMessage = null, errorText = ui)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }

    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.importBackup(uri.toString())
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                val ui = UiText.Res(R.string.settings_restore_success_msg, listOf(count))
                _uiState.value = _uiState.value.copy(successMessage = null, successText = ui)
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.settings_restore_success_snack)))
            } else {
                android.util.Log.e("Settings", "import failed", result.exceptionOrNull())
                val ui = UiText.Res(R.string.settings_error_restore)
                _uiState.value = _uiState.value.copy(errorMessage = null, errorText = ui)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }


    fun resetLocalData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _operation.value = OperationState.Loading
            try {
                dataResetter.wipe()
            } catch (_: Exception) {
            }
            onboardingState.resetOnboarding()
            _uiState.value = _uiState.value.copy(isLoading = false, isLocalDataReset = true)
            _operation.value = OperationState.Success()
            _effect.send(UiEffect.Navigate("splash"))
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
