package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.domain.model.RecurringInterval
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType

object DomainMappers {
    fun safeTransactionType(raw: String?): TransactionType =
        runCatching { TransactionType.valueOf(raw.orEmpty()) }
            .onFailure { println("[DomainMappers] Unknown TransactionType: $raw, fallback to EXPENSE: ${it.message}") }
            .getOrDefault(TransactionType.EXPENSE)

    fun safeCategory(raw: String?): TransactionCategory =
        runCatching { TransactionCategory.valueOf(raw.orEmpty()) }
            .onFailure { println("[DomainMappers] Unknown TransactionCategory: $raw, fallback to OTHER: ${it.message}") }
            .getOrDefault(TransactionCategory.OTHER)

    fun safeRecurringInterval(raw: String?): RecurringInterval =
        runCatching { RecurringInterval.valueOf(raw.orEmpty()) }
            .onFailure { println("[DomainMappers] Unknown RecurringInterval: $raw, fallback to NONE: ${it.message}") }
            .getOrDefault(RecurringInterval.NONE)

    fun safeDueBillStatus(raw: String?): com.ssajudn.barebudget.domain.model.DueBillStatus =
        runCatching { com.ssajudn.barebudget.domain.model.DueBillStatus.valueOf(raw.orEmpty()) }
            .getOrDefault(com.ssajudn.barebudget.domain.model.DueBillStatus.UNPAID)

    const val DEFAULT_WALLET_NAME = com.ssajudn.barebudget.domain.AppConfig.DEFAULT_WALLET_NAME
    const val DEFAULT_WALLET_COLOR = com.ssajudn.barebudget.domain.AppConfig.DEFAULT_WALLET_COLOR
    const val DEFAULT_ICON = com.ssajudn.barebudget.domain.AppConfig.DEFAULT_WALLET_ICON
    const val DEFAULT_GOAL_COLOR = com.ssajudn.barebudget.domain.AppConfig.DEFAULT_GOAL_COLOR
}
