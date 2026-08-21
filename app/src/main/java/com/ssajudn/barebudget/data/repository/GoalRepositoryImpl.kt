package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.UpdateGoalRequest
import com.ssajudn.barebudget.data.datasource.local.GoalLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.GoalRemoteDataSource
import com.ssajudn.barebudget.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val local: GoalLocalDataSource,
    private val remote: GoalRemoteDataSource,
    private val sessionManager: UserSessionManager
) : GoalRepository {

    override suspend fun getGoals(): Result<List<Goal>> =
        if (sessionManager.isGuestMode) local.getGoals()
        else remote.getGoals()

    override suspend fun createGoal(request: CreateGoalRequest): Result<Goal> =
        if (sessionManager.isGuestMode) local.createGoal(request)
        else remote.createGoal(request)

    override suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.depositToGoal(id, amount, walletId)
        else remote.depositToGoal(id, amount, walletId)

    override suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean> =
        if (sessionManager.isGuestMode) local.updateGoal(id, request)
        else remote.updateGoal(id, request)

    override suspend fun deleteGoal(id: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.deleteGoal(id)
        else remote.deleteGoal(id)

    override fun observeGoals(): Flow<List<Goal>> =
        if (sessionManager.isGuestMode) local.observeGoals()
        else remote.observeGoals()
}
