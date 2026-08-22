package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.data.datasource.local.TransactionLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.TransactionRemoteDataSource
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val local: TransactionLocalDataSource,
    private val remote: TransactionRemoteDataSource,
    private val sessionManager: UserSessionManager
) : TransactionRepository {

    override suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        if (sessionManager.isGuestMode) local.getTransactions(category, page, limit)
        else remote.getTransactions(category, page, limit)

    override suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        if (sessionManager.isGuestMode) local.createTransaction(request)
        else remote.createTransaction(request)

    override suspend fun deleteTransaction(id: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.deleteTransaction(id)
        else remote.deleteTransaction(id)

    override fun observeTransactions(): Flow<List<Transaction>> =
        if (sessionManager.isGuestMode) local.observeTransactions()
        else remote.observeTransactions()
}
