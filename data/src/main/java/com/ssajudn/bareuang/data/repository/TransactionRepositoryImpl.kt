package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.DashboardTransactionData
import com.ssajudn.bareuang.data.datasource.local.TransactionLocalDataSource
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val local: TransactionLocalDataSource
) : TransactionRepository {

    override suspend fun getDashboardTransactions(monthYear: String, todayIso: String): Result<DashboardTransactionData> =
        local.getDashboardTransactions(monthYear, todayIso)

    override suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        local.getTransactions(category, page, limit)

    override suspend fun getAllTransactions(): Result<List<Transaction>> = local.getAllTransactions()

    override suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        local.createTransaction(request)

    override suspend fun bulkCreate(requests: List<CreateTransactionRequest>): Result<Int> =
        local.bulkCreate(requests)

    override suspend fun deleteTransaction(id: String): Result<Boolean> =
        local.deleteTransaction(id)

    override fun observeTransactions(): Flow<List<Transaction>> =
        local.observeTransactions()
}
