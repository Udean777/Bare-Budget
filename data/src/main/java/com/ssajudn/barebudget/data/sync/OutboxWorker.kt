package com.ssajudn.barebudget.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.OutboxState
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.data.network.dto.CreateDueBillRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateTransactionRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateWalletRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class OutboxWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val db: AppDatabase,
    private val api: ApiService,
    private val sessionManager: UserSessionManager,
    private val gson: Gson = Gson()
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (sessionManager.isGuestMode) return@withContext Result.success()
        val pending = db.outboxDao().getPending(limit = 20)
        if (pending.isEmpty()) return@withContext Result.success()
        var hasRetry = false
        for (item in pending) {
            if (item.ownerId != sessionManager.userId) continue
            db.outboxDao().updateState(item.id, OutboxState.IN_FLIGHT.name, item.attempts, null)
            val ok = try {
                when (item.entityType) {
                    "wallet" -> {
                        val req = gson.fromJson(item.payloadJson, CreateWalletRequestDto::class.java)
                        val r = api.createWallet(req)
                        r.isSuccessful
                    }
                    "transaction" -> {
                        val req = gson.fromJson(item.payloadJson, CreateTransactionRequestDto::class.java)
                        val r = api.createTransaction(req)
                        r.isSuccessful
                    }
                    "goal" -> {
                        val req = gson.fromJson(item.payloadJson, CreateGoalRequestDto::class.java)
                        val r = api.createGoal(req)
                        r.isSuccessful
                    }
                    "duebill" -> {
                        val req = gson.fromJson(item.payloadJson, CreateDueBillRequestDto::class.java)
                        val r = api.createDueBill(req)
                        r.isSuccessful
                    }
                    else -> true
                }
            } catch (_: Exception) { false }
            if (ok) {
                db.outboxDao().delete(item.id)
            } else {
                val attempts = item.attempts + 1
                if (attempts >= 5) {
                    db.outboxDao().updateState(item.id, OutboxState.FAILED_RETRYABLE.name, attempts, System.currentTimeMillis() + 3600_000)
                } else {
                    val backoff = (1 shl attempts) * 30_000L
                    db.outboxDao().updateState(item.id, OutboxState.FAILED_RETRYABLE.name, attempts, System.currentTimeMillis() + backoff)
                    hasRetry = true
                }
            }
        }
        if (hasRetry) Result.retry() else Result.success()
    }

    // ponytail: DTO already snake_case, mapping kept minimal until full DTO wiring
    private fun mapWallet(dto: CreateWalletRequestDto) = com.ssajudn.barebudget.domain.model.CreateWalletRequest(dto.name, dto.balance, dto.colorHex, dto.iconName)
    private fun mapTx(dto: CreateTransactionRequestDto) = com.ssajudn.barebudget.domain.model.CreateTransactionRequest(dto.amount, com.ssajudn.barebudget.data.repository.DomainMappers.safeTransactionType(dto.type), com.ssajudn.barebudget.data.repository.DomainMappers.safeCategory(dto.category), dto.merchant, dto.date, dto.notes ?: "", dto.receiptUrl ?: "", dto.walletId, dto.toWalletId)
    private fun mapGoal(dto: CreateGoalRequestDto) = com.ssajudn.barebudget.domain.model.CreateGoalRequest(dto.name, dto.targetAmount, dto.targetDate ?: "", dto.colorHex, dto.notes ?: "")
    private fun mapDueBill(dto: CreateDueBillRequestDto) = com.ssajudn.barebudget.domain.model.CreateDueBillRequest(dto.providerName, dto.providerIconUrl, dto.totalAmount, dto.dueDate, dto.isRecurring, com.ssajudn.barebudget.data.repository.DomainMappers.safeRecurringInterval(dto.recurringInterval), dto.notes ?: "")
}
