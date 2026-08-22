package com.ssajudn.barebudget.domain.repository

import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain port — contract milik domain, implementasi di data.
 * Dependency Rule: data → domain, bukan sebaliknya.
 */
interface TransactionRepository {
    suspend fun getTransactions(category: String? = null, page: Int = 1, limit: Int = 50): Result<List<Transaction>>
    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction>
    suspend fun deleteTransaction(id: String): Result<Boolean>
    fun observeTransactions(): Flow<List<Transaction>>
}
