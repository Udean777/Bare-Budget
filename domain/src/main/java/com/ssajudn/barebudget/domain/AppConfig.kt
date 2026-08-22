package com.ssajudn.barebudget.domain

/**
 * Centralized magic strings — Clean Code: named constants.
 * Used by DomainMappers, LocalWalletRepository, Goal logic, etc.
 */
object AppConfig {
    const val DEFAULT_WALLET_NAME = "Uang Tunai"
    const val DEFAULT_WALLET_COLOR = "#2ECC71"
    const val DEFAULT_WALLET_ICON = "account_balance_wallet"
    const val DEFAULT_GOAL_COLOR = "#4E73DF"
    const val GUEST_PREFIX = "guest_"
    const val GUEST_EMAIL = "guest@barebudget.app"
}
