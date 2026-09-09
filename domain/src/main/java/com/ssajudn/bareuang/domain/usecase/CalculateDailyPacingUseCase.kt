package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.BudgetPeriod
import com.ssajudn.bareuang.domain.model.DailyPacingStatus

object CalculateDailyPacingUseCase {
    operator fun invoke(
        monthlyBudget: Long,
        remainingBudget: Long,
        todaySpent: Long,
        period: BudgetPeriod,
        customTarget: Long?,
    ): DailyPacingStatus {
        val automaticAllowance = if (monthlyBudget > 0) {
            remainingBudget.coerceAtLeast(0L) / period.remainingDays
        } else 0L
        val allowance = customTarget ?: automaticAllowance
        return DailyPacingStatus(
            allowance = allowance,
            spent = todaySpent,
            remaining = Math.subtractExact(allowance, todaySpent),
            isCustom = customTarget != null,
        )
    }
}

