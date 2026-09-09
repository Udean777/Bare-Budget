package com.ssajudn.bareuang.ui.transaction

import androidx.annotation.StringRes
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.domain.usecase.TransactionValidationError

enum class AddTransactionError(@StringRes val resId: Int) {
    INVALID_AMOUNT(R.string.tx_error_invalid_amount),
    WALLET_REQUIRED(R.string.tx_error_wallet_required),
    TO_WALLET_REQUIRED(R.string.tx_error_to_wallet_required),
    SAME_WALLET(R.string.tx_error_same_wallet),
    INSUFFICIENT_BALANCE(R.string.tx_error_insufficient_balance),
    BUDGET_REQUIRED(R.string.tx_error_budget_required),
    DAILY_BUDGET_EXCEEDED(R.string.tx_error_daily_exceeded),
    SAVE_FAILED(R.string.tx_error_save_failed)
}

fun AddTransactionError.toUiText(argument: String? = null): UiText =
    if (this == AddTransactionError.INSUFFICIENT_BALANCE) {
        UiText.Res(resId, listOf(argument.orEmpty()))
    } else {
        UiText.Res(resId)
    }

fun TransactionValidationError.toUiError(): AddTransactionError = when (this) {
    TransactionValidationError.INVALID_AMOUNT -> AddTransactionError.INVALID_AMOUNT
    TransactionValidationError.WALLET_REQUIRED -> AddTransactionError.WALLET_REQUIRED
    TransactionValidationError.TO_WALLET_REQUIRED -> AddTransactionError.TO_WALLET_REQUIRED
    TransactionValidationError.SAME_WALLET -> AddTransactionError.SAME_WALLET
    TransactionValidationError.INSUFFICIENT_BALANCE -> AddTransactionError.INSUFFICIENT_BALANCE
}
