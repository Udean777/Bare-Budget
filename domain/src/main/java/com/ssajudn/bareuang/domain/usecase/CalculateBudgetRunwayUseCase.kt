package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.BudgetPeriod
import com.ssajudn.bareuang.domain.model.RunwayStatus

data class BudgetRunwayResult(
    val remainingBudget: Long,
    val averageDailySpend: Long,
    val estimatedDeathDay: Int,
    val status: RunwayStatus,
)

object CalculateBudgetRunwayUseCase {
    operator fun invoke(monthlyBudget: Long, totalSpent: Long, period: BudgetPeriod): BudgetRunwayResult {
        val remainingBudget = Math.subtractExact(monthlyBudget, totalSpent)
        val averageDailySpend = if (period.daysPassed > 0) totalSpent / period.daysPassed else 0L
        return when {
            monthlyBudget <= 0 -> BudgetRunwayResult(remainingBudget, averageDailySpend, period.daysInMonth, RunwayStatus.BudgetNotSet)
            remainingBudget <= 0 -> BudgetRunwayResult(remainingBudget, averageDailySpend, period.daysPassed, RunwayStatus.Exhausted)
            averageDailySpend <= 0 -> BudgetRunwayResult(remainingBudget, averageDailySpend, period.daysInMonth, RunwayStatus.NoSpending)
            else -> {
                val daysRemaining = (remainingBudget / averageDailySpend).toInt()
                val deathDay = (period.daysPassed + daysRemaining).coerceAtMost(period.daysInMonth)
                val status = if (deathDay < period.daysInMonth) {
                    RunwayStatus.Warning(deathDay, daysRemaining)
                } else {
                    RunwayStatus.Healthy
                }
                BudgetRunwayResult(remainingBudget, averageDailySpend, deathDay, status)
            }
        }
    }
}

