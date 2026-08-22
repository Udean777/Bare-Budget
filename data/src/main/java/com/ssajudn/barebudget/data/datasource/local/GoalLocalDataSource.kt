package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalGoalEntity
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.UpdateGoalRequest
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.GoalRepository
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.data.error.ApiErrorParser
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService,
    private val sessionManager: com.ssajudn.barebudget.data.local.UserSessionManager,
    private val outboxScheduler: com.ssajudn.barebudget.data.sync.OutboxScheduler? = null
) {

    suspend fun getGoals(): Result<List<Goal>> = withContext(Dispatchers.IO) {
        try {
            Result.success(db.goalDao().getAllGoals().map { it.toGoal() })
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createGoal(request: CreateGoalRequest): Result<Goal> = withContext(Dispatchers.IO) {
        val localGoal = Goal(
            id = UUID.randomUUID().toString(),
            name = request.name,
            targetAmount = request.targetAmount,
            currentAmount = 0L,
            targetDate = request.targetDate,
            colorHex = request.colorHex,
            notes = request.notes
        )
        db.goalDao().insertGoal(LocalGoalEntity.fromGoal(localGoal, isSynced = false).copy(ownerId = sessionManager.userId))
        if (!sessionManager.isGuestMode && outboxScheduler != null) {
            val dto = com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto(request.name, request.targetAmount, request.targetDate, request.colorHex, request.notes ?: "")
            try { outboxScheduler.enqueue(sessionManager.userId, "goal", localGoal.id!!, dto) } catch (_: Exception) {}
        }
        Result.success(localGoal)
    }

    suspend fun depositToGoal(id: String, amount: Long, walletId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val block: () -> Unit = {
                    db.goalDao().depositToGoal(id, amount)
                    val isDeposit = amount > 0
                    balanceService.add(walletId, -amount)
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
                        LocalTransactionEntity.fromTransaction(localTx, isSynced = false).copy(ownerId = sessionManager.userId)
                    )
                }
                try {
                    db.runInTransaction { block() }
                } catch (_: Exception) {
                    block()
                }
                if (!sessionManager.isGuestMode && outboxScheduler != null) {
                    val dto = com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto("deposit:$id", amount, "", "", "")
                    // ponytail: deposit outbox uses generic payload, worker maps to deposit endpoint when wiring completes
                    try { outboxScheduler.enqueue(sessionManager.userId, "goal_deposit", id, mapOf("id" to id, "amount" to amount, "walletId" to walletId)) } catch (_: Exception) {}
                }
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun updateGoal(id: String, request: UpdateGoalRequest): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                db.goalDao().updateGoal(
                    id = id,
                    name = request.name,
                    targetAmount = request.targetAmount,
                    targetDate = request.targetDate.ifBlank { null },
                    colorHex = request.colorHex,
                    notes = request.notes,
                    isSynced = false
                )
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteGoal(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.goalDao().deleteGoal(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeGoals(): Flow<List<Goal>> =
        db.goalDao().observeAllGoals().map { list -> list.map { it.toGoal() } }
}