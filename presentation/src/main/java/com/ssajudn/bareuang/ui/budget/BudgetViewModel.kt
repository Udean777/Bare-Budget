package com.ssajudn.bareuang.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import javax.inject.Inject

enum class BudgetError(val resId: Int) {
    LOCKED(R.string.budget_error_locked), INVALID_AMOUNT(R.string.budget_error_invalid),
    SET_FAILED(R.string.budget_error_set_failed), CATEGORY_SET(R.string.budget_error_category_set),
    CATEGORY_DELETE(R.string.budget_error_category_delete)
}
data class BudgetUiState(
    val currentLimit: Long = 0L,
    val rawAmount: String = "",
    val parsedAmount: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: UiText? = null,
    val categoryBudgets: List<com.ssajudn.bareuang.domain.model.CategoryBudget> = emptyList(),
    val isCustomDailyTarget: Boolean = false,
    val dailyTargetInput: String = "",
    val isDailyTargetSaved: Boolean = false
) {
    val isLocked: Boolean get() = currentLimit > 0
    val totalAllocatedCategory: Long get() = categoryBudgets.sumOf { it.limitAmount }
    val isOverAllocated: Boolean get() = currentLimit > 0 && totalAllocatedCategory > currentLimit
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val dailyPacingPreferences: DailyPacingPreferencesPort
) : ViewModel() {
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBudget()
        observeCategoryBudgets()
        observeDailyTarget()
    }

    private fun observeDailyTarget() {
        viewModelScope.launch {
            dailyPacingPreferences.customTarget.collect { target ->
                _uiState.value = _uiState.value.copy(
                    isCustomDailyTarget = target != null,
                    dailyTargetInput = target?.toString() ?: dailyPacingPreferences.lastCustomTarget.value?.toString().orEmpty(),
                    isDailyTargetSaved = target != null
                )
            }
        }
    }

    fun onDailyTargetChange(input: String) {
        _uiState.value = _uiState.value.copy(
            dailyTargetInput = input.filter { it.isDigit() }.take(12),
            isDailyTargetSaved = false,
        )
    }

    fun setAutomaticDailyTarget() {
        _uiState.value = _uiState.value.copy(
            isCustomDailyTarget = false,
            isDailyTargetSaved = false,
        )
        dailyPacingPreferences.setCustomTarget(null)
    }

    fun selectCustomDailyTarget() {
        val saved = dailyPacingPreferences.lastCustomTarget.value
        if (saved != null) dailyPacingPreferences.setCustomTarget(saved)
        _uiState.value = _uiState.value.copy(
            isCustomDailyTarget = true,
            dailyTargetInput = saved?.toString().orEmpty(),
            isDailyTargetSaved = saved != null,
        )
    }

    fun editCustomDailyTarget() {
        _uiState.value = _uiState.value.copy(isDailyTargetSaved = false)
    }

    fun saveCustomDailyTarget() {
        val amount = _uiState.value.dailyTargetInput.toLongOrNull()
        if (amount == null || amount <= 0L) {
            _uiState.value = _uiState.value.copy(error = UiText.Res(BudgetError.INVALID_AMOUNT.resId))
            return
        }
        dailyPacingPreferences.setCustomTarget(amount)
        _uiState.value = _uiState.value.copy(isDailyTargetSaved = true)
        viewModelScope.launch {
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(R.string.budget_daily_target_saved)))
        }
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMonthlyBudget()
                .onSuccess { existing ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLimit = existing,
                        rawAmount = if (existing > 0) existing.toString() else "",
                        parsedAmount = existing
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    private fun observeCategoryBudgets() {
        viewModelScope.launch {
            repository.getCategoryBudgets().collect { list ->
                _uiState.value = _uiState.value.copy(categoryBudgets = list)
            }
        }
    }

    fun onAmountChange(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(12)
        val parsed = digitsOnly.toLongOrNull() ?: 0L
        _uiState.value = _uiState.value.copy(
            rawAmount = digitsOnly,
            parsedAmount = parsed
        )
    }

    fun saveBudget() {
        val state = _uiState.value
        if (state.isLoading || _operation.value is OperationState.Loading) return
        if (state.isLocked) {
            val ui = UiText.Res(BudgetError.LOCKED.resId)
            _uiState.value = state.copy(error = ui)
            return
        }
        if (state.parsedAmount <= 0) {
            val ui = UiText.Res(BudgetError.INVALID_AMOUNT.resId)
            _uiState.value = state.copy(error = ui)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            _operation.value = OperationState.Loading
            repository.setBudget(state.parsedAmount)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    _operation.value = OperationState.Success()
                    _effect.trySend(UiEffect.PopBackStack)
                }
                .onFailure { error ->
                    android.util.Log.e("Budget", "setBudget failed", error)
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.SET_FAILED.resId)
                    _uiState.value = _uiState.value.copy(isLoading = false, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.trySend(UiEffect.ShowSnackbarRes(ui))
                }
        }
    }

    fun setCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory, limit: Long) {
        if (limit <= 0) return
        viewModelScope.launch {
            repository.setCategoryBudget(category, limit)
                .onFailure { error ->
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.CATEGORY_SET.resId)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                }
        }
    }

    fun deleteCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory) {
        viewModelScope.launch {
            repository.deleteCategoryBudget(category)
                .onFailure { error ->
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.CATEGORY_DELETE.resId)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                }
        }
    }
}
