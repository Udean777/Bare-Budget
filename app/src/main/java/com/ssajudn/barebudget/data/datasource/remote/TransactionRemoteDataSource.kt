package com.ssajudn.barebudget.data.datasource.remote

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
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
class TransactionRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService
) {

    suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getTransactions(category = category, page = page, limit = limit)
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.data
                    // Cache to local Room
                    db.transactionDao()
                        .insertTransactions(list.map { LocalTransactionEntity.fromTransaction(it, true) })
                    Result.success(list)
                } else {
                    // Offline fallback from Room Cache
                    val entities = db.transactionDao().getAllTransactions()
                    if (entities.isNotEmpty()) {
                        Result.success(entities.map { it.toTransaction() })
                    } else {
                        Result.failure(ApiErrorParser.parse(response))
                    }
                }
            } catch (e: Exception) {
                // Local fallback
                val entities = db.transactionDao().getAllTransactions()
                if (entities.isNotEmpty()) {
                    Result.success(entities.map { it.toTransaction() })
                } else {
                    Result.failure(ApiErrorParser.fromThrowable(e))
                }
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

                balanceService.adjustForCreate(request)

                val response = api.createTransaction(request)
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!
                    db.transactionDao()
                        .insertTransaction(LocalTransactionEntity.fromTransaction(created, isSynced = true))
                    Result.success(created)
                } else {
                    // Save offline to Room with isSynced = false
                    db.transactionDao()
                        .insertTransaction(LocalTransactionEntity.fromTransaction(newTx, isSynced = false))
                    Result.success(newTx)
                }
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteTransaction(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val tx = db.transactionDao().getTransactionById(id)
            if (tx != null) balanceService.revert(tx)
            db.transactionDao().deleteTransaction(id)
            api.deleteTransaction(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeTransactions(): Flow<List<Transaction>> =
        db.transactionDao().observeAllTransactions().map { list -> list.map { it.toTransaction() } }
}