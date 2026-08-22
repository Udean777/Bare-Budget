package com.ssajudn.barebudget.domain.repository

import com.ssajudn.barebudget.domain.model.DashboardSummary

/**
 * Domain port untuk Budget.
 * Note: getDashboardSummary adalah derived state — setelah Phase 5 dipindah ke UseCase,
 * interface ini tinggal setBudget (SRP). Untuk transisi, keep keduanya, getDashboardSummary di-@Deprecated.
 */
interface BudgetRepository {
    @Deprecated("Pindah ke GetDashboardSummaryUseCase — logic dashboard keluar dari data layer (SRP).")
    suspend fun getDashboardSummary(): Result<DashboardSummary>

    suspend fun setBudget(monthlyLimit: Long, monthYear: String = ""): Result<Boolean>

    suspend fun getMonthlyBudget(monthYear: String = ""): Result<Long>
}
