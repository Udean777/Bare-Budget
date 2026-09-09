package com.ssajudn.bareuang.ui.common

import androidx.annotation.StringRes
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.presentation.R

@StringRes
fun TransactionCategory.labelRes(): Int = when (this) {
    TransactionCategory.FOOD -> R.string.category_food
    TransactionCategory.TRANSPORT -> R.string.category_transport
    TransactionCategory.BILLS -> R.string.category_bills
    TransactionCategory.SHOPPING -> R.string.category_shopping
    TransactionCategory.ENTERTAINMENT -> R.string.category_entertainment
    TransactionCategory.SOCIAL -> R.string.category_social
    TransactionCategory.SALARY -> R.string.category_salary
    TransactionCategory.BONUS -> R.string.category_bonus
    TransactionCategory.INVESTMENT -> R.string.category_investment
    TransactionCategory.TRANSFER -> R.string.category_transfer
    TransactionCategory.OTHER -> R.string.category_other
}

@StringRes
fun RecurringInterval.labelRes(): Int = when (this) {
    RecurringInterval.NONE -> R.string.tx_recurring_once
    RecurringInterval.WEEKLY -> R.string.tx_recurring_weekly
    RecurringInterval.MONTHLY -> R.string.tx_recurring_monthly
    RecurringInterval.YEARLY -> R.string.tx_recurring_yearly
}

@StringRes
fun AppCurrency.labelRes(): Int = when (this) {
    AppCurrency.IDR -> R.string.currency_idr
    AppCurrency.USD -> R.string.currency_usd
}
