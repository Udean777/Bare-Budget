package com.ssajudn.bareuang.domain.model

data class Wallet(
    val id: String? = null,
    val name: String,
    val balance: Long = 0L,
    val colorHex: String = "#4E73DF",
    val iconName: String = "account_balance_wallet",
    val createdAt: String? = null
)

data class CreateWalletRequest(
    val name: String,
    val balance: Long = 0L,
    val colorHex: String = "#4E73DF",
    val iconName: String = "account_balance_wallet"
)
