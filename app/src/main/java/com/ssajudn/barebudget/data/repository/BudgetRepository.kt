package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.BareBudgetApplication
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalBudgetEntity
import com.ssajudn.barebudget.data.local.room.LocalDueBillEntity
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
                val totalSpent = currentMonthTx.sumOf { it.amount }

                val remainingBudget = monthlyBudget - totalSpent
                val avgDaily = if (daysPassed > 0) totalSpent / daysPassed else 0L

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
                category = request.category,
                merchant = request.merchant,
                date = dateStr,
                notes = request.notes,
                receiptUrl = request.receiptUrl
            )

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
    // 5. SMART MERGE (MIGRATE LOCAL ROOM -> CLOUD)
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

            // 3. Sync local budget to Cloud
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val localBudget = db.budgetDao().getBudget(monthYear)
            if (localBudget != null && localBudget.monthlyLimit > 0) {
                api.setBudget(SetBudgetRequest(localBudget.monthlyLimit, monthYear))
            }

            // 4. Trigger backend DB migration endpoint as secondary safety
            api.migrateGuestData(mapOf("guest_user_id" to guestUserId))

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
