package com.ssajudn.bareuang.ui.common

sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect {
        companion object { fun from(uiText: UiText, fallback: String = "") = ShowSnackbar(when(uiText){ is UiText.Dyn -> uiText.message; is UiText.Res, is UiText.PluralRes -> fallback }) }
    }
    data class ShowSnackbarRes(val uiText: UiText) : UiEffect
    data class Navigate(val route: String) : UiEffect
    object PopBackStack : UiEffect
}

sealed interface OperationState {
    object Idle : OperationState; object Loading : OperationState
    data class Success(val msg: String? = null) : OperationState
    data class Error(val message: String, val uiText: UiText = UiText.Dyn(message)) : OperationState {
        companion object { fun from(uiText: UiText, fallback: String = "") = Error(when(uiText){ is UiText.Dyn -> uiText.message; is UiText.Res, is UiText.PluralRes -> fallback }, uiText) }
    }
}
