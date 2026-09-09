package com.ssajudn.bareuang.data.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.usecase.ProcessRecurringTransactionsUseCase
import com.ssajudn.bareuang.domain.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService,
    private val processRecurring: ProcessRecurringTransactionsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val templates = db.transactionDao().getRecurringTemplates().map { it.toTransaction() }
            if (templates.isEmpty()) return Result.success()

            val today = DateUtils.getCurrentDateISO()
            val result = processRecurring(templates, today)

            if (result.newTransactions.isNotEmpty() || result.updatedTemplates.isNotEmpty()) {
                db.withTransaction {
                    for (tx in result.newTransactions) {
                        val req = CreateTransactionRequest(
                            amount = tx.amount,
                            type = tx.type,
                            category = tx.category,
                            merchant = tx.merchant ?: "",
                            date = tx.date,
                            notes = tx.notes ?: "",
                            receiptUrl = tx.receiptUrl ?: "",
                            walletId = tx.walletId,
                            toWalletId = tx.toWalletId
                        )
                        balanceService.adjustForCreate(req)
                        val entity = LocalTransactionEntity.fromTransaction(tx, isSynced = false)
                        db.transactionDao().insertTransaction(entity)
                    }

                    for (update in result.updatedTemplates) {
                        db.transactionDao().updateNextOccurrence(update.templateId, update.nextOccurrenceDate)
                    }
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_PERIODIC = "recurring_tx_periodic"
        const val UNIQUE_ONE_TIME = "recurring_tx_once"

        fun ensureScheduled(context: Context) {
            val constraints = Constraints.Builder().build()
            val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<RecurringTransactionWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
