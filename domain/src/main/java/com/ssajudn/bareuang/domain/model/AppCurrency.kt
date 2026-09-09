package com.ssajudn.bareuang.domain.model

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val prefix: String,
    val thousandSeparator: Char
) {
    IDR(
        code = "IDR",
        symbol = "Rp",
        prefix = "Rp ",
        thousandSeparator = '.'
    ),
    USD(
        code = "USD",
        symbol = "$",
        prefix = "$ ",
        thousandSeparator = ','
    );

    companion object {
        val DEFAULT = IDR

        fun fromCode(code: String?): AppCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
