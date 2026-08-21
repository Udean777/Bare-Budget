package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.data.datasource.local.BudgetLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.BudgetRemoteDataSource
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val local: BudgetLocalDataSource,
    private val remote: BudgetRemoteDataSource,
    private val sessionManager: UserSessionManager
) : BudgetRepository {

    @Suppress("DEPRECATION")
    override suspend fun getDashboardSummary(): Result<DashboardSummary> =
        if (sessionManager.isGuestMode) local.getDashboardSummary()
        else remote.getDashboardSummary()

    override suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.setBudget(monthlyLimit, monthYear)
        else remote.setBudget(monthlyLimit, monthYear)

    override suspend fun getMonthlyBudget(monthYear: String): Result<Long> =
        if (sessionManager.isGuestMode) local.getMonthlyBudget(monthYear)
        else remote.getMonthlyBudget(monthYear)
}
