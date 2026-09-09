package com.ssajudn.bareuang.domain.model

data class CategoryBudget(
    val category: TransactionCategory,
    val limitAmount: Long,
    val spentAmount: Long = 0L,
    val monthYear: String = ""
) {
    val remainingAmount: Long get() = (limitAmount - spentAmount).coerceAtLeast(0L)
    val progressPercentage: Float get() = if (limitAmount > 0) (spentAmount.toFloat() / limitAmount).coerceIn(0f, 1f) else 0f
    val isOverspent: Boolean get() = spentAmount > limitAmount
    val isWarning: Boolean get() = progressPercentage >= 0.8f && !isOverspent
}
