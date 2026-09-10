package com.ssajudn.bareuang.domain.utils

import com.ssajudn.bareuang.domain.model.AppCurrency
import java.text.NumberFormat
import java.util.Locale

object DomainCurrencyFormatter {
    private val idLocale = Locale.forLanguageTag("id-ID")
    private val usLocale = Locale.US

    private val rupiahFormat = NumberFormat.getCurrencyInstance(idLocale).apply {
        maximumFractionDigits = 0; minimumFractionDigits = 0
    }
    private val dollarFormat = NumberFormat.getCurrencyInstance(usLocale).apply {
        maximumFractionDigits = 0; minimumFractionDigits = 0
    }

    fun format(amount: Long, currency: AppCurrency = AppCurrency.IDR): String = when (currency) {
        AppCurrency.IDR -> synchronized(rupiahFormat) { rupiahFormat.format(amount) }.replace("Rp", "Rp ").trim()
        AppCurrency.USD -> synchronized(dollarFormat) { dollarFormat.format(amount) }.replace("$", "$ ").trim()
    }

    fun formatCompact(amount: Long, currency: AppCurrency = AppCurrency.IDR): String = when (currency) {
        AppCurrency.IDR -> when {
            amount >= 1_000_000_000 -> String.format(idLocale, "%.1f M", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(idLocale, "%.1f jt", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(idLocale, "%.0f rb", amount / 1_000.0)
            else -> amount.toString()
        }
        AppCurrency.USD -> when {
            amount >= 1_000_000_000 -> String.format(usLocale, "%.1f B", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(usLocale, "%.1f M", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(usLocale, "%.0f K", amount / 1_000.0)
            else -> amount.toString()
        }
    }
}
