package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalDueBillEntity
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.UpdateDueBillRequest
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.DueBillRepository
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
class DueBillLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService
) {

    suspend fun getDueBills(status: String?): Result<List<DueBill>> = withContext(Dispatchers.IO) {
        try {
            val entities = if (status.isNullOrBlank()) {
                db.dueBillDao().getAllDueBills()
            } else {
                db.dueBillDao().getDueBillsByStatus(status)
            }
            Result.success(entities.map { it.toDueBill() })
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill> = withContext(Dispatchers.IO) {
        try {
            val newBill = DueBill(
                id = UUID.randomUUID().toString(),
                providerName = request.providerName,
                providerIconUrl = request.providerIconUrl,
                totalAmount = request.totalAmount,
                dueDate = request.dueDate,
                status = DueBillStatus.UNPAID,
                isRecurring = request.isRecurring,
                recurringInterval = request.recurringInterval,
                notes = request.notes
            )
            db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(newBill, isSynced = false))
            Result.success(newBill)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun updateDueBill(id: String, request: UpdateDueBillRequest): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                db.dueBillDao().updateDueBill(
                    id = id,
                    providerName = request.providerName,
                    providerIconUrl = request.providerIconUrl,
                    totalAmount = request.totalAmount,
                    dueDate = request.dueDate,
                    isRecurring = request.isRecurring,
                    recurringInterval = request.recurringInterval.name,
                    notes = request.notes,
                    isSynced = false
                )
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String?): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val bill = db.dueBillDao().getDueBillById(id)
                var newPaidWalletId: String? = bill?.paidWalletId

                if (status == DueBillStatus.PAID && walletId != null) {
                    newPaidWalletId = walletId
                    if (bill != null) {
                        val newTx = Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = bill.totalAmount,
                            type = TransactionType.EXPENSE,
                            category = TransactionCategory.BILLS,
                            merchant = bill.providerName,
                            date = DateUtils.getCurrentDateISO(),
                            notes = "Pembayaran tagihan: ${bill.providerName}",
                            walletId = walletId
                        )
                        balanceService.add(walletId, -bill.totalAmount)
                        db.transactionDao().insertTransaction(
                            LocalTransactionEntity.fromTransaction(newTx, isSynced = false)
                        )
                    }
                } else if (status == DueBillStatus.UNPAID) {
                    val previousPaidWalletId = bill?.paidWalletId
                    if (bill != null && bill.status == DueBillStatus.PAID.name && !previousPaidWalletId.isNullOrBlank()) {
                        balanceService.add(previousPaidWalletId, bill.totalAmount)

                        val refundTx = Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = bill.totalAmount,
                            type = TransactionType.INCOME,
                            category = TransactionCategory.BILLS,
                            merchant = "Refund: ${bill.providerName}",
                            date = DateUtils.getCurrentDateISO(),
                            notes = "Pembatalan pembayaran tagihan ${bill.providerName}",
                            walletId = previousPaidWalletId
                        )
                        db.transactionDao().insertTransaction(
                            LocalTransactionEntity.fromTransaction(refundTx, isSynced = false)
                        )
                    }
                    newPaidWalletId = null
                }

                db.dueBillDao().updateDueBillStatus(id, status.name, newPaidWalletId)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteDueBill(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.dueBillDao().deleteDueBill(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeDueBills(): Flow<List<DueBill>> =
        db.dueBillDao().observeAllDueBills().map { list -> list.map { it.toDueBill() } }
}