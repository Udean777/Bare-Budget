package com.ssajudn.barebudget.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val indonesianLocale = Locale("id", "ID")
    private val rupiahFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    /**
     * Format number to Rupiah string: e.g. 50000 -> "Rp 50.000"
     */
    fun formatRupiah(amount: Long): String {
        return rupiahFormat.format(amount)
            .replace("Rp", "Rp ")
            .trim()
    }

    /**
     * Parse raw currency input string (e.g. "Rp 50.000" or "50,000" or "50000") to Long
     */
    fun parseAmount(input: String): Long {
        val cleanString = input.replace(Regex("[^0-9]"), "")
        return cleanString.toLongOrNull() ?: 0L
    }

    /**
     * Compact format for large numbers (e.g. 1.500.000 -> "1.5jt", 50.000 -> "50rb")
     */
    fun formatCompact(amount: Long): String {
        return when {
            amount >= 1_000_000_000 -> String.format(indonesianLocale, "%.1f M", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(indonesianLocale, "%.1f jt", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(indonesianLocale, "%.0f rb", amount / 1_000.0)
            else -> amount.toString()
        }
    }
}
