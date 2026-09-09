package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.DashboardTransactionData
import kotlinx.coroutines.flow.Flow

/**
 * Domain port — contract milik domain, implementasi di data.
 * Dependency Rule: data → domain, bukan sebaliknya.
 */
interface TransactionRepository {
    suspend fun getDashboardTransactions(monthYear: String, todayIso: String): Result<DashboardTransactionData> =
        getAllTransactions().map { transactions ->
            val current = transactions.filter { !it.isRecurringParent && it.date.startsWith(monthYear) }
            val expenses = current.filter { it.type.name == "EXPENSE" && it.category.name != "BILLS" }
            DashboardTransactionData(
                totalSpent = expenses.sumOf { it.amount },
                todaySpent = expenses.filter { it.date.take(10) == todayIso }.sumOf { it.amount },
                topCategories = com.ssajudn.bareuang.domain.usecase.BuildCategorySummaryUseCase(expenses),
                recentTransactions = com.ssajudn.bareuang.domain.usecase.GetRecentTransactionsUseCase(current),
                recurringTransactions = transactions.filter { it.isRecurringParent },
            )
        }
    suspend fun getTransactions(category: String? = null, page: Int = 1, limit: Int = 50): Result<List<Transaction>>
    /** Full dataset for calculations/import dedup; UI lists must use pagination. */
    suspend fun getAllTransactions(): Result<List<Transaction>> = getTransactions(page = 1, limit = Int.MAX_VALUE)
    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction>
    suspend fun bulkCreate(requests: List<CreateTransactionRequest>): Result<Int>
    suspend fun deleteTransaction(id: String): Result<Boolean>
    fun observeTransactions(): Flow<List<Transaction>>
}
