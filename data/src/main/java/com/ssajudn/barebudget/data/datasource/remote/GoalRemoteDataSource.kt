package com.ssajudn.barebudget.data.datasource.remote

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalGoalEntity
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.data.model.DepositGoalRequest
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.UpdateGoalRequest
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.GoalRepository
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService
) {

    suspend fun getGoals(): Result<List<Goal>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGoals()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.data
                val remoteList = dtos.map { it.toDomain() }
                db.goalDao().clearAll()
                db.goalDao().insertGoals(remoteList.map { LocalGoalEntity.fromGoal(it, isSynced = true) })
                Result.success(remoteList)
            } else {
                val cached = db.goalDao().getAllGoals().map { it.toGoal() }
                Result.success(cached)
            }
        } catch (e: Exception) {
            val fallback = db.goalDao().getAllGoals().map { it.toGoal() }
            if (fallback.isNotEmpty()) Result.success(fallback) else Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createGoal(request: CreateGoalRequest): Result<Goal> = withContext(Dispatchers.IO) {
        val tempId = UUID.randomUUID().toString()
        val localGoal = Goal(id = tempId, name = request.name, targetAmount = request.targetAmount, currentAmount = 0L, targetDate = request.targetDate, colorHex = request.colorHex, notes = request.notes)
        db.goalDao().insertGoal(LocalGoalEntity.fromGoal(localGoal, isSynced = false))
        try {
            val dtoReq = com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto(request.name, request.targetAmount, request.targetDate, request.colorHex, request.notes ?: "")
            val response = api.createGoal(dtoReq)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toDomain()
                db.goalDao().deleteGoal(tempId)
                db.goalDao().insertGoal(LocalGoalEntity.fromGoal(created, isSynced = true))
                Result.success(created)
            } else Result.success(localGoal)
        } catch (e: Exception) { Result.success(localGoal) }
    }

    suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Optimistic local goal update
                db.goalDao().depositToGoal(id, amount)

                // 2. Adjust wallet balance via centralized service
                val isDeposit = amount > 0
                balanceService.add(walletId, -amount)

                // 3. Record local transaction for history and runway analytics
                val goalEntity = db.goalDao().getGoalById(id)
                val goalName = goalEntity?.name ?: "Tabungan"
                val absAmount = kotlin.math.abs(amount)
                val txType = if (isDeposit) TransactionType.EXPENSE else TransactionType.INCOME
                val merchantName = if (isDeposit) "Tabungan: $goalName" else "Penarikan: $goalName"

                val localTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = absAmount,
                    type = txType,
                    category = TransactionCategory.OTHER,
                    merchant = merchantName,
                    date = DateUtils.getCurrentDateISO(),
                    notes = if (isDeposit) "Setor ke tabungan $goalName" else "Penarikan dari tabungan $goalName",
                    walletId = walletId
                )
                db.transactionDao().insertTransaction(
                    LocalTransactionEntity.fromTransaction(localTx, isSynced = true)
                )

                val response = api.depositToGoal(id, DepositGoalRequest(amount = amount, walletId = walletId))
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.success(true) // local already updated
                }
            } catch (e: Exception) {
                Result.success(true)
            }
        }

    suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                db.goalDao().updateGoal(id = id, name = request.name, targetAmount = request.targetAmount, targetDate = request.targetDate.ifBlank { null }, colorHex = request.colorHex, notes = request.notes, isSynced = true)
                try {
                    val dto = com.ssajudn.barebudget.data.network.dto.UpdateGoalRequestDto(request.name, request.targetAmount, request.targetDate.ifBlank { null }, request.colorHex, request.notes)
                    api.updateGoal(id, dto)
                } catch (e: Exception) {}
                Result.success(true)
            } catch (e: Exception) { Result.failure(ApiErrorParser.fromThrowable(e)) }
        }

    suspend fun deleteGoal(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.goalDao().deleteGoal(id)

            try {
                api.deleteGoal(id)
            } catch (e: Exception) {
                // ignore — local already updated
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeGoals(): Flow<List<Goal>> =
        db.goalDao().observeAllGoals().map { list -> list.map { it.toGoal() } }
}