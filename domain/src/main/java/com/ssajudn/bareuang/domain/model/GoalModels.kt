package com.ssajudn.bareuang.domain.model

data class Goal(
    val id: String? = null,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val targetDate: String? = null,
    val colorHex: String = "#4E73DF",
    val notes: String? = null,
    val createdAt: String? = null
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingAmount: Long
        get() = (targetAmount - currentAmount).coerceAtLeast(0L)

    /**
     * Days until [targetDate] (ISO yyyy-MM-dd…), or null when unset/unparseable.
     */
    fun daysLeftUntilTarget(referenceDate: java.time.LocalDate): Int? {
        val isoDate = targetDate?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
        return runCatching {
            java.time.temporal.ChronoUnit.DAYS.between(referenceDate, java.time.LocalDate.parse(isoDate))
        }.getOrNull()?.toInt()
    }

    /**
     * Suggested savings pace to reach the target date: per-month and per-day
     * amounts, or null when there is no future target date.
     */
    fun suggestedSavingsPace(referenceDate: java.time.LocalDate): Pair<Long, Long>? {
        val daysLeft = daysLeftUntilTarget(referenceDate) ?: return null
        if (daysLeft <= 0 || remainingAmount <= 0L) return null
        val monthsLeft = maxOf(1L, daysLeft / 30L)
        return (remainingAmount / monthsLeft) to (remainingAmount / daysLeft)
    }
}

data class CreateGoalRequest(
    val name: String,
    val targetAmount: Long,
    val targetDate: String = "",
    val colorHex: String = "#4E73DF",
    val notes: String = ""
)

data class UpdateGoalRequest(
    val name: String,
    val targetAmount: Long,
    val targetDate: String = "",
    val colorHex: String = "#4E73DF",
    val notes: String = ""
)
