package com.ssajudn.bareuang.data.mapper

import android.util.Log
import com.ssajudn.bareuang.domain.AppConfig
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType

/** Converts persisted values to resilient domain values and exposes persistence defaults. */
object PersistenceMappers {
    fun safeTransactionType(raw: String?): TransactionType =
        runCatching { TransactionType.valueOf(raw.orEmpty()) }
            .onFailure { Log.w("PersistenceMappers", "Unknown TransactionType: $raw, fallback to EXPENSE: ${it.message}") }
            .getOrDefault(TransactionType.EXPENSE)

    fun safeCategory(raw: String?): TransactionCategory =
        runCatching { TransactionCategory.valueOf(raw.orEmpty()) }
            .onFailure { Log.w("PersistenceMappers", "Unknown TransactionCategory: $raw, fallback to OTHER: ${it.message}") }
            .getOrDefault(TransactionCategory.OTHER)

    fun safeRecurringInterval(raw: String?): RecurringInterval =
        runCatching { RecurringInterval.valueOf(raw.orEmpty()) }
            .onFailure { Log.w("PersistenceMappers", "Unknown RecurringInterval: $raw, fallback to NONE: ${it.message}") }
            .getOrDefault(RecurringInterval.NONE)

    fun safeDueBillStatus(raw: String?): DueBillStatus =
        runCatching { DueBillStatus.valueOf(raw.orEmpty()) }
            .getOrDefault(DueBillStatus.UNPAID)

    const val DEFAULT_WALLET_NAME = AppConfig.DEFAULT_WALLET_NAME
    const val DEFAULT_WALLET_COLOR = AppConfig.DEFAULT_WALLET_COLOR
    const val DEFAULT_ICON = AppConfig.DEFAULT_WALLET_ICON
    const val DEFAULT_GOAL_COLOR = AppConfig.DEFAULT_GOAL_COLOR
}
