package com.ssajudn.barebudget.ui.common

sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect
    data class Navigate(val route: String) : UiEffect
    object PopBackStack : UiEffect
}

sealed interface OperationState { object Idle : OperationState; object Loading : OperationState; data class Success(val msg: String? = null) : OperationState; data class Error(val message: String) : OperationState }
