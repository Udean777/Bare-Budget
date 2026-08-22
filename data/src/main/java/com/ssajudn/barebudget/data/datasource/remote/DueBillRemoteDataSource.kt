package com.ssajudn.barebudget.data.datasource.remote

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
import com.ssajudn.barebudget.data.model.UpdateDueBillStatusRequest
import com.ssajudn.barebudget.data.network.ApiService
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
class DueBillRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService
) {

    suspend fun getDueBills(status: String?): Result<List<DueBill>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDueBills(status)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.data
                val list = dtos.map { it.toDomain() }
                db.dueBillDao().insertDueBills(list.map { LocalDueBillEntity.fromDueBill(it, true) })
                Result.success(list)
            } else {
                val entities = db.dueBillDao().getAllDueBills()
                Result.success(entities.map { it.toDueBill() })
            }
        } catch (e: Exception) {
            val entities = db.dueBillDao().getAllDueBills()
            Result.success(entities.map { it.toDueBill() })
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

            val dtoReq = com.ssajudn.barebudget.data.network.dto.CreateDueBillRequestDto(request.providerName, request.providerIconUrl, request.totalAmount, request.dueDate, request.isRecurring, request.recurringInterval.name, request.notes ?: "")
            val response = api.createDueBill(dtoReq)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!.toDomain()
                db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(created, isSynced = true))
                Result.success(created)
            } else {
                db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(newBill, isSynced = false))
                Result.success(newBill)
            }
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
                    isSynced = true
                )
                val dto = com.ssajudn.barebudget.data.network.dto.UpdateDueBillRequestDto(request.providerName, request.providerIconUrl, request.totalAmount, request.dueDate, request.isRecurring, request.recurringInterval.name, request.notes ?: "")
                api.updateDueBill(id, dto)
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
                            LocalTransactionEntity.fromTransaction(newTx, isSynced = true)
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
                            LocalTransactionEntity.fromTransaction(refundTx, isSynced = true)
                        )
                    }
                    newPaidWalletId = null
                }

                db.dueBillDao().updateDueBillStatus(id, status.name, newPaidWalletId)
                api.updateDueBillStatus(id, UpdateDueBillStatusRequest(status = status, walletId = walletId))
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteDueBill(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.dueBillDao().deleteDueBill(id)
            api.deleteDueBill(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeDueBills(): Flow<List<DueBill>> =
        db.dueBillDao().observeAllDueBills().map { list -> list.map { it.toDueBill() } }
}