package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateTransactionUseCaseTest {
    @Test
    fun `delegates request to transaction repository`() = runBlocking {
        val expected = Transaction(id = "tx-1", amount = 25_000L, category = com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD, date = "2026-09-03")
        var received: CreateTransactionRequest? = null
        val repository = object : TransactionRepository {
            override suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> {
                received = request
                return Result.success(expected)
            }
            override suspend fun getTransactions(category: String?, page: Int, limit: Int) = Result.success(emptyList<Transaction>())
            override suspend fun bulkCreate(requests: List<CreateTransactionRequest>) = Result.success(requests.size)
            override suspend fun deleteTransaction(id: String) = Result.success(true)
            override fun observeTransactions(): Flow<List<Transaction>> = emptyFlow()
        }
        val request = CreateTransactionRequest(
            amount = 25_000L,
            category = com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD,
            merchant = "Test",
            date = "2026-09-03",
        )

        val result = CreateTransactionUseCase(repository)(request)

        assertEquals(expected, result.getOrThrow())
        assertEquals(request, received)
    }
}
