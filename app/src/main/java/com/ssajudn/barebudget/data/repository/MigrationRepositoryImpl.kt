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
            // 1. Wallets
            val localWallets = database.walletDao().getAllWallets()
            val walletIdMap = mutableMapOf<String, String>()

            for (localW in localWallets) {
                val oldId = localW.id
                val req = CreateWalletRequest(
                    name = localW.name,
                    balance = localW.balance,
                    colorHex = localW.colorHex,
                    iconName = localW.iconName
                )
                val response = apiService.createWallet(req)
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!
                    created.id?.let { newId ->
                        walletIdMap[oldId] = newId
                    }
                }
            }

            // 2. Transactions
            val localTx = database.transactionDao().getAllTransactions()
            for (tx in localTx) {
                val mappedWalletId = tx.walletId?.let { walletIdMap[it] ?: it }
                val mappedToWalletId = tx.toWalletId?.let { walletIdMap[it] ?: it }
                val req = CreateTransactionRequest(
                    amount = tx.amount,
                    type = DomainMappers.safeTransactionType(tx.type),
                    category = DomainMappers.safeCategory(tx.category),
                    merchant = tx.merchant ?: "-",
                    date = tx.date,
                    notes = tx.notes ?: "",
                    walletId = mappedWalletId,
                    toWalletId = mappedToWalletId
                )
                apiService.createTransaction(req)
            }

            // 3. Goals
            val localGoals = database.goalDao().getAllGoals()
            for (g in localGoals) {
                val req = CreateGoalRequest(
                    name = g.name,
                    targetAmount = g.targetAmount,
                    targetDate = g.targetDate ?: "",
                    colorHex = g.colorHex,
                    notes = ""
                )
                apiService.createGoal(req)
            }

            // 4. Due Bills
            val localBills = database.dueBillDao().getAllDueBills()
            for (b in localBills) {
                val req = CreateDueBillRequest(
                    providerName = b.providerName,
                    totalAmount = b.totalAmount,
                    dueDate = b.dueDate,
                    isRecurring = b.isRecurring,
                    recurringInterval = DomainMappers.safeRecurringInterval(b.recurringInterval),
                    notes = b.notes ?: ""
                )
                apiService.createDueBill(req)
            }

            // Clean local database after migration
            database.clearAllTables()
            sessionManager.isGuestMode = false

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(com.ssajudn.barebudget.data.error.ApiErrorParser.fromThrowable(e))
        }
    }
}
