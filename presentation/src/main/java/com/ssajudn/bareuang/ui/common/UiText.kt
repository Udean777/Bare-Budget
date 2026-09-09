package com.ssajudn.bareuang.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Dyn(val message: String) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())
    is UiText.Dyn -> message
}
fun UiText.asString(context: android.content.Context): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) context.getString(id) else context.getString(id, *args.toTypedArray())
    is UiText.Dyn -> message
}

fun UiText.resolveFallback(fallback: String = ""): String = when(this){
    is UiText.Dyn -> message
    is UiText.Res -> fallback
}

fun String.toUiText(): UiText = UiText.Dyn(this)

/** Maps AppException to UiText with i18n stringRes — never leak raw system message, per-feature granular */
fun com.ssajudn.bareuang.domain.error.AppException.toUiText(): UiText = when(this){
    is com.ssajudn.bareuang.domain.error.AppException.NetworkException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_network)
    is com.ssajudn.bareuang.domain.error.AppException.AuthException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_auth)
    is com.ssajudn.bareuang.domain.error.AppException.DataException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.SyncException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.UnknownError -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.DailyBudgetExceededException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_daily_exceeded)
}
