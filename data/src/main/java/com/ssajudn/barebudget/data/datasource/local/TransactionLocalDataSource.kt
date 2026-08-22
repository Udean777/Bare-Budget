package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService,
    private val sessionManager: com.ssajudn.barebudget.data.local.UserSessionManager,
    private val outboxScheduler: com.ssajudn.barebudget.data.sync.OutboxScheduler? = null
) {

    suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        withContext(Dispatchers.IO) {
            try {
                val entities = if (category.isNullOrBlank()) {
                    db.transactionDao().getAllTransactions()
                } else {
                    db.transactionDao().getTransactionsByCategory(category)
                }
                Result.success(entities.map { it.toTransaction() })
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        withContext(Dispatchers.IO) {
            try {
                val dateStr = request.date.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                }
                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = request.amount,
                    type = request.type,
                    category = request.category,
                    merchant = request.merchant,
                    date = dateStr,
                    notes = request.notes,
                    receiptUrl = request.receiptUrl,
                    walletId = request.walletId,
                    toWalletId = request.toWalletId
                )

                try { db.withTransaction {
                    balanceService.adjustForCreate(request)
                    val entity = LocalTransactionEntity.fromTransaction(newTx, isSynced = false).copy(ownerId = sessionManager.userId)
                    db.transactionDao().insertTransaction(entity)
                    if (!sessionManager.isGuestMode && outboxScheduler != null) {
                        val dto = com.ssajudn.barebudget.data.network.dto.CreateTransactionRequestDto(newTx.amount, newTx.type.name, newTx.category.name, newTx.merchant ?: "", newTx.date, newTx.notes ?: "", newTx.receiptUrl, newTx.walletId, newTx.toWalletId)
                        try { outboxScheduler.enqueue(sessionManager.userId, "transaction", newTx.id!!, dto) } catch (_: Exception) {}
                    }
                } } catch (_: Exception) {
                    balanceService.adjustForCreate(request)
                    val entity = LocalTransactionEntity.fromTransaction(newTx, isSynced = false).copy(ownerId = sessionManager.userId)
                    db.transactionDao().insertTransaction(entity)
                }
                Result.success(newTx)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteTransaction(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            try { db.withTransaction {
                val tx = db.transactionDao().getTransactionById(id)
                if (tx != null) balanceService.revert(tx)
                db.transactionDao().deleteTransaction(id)
            } } catch (_: Exception) {
                val tx = db.transactionDao().getTransactionById(id)
                if (tx != null) balanceService.revert(tx)
                db.transactionDao().deleteTransaction(id)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeTransactions(): Flow<List<Transaction>> =
        db.transactionDao().observeAllTransactions().map { list -> list.map { it.toTransaction() } }
}