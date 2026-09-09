package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalDueBillEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.domain.model.CreateDueBillRequest
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.UpdateDueBillRequest
import com.ssajudn.bareuang.data.service.WalletBalanceService
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.data.error.ApiErrorParser
import androidx.room.withTransaction
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
    private val balanceService: WalletBalanceService,
    private val currencyPreferences: com.ssajudn.bareuang.data.local.CurrencyPreferences
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
            if (request.providerName.isBlank()) return@withContext Result.failure(IllegalArgumentException("Nama provider tidak boleh kosong"))
            if (request.totalAmount <= 0) return@withContext Result.failure(IllegalArgumentException("Jumlah tagihan harus lebih dari 0"))
            val newBill = DueBill(
                id = UUID.randomUUID().toString(),
                providerName = request.providerName.trim(),
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
                if (request.providerName.isBlank()) return@withContext Result.failure(IllegalArgumentException("Nama provider tidak boleh kosong"))
                if (request.totalAmount <= 0) return@withContext Result.failure(IllegalArgumentException("Jumlah tagihan harus lebih dari 0"))
                db.dueBillDao().updateDueBill(
                    id = id,
                    providerName = request.providerName.trim(),
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
                val block: () -> Unit = {
                    val bill = db.dueBillDao().getDueBillById(id)
                    var newPaidWalletId: String? = bill?.paidWalletId
                    if (status == DueBillStatus.PAID && walletId != null) {
                        if (bill != null) {
                            val wallet = db.walletDao().getWalletById(walletId)
                                ?: throw IllegalArgumentException("Dompet tidak ditemukan")
                            if (wallet.balance < bill.totalAmount) {
                                val cur = currencyPreferences.getCurrency()
                                throw IllegalStateException("Saldo dompet tidak cukup. Saldo: ${DomainCurrencyFormatter.format(wallet.balance, cur)}, tagihan: ${DomainCurrencyFormatter.format(bill.totalAmount, cur)}")
                            }
                        }
                        newPaidWalletId = walletId
                        if (bill != null) {
                            val newTx = Transaction(id = UUID.randomUUID().toString(), amount = bill.totalAmount, type = TransactionType.EXPENSE, category = TransactionCategory.BILLS, merchant = bill.providerName, date = DateUtils.getCurrentDateISO(), notes = "Pembayaran tagihan: ${bill.providerName}", walletId = walletId)
                            balanceService.add(walletId, -bill.totalAmount)
                            db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(newTx, isSynced = false))
                        }
                    } else if (status == DueBillStatus.UNPAID) {
                        val previousPaidWalletId = bill?.paidWalletId
                        if (bill != null && bill.status == DueBillStatus.PAID.name && !previousPaidWalletId.isNullOrBlank()) {
                            balanceService.add(previousPaidWalletId, bill.totalAmount)
                            val refundTx = Transaction(id = UUID.randomUUID().toString(), amount = bill.totalAmount, type = TransactionType.INCOME, category = TransactionCategory.BILLS, merchant = "Refund: ${bill.providerName}", date = DateUtils.getCurrentDateISO(), notes = "Pembatalan pembayaran tagihan ${bill.providerName}", walletId = previousPaidWalletId)
                            db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(refundTx, isSynced = false))
                        }
                        newPaidWalletId = null
                    }
                    db.dueBillDao().updateDueBillStatus(id, status.name, newPaidWalletId)
                }
                db.runInTransaction { block() }
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
