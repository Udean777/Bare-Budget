package com.ssajudn.barebudget.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.CreateGoalRequest
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.repository.MigrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val sessionManager: UserSessionManager,
    private val firebaseAuth: FirebaseAuth
) : MigrationRepository {

    override suspend fun migrateGuestData(guestUserId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            return@withContext Result.failure(AppException.AuthException("User belum login ke Firebase"))
        }

        try {
            val failures = mutableListOf<String>()
            val localWallets = database.walletDao().getAllWallets()
            val walletIdMap = mutableMapOf<String, String>()

            for (localW in localWallets) {
                val oldId = localW.id
                val req = com.ssajudn.barebudget.data.network.dto.CreateWalletRequestDto(localW.name, localW.balance, localW.colorHex, localW.iconName)
                val response = apiService.createWallet(req)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.id.let { newId -> walletIdMap[oldId] = newId }
                } else {
                    failures.add("wallet:${localW.id}")
                }
            }

            val localTx = database.transactionDao().getAllTransactions()
            for (tx in localTx) {
                val mappedWalletId = tx.walletId?.let { walletIdMap[it] ?: it }
                val mappedToWalletId = tx.toWalletId?.let { walletIdMap[it] ?: it }
                val req = com.ssajudn.barebudget.data.network.dto.CreateTransactionRequestDto(tx.amount, tx.type, tx.category, tx.merchant ?: "-", tx.date, tx.notes ?: "", null, mappedWalletId, mappedToWalletId)
                val resp = apiService.createTransaction(req)
                if (!resp.isSuccessful) failures.add("tx:${tx.id}")
            }

            val localGoals = database.goalDao().getAllGoals()
            for (g in localGoals) {
                val req = com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto(g.name, g.targetAmount, g.targetDate ?: "", g.colorHex, "")
                val resp = apiService.createGoal(req)
                if (!resp.isSuccessful) failures.add("goal:${g.id}")
            }

            val localBills = database.dueBillDao().getAllDueBills()
            for (b in localBills) {
                val req = com.ssajudn.barebudget.data.network.dto.CreateDueBillRequestDto(b.providerName, b.providerIconUrl, b.totalAmount, b.dueDate, b.isRecurring, b.recurringInterval, b.notes ?: "")
                val resp = apiService.createDueBill(req)
                if (!resp.isSuccessful) failures.add("bill:${b.id}")
            }

            if (failures.isNotEmpty()) {
                return@withContext Result.failure(AppException.UnknownError("Migration partial failure: ${failures.take(5).joinToString()}"))
            }

            database.clearAllTables()
            sessionManager.isGuestMode = false

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(com.ssajudn.barebudget.data.error.ApiErrorParser.fromThrowable(e))
        }
    }
}
