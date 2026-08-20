package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.BareBudgetApplication
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalBudgetEntity
import com.ssajudn.barebudget.data.local.room.LocalDueBillEntity
import com.ssajudn.barebudget.data.local.room.LocalGoalEntity
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.data.model.*
import com.ssajudn.barebudget.data.network.ApiClient
import com.ssajudn.barebudget.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class BudgetRepository(
    private val api: ApiService = ApiClient.apiService,
    private val db: AppDatabase = BareBudgetApplication.instance.database,
    private val sessionManager: UserSessionManager = BareBudgetApplication.instance.sessionManager
) {

    private val isGuest: Boolean
        get() = sessionManager.isGuestMode

    // ==========================================
    // 1. DASHBOARD & FINANCIAL RUNWAY
    // ==========================================
    suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                // Calculate dashboard metrics locally from Room DB
                val now = Calendar.getInstance()
                val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
                val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
                val daysPassed = now.get(Calendar.DAY_OF_MONTH)

                val localBudget = db.budgetDao().getBudget(monthYear)
                val monthlyBudget = localBudget?.monthlyLimit ?: 0L

                val allTx = db.transactionDao().getAllTransactions()
                val currentMonthTx = allTx.filter { it.date.startsWith(monthYear) }

                // Only count EXPENSE for total spent
                val expensesTx = currentMonthTx.filter {
                    val t = try { com.ssajudn.barebudget.data.model.TransactionType.valueOf(it.type) } catch(e: Exception) { com.ssajudn.barebudget.data.model.TransactionType.EXPENSE }
                    t == com.ssajudn.barebudget.data.model.TransactionType.EXPENSE
                }
                val totalSpent = expensesTx.sumOf { it.amount }

                val remainingBudget = monthlyBudget - totalSpent
                val avgDaily = if (daysPassed > 0) totalSpent / daysPassed else 0L

                // Calculate Net Worth from local Wallets
                val wallets = db.walletDao().getAllWallets()
                val currentNetWorth = wallets.sumOf { it.balance }

                var estimatedDeathDay = daysInMonth
                var runwayMsg: String

                if (monthlyBudget <= 0) {
                    runwayMsg = "Monthly budget not set yet. Tap here to set your target budget."
                } else if (remainingBudget <= 0) {
                    estimatedDeathDay = daysPassed
                    runwayMsg = "CRITICAL: You have exhausted your budget for this month! Stop all non-essential spending."
                } else if (avgDaily <= 0) {
                    estimatedDeathDay = daysInMonth
                    runwayMsg = "GREAT: No expenses recorded yet this month. Keep it up!"
                } else {
                    val daysRemainingFromRunway = (remainingBudget / avgDaily).toInt()
                    val calculatedDeathDay = daysPassed + daysRemainingFromRunway
                    estimatedDeathDay = calculatedDeathDay.coerceAtMost(daysInMonth)

                    if (calculatedDeathDay < daysInMonth) {
                        runwayMsg = "WARNING: At your current burn rate, your money runs out on day $calculatedDeathDay ($daysRemainingFromRunway days left)!"
                    } else {
                        runwayMsg = "HEALTHY: Your financial runway is safe until the end of the month."
                    }
                }

                // Category breakdown
                val catMap = currentMonthTx.groupBy { it.category }
                val topCategories = catMap.map { (catStr, list) ->
                    val cat = try { TransactionCategory.valueOf(catStr) } catch (e: Exception) { TransactionCategory.OTHER }
                    CategorySummary(
                        category = cat,
                        total = list.sumOf { it.amount },
                        count = list.size.toLong()
                    )
                }.sortedByDescending { it.total }

                // Due bills
                val allBills = db.dueBillDao().getAllDueBills()
                val unpaidBills = allBills.filter { it.status == DueBillStatus.UNPAID.name }
                val unpaidSum = unpaidBills.sumOf { it.totalAmount }

                val summary = DashboardSummary(
                    monthlyBudget = monthlyBudget,
                    totalSpent = totalSpent,
                    remainingBudget = remainingBudget,
                    daysPassed = daysPassed,
                    daysInMonth = daysInMonth,
                    averageDailySpend = avgDaily,
                    estimatedDeathDay = estimatedDeathDay,
                    runwayMessage = runwayMsg,
                    topCategories = topCategories,
                    unpaidDueBillsSum = unpaidSum,
                    netWorth = currentNetWorth,
                    recentTransactions = currentMonthTx.take(5).map { it.toTransaction() }
                )
                Result.success(summary)
            } else {
                val response = api.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch summary"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. BUDGET MANAGEMENT
    // ==========================================
    suspend fun setBudget(monthlyLimit: Long, monthYear: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            } else monthYear

            if (isGuest) {
                db.budgetDao().insertBudget(
                    LocalBudgetEntity(
                        monthYear = my,
                        monthlyLimit = monthlyLimit,
                        isSynced = false
                    )
                )
                Result.success(true)
            } else {
                val response = api.setBudget(SetBudgetRequest(monthlyLimit, my))
                if (response.isSuccessful) {
                    db.budgetDao().insertBudget(
                        LocalBudgetEntity(monthYear = my, monthlyLimit = monthlyLimit, isSynced = true)
                    )
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to set budget"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. TRANSACTIONS
    // ==========================================
    suspend fun getTransactions(category: String? = null, page: Int = 1, limit: Int = 50): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                val entities = if (category.isNullOrBlank()) {
                    db.transactionDao().getAllTransactions()
                } else {
                    db.transactionDao().getTransactionsByCategory(category)
                }
                Result.success(entities.map { it.toTransaction() })
            } else {
                val response = api.getTransactions(category = category, page = page, limit = limit)
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.data
                    // Cache to local Room
                    db.transactionDao().insertTransactions(list.map { LocalTransactionEntity.fromTransaction(it, true) })
                    Result.success(list)
                } else {
                    // Offline fallback from Room Cache
                    val entities = db.transactionDao().getAllTransactions()
                    if (entities.isNotEmpty()) {
                        Result.success(entities.map { it.toTransaction() })
                    } else {
                        Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch transactions"))
                    }
                }
            }
        } catch (e: Exception) {
            // Local fallback
            val entities = db.transactionDao().getAllTransactions()
            if (entities.isNotEmpty()) {
                Result.success(entities.map { it.toTransaction() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> = withContext(Dispatchers.IO) {
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
                walletId = request.walletId
            )

            // Adjust local wallet balance if provided
            if (request.walletId != null) {
                val amountAdj = if (request.type == com.ssajudn.barebudget.data.model.TransactionType.INCOME) request.amount else -request.amount
                db.walletDao().updateBalance(request.walletId, amountAdj)
            }

            if (isGuest) {
                db.transactionDao().insertTransaction(
                    LocalTransactionEntity.fromTransaction(newTx, isSynced = false)
                )
                Result.success(newTx)
            } else {
                val response = api.createTransaction(request)
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!
                    db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(created, isSynced = true))
                    Result.success(created)
                } else {
                    // Save offline to Room with isSynced = false
                    db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(newTx, isSynced = false))
                    Result.success(newTx)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val tx = db.transactionDao().getTransactionById(id)
            if (tx != null && tx.walletId != null) {
                val txType = try { 
                    com.ssajudn.barebudget.data.model.TransactionType.valueOf(tx.type) 
                } catch (e: Exception) { 
                    com.ssajudn.barebudget.data.model.TransactionType.EXPENSE 
                }
                // Reverse the balance
                val amountAdj = if (txType == com.ssajudn.barebudget.data.model.TransactionType.INCOME) -tx.amount else tx.amount
                db.walletDao().updateBalance(tx.walletId, amountAdj)
            }

            db.transactionDao().deleteTransaction(id)
            if (!isGuest) {
                api.deleteTransaction(id)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. DUE BILLS
    // ==========================================
    suspend fun getDueBills(status: String? = null): Result<List<DueBill>> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                val entities = if (status.isNullOrBlank()) {
                    db.dueBillDao().getAllDueBills()
                } else {
                    db.dueBillDao().getDueBillsByStatus(status)
                }
                Result.success(entities.map { it.toDueBill() })
            } else {
                val response = api.getDueBills(status)
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.data
                    db.dueBillDao().insertDueBills(list.map { LocalDueBillEntity.fromDueBill(it, true) })
                    Result.success(list)
                } else {
                    val entities = db.dueBillDao().getAllDueBills()
                    Result.success(entities.map { it.toDueBill() })
                }
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
                totalAmount = request.totalAmount,
                dueDate = request.dueDate,
                status = DueBillStatus.UNPAID,
                notes = request.notes
            )

            if (isGuest) {
                db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(newBill, isSynced = false))
                Result.success(newBill)
            } else {
                val response = api.createDueBill(request)
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!
                    db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(created, isSynced = true))
                    Result.success(created)
                } else {
                    db.dueBillDao().insertDueBill(LocalDueBillEntity.fromDueBill(newBill, isSynced = false))
                    Result.success(newBill)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDueBillStatus(id: String, status: DueBillStatus): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.dueBillDao().updateDueBillStatus(id, status.name)
            if (!isGuest) {
                api.updateDueBillStatus(id, UpdateDueBillStatusRequest(status))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDueBill(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.dueBillDao().deleteDueBill(id)
            if (!isGuest) {
                api.deleteDueBill(id)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 5. SAVINGS GOALS (OFFLINE-FIRST)
    // ==========================================
    suspend fun getGoals(): Result<List<Goal>> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                val local = db.goalDao().getAllGoals().map { it.toGoal() }
                return@withContext Result.success(local)
            }

            val response = api.getGoals()
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!.data
                // Cache into Room
                db.goalDao().clearAll()
                db.goalDao().insertGoals(remoteList.map { LocalGoalEntity.fromGoal(it, isSynced = true) })
                Result.success(remoteList)
            } else {
                val cached = db.goalDao().getAllGoals().map { it.toGoal() }
                Result.success(cached)
            }
        } catch (e: Exception) {
            val fallback = db.goalDao().getAllGoals().map { it.toGoal() }
            if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun createGoal(request: CreateGoalRequest): Result<Goal> = withContext(Dispatchers.IO) {
        val tempId = UUID.randomUUID().toString()
        val localGoal = Goal(
            id = tempId,
            name = request.name,
            targetAmount = request.targetAmount,
            currentAmount = 0L,
            targetDate = request.targetDate,
            colorHex = request.colorHex,
            notes = request.notes
        )

        // Optimistic local insert
        db.goalDao().insertGoal(LocalGoalEntity.fromGoal(localGoal, isSynced = false))

        if (isGuest) {
            return@withContext Result.success(localGoal)
        }

        try {
            val response = api.createGoal(request)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                db.goalDao().deleteGoal(tempId)
                db.goalDao().insertGoal(LocalGoalEntity.fromGoal(created, isSynced = true))
                Result.success(created)
            } else {
                Result.success(localGoal)
            }
        } catch (e: Exception) {
            Result.success(localGoal) // return optimistic fallback
        }
    }

    suspend fun depositToGoal(id: String, amount: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        // Optimistic local update
        db.goalDao().depositToGoal(id, amount)

        if (isGuest) {
            return@withContext Result.success(true)
        }

        try {
            val response = api.depositGoal(id, DepositGoalRequest(amount))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.success(true) // local already updated
            }
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    suspend fun deleteGoal(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        db.goalDao().deleteGoal(id)

        if (isGuest) {
            return@withContext Result.success(true)
        }

        try {
            api.deleteGoal(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    // ==========================================
    // 6. SMART MERGE (MIGRATE LOCAL ROOM -> CLOUD)
    // ==========================================
    suspend fun migrateGuestData(guestUserId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Batch sync all unsynced local transactions to Cloud
            val localTx = db.transactionDao().getAllTransactions()
            for (tx in localTx) {
                api.createTransaction(
                    CreateTransactionRequest(
                        amount = tx.amount,
                        category = try { TransactionCategory.valueOf(tx.category) } catch (e: Exception) { TransactionCategory.OTHER },
                        merchant = tx.merchant ?: "",
                        date = tx.date,
                        notes = tx.notes ?: "",
                        receiptUrl = tx.receiptUrl ?: ""
                    )
                )
            }

            // 2. Batch sync all unsynced local due bills to Cloud
            val localBills = db.dueBillDao().getAllDueBills()
            for (bill in localBills) {
                api.createDueBill(
                    CreateDueBillRequest(
                        providerName = bill.providerName,
                        totalAmount = bill.totalAmount,
                        dueDate = bill.dueDate,
                        notes = bill.notes ?: ""
                    )
                )
            }

            // 3. Sync local goals to Cloud
            val localGoals = db.goalDao().getAllGoals()
            for (g in localGoals) {
                val created = api.createGoal(
                    CreateGoalRequest(
                        name = g.name,
                        targetAmount = g.targetAmount,
                        targetDate = g.targetDate ?: "",
                        colorHex = g.colorHex,
                        notes = g.notes ?: ""
                    )
                )
                if (created.isSuccessful && created.body()?.id != null && g.currentAmount > 0) {
                    api.depositGoal(created.body()!!.id!!, DepositGoalRequest(g.currentAmount))
                }
            }

            // 4. Sync local budget to Cloud
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val localBudget = db.budgetDao().getBudget(monthYear)
            if (localBudget != null && localBudget.monthlyLimit > 0) {
                api.setBudget(SetBudgetRequest(localBudget.monthlyLimit, monthYear))
            }

            // 5. Trigger backend DB migration endpoint as secondary safety

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ==========================================
        suspend fun getWallets(): Result<List<Wallet>> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                val local = db.walletDao().getAllWallets().map { it.toWallet() }
                if (local.isEmpty()) {
                    val defaultWallet = Wallet(
                        id = UUID.randomUUID().toString(),
                        name = "Uang Tunai",
                        balance = 0L,
                        colorHex = "#2ECC71",
                        iconName = "account_balance_wallet"
                    )
                    db.walletDao().insertWallet(com.ssajudn.barebudget.data.local.room.LocalWalletEntity.fromWallet(defaultWallet, isSynced = false))
                    Result.success(listOf(defaultWallet))
                } else {
                    Result.success(local)
                }
            } else {
                val response = api.getWallets()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch wallets"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createWallet(request: CreateWalletRequest): Result<Wallet> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                val wallet = Wallet(
                    id = UUID.randomUUID().toString(),
                    name = request.name,
                    balance = request.balance,
                    colorHex = request.colorHex,
                    iconName = request.iconName
                )
                db.walletDao().insertWallet(com.ssajudn.barebudget.data.local.room.LocalWalletEntity.fromWallet(wallet, isSynced = false))
                Result.success(wallet)
            } else {
                val response = api.createWallet(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create wallet"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWallet(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isGuest) {
                db.walletDao().deleteWallet(id)
                Result.success(true)
            } else {
                val response = api.deleteWallet(id)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete wallet"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

