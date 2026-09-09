package com.ssajudn.bareuang.domain.model

/** The calendar window used by dashboard budget calculations. */
data class BudgetPeriod(
    val monthYear: String,
    val todayIso: String,
    val daysPassed: Int,
    val daysInMonth: Int,
) {
    val remainingDays: Int get() = (daysInMonth - daysPassed + 1).coerceAtLeast(1)
}

data class DateRange(val fromInclusive: String, val toExclusive: String)

sealed interface RunwayStatus {
    data object BudgetNotSet : RunwayStatus
    data object Exhausted : RunwayStatus
    data object NoSpending : RunwayStatus
    data class Warning(val deathDay: Int, val daysRemaining: Int) : RunwayStatus
    data object Healthy : RunwayStatus
}

data class DailyPacingStatus(
    val allowance: Long,
    val spent: Long,
    val remaining: Long,
    val isCustom: Boolean,
) {
    val isExceeded: Boolean get() = remaining < 0
}

data class OutstandingBillsSummary(val unpaidTotal: Long, val unpaidCount: Int)

