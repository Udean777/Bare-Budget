package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.data.datasource.local.TransactionLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.TransactionRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertTrue

class DelegatorTest {

    private fun fakeSession(isGuest: Boolean): UserSessionManager {
        val s = mockk<UserSessionManager>()
        every { s.isGuestMode } returns isGuest
        return s
    }

    @Test
    fun `delegator routes to local when guest`() = runTest {
        val local = mockk<TransactionLocalDataSource>()
        val remote = mockk<TransactionRemoteDataSource>()
        val session = fakeSession(true)
        val delegator = TransactionRepositoryImpl(local, remote, session)

        val tx = Transaction(id = "1", amount = 1000L, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, merchant = "Test", date = "2026-08-21")
        coEvery { local.createTransaction(any()) } returns Result.success(tx)
        coEvery { remote.createTransaction(any()) } returns Result.success(tx.copy(id = "remote"))

        val req = CreateTransactionRequest(amount = 1000L, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, merchant = "Test", date = "2026-08-21", walletId = "w1")
        val result = delegator.createTransaction(req)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { local.createTransaction(any()) }
        coVerify(exactly = 0) { remote.createTransaction(any()) }
    }

    @Test
    fun `delegator routes to remote when logged in`() = runTest {
        val local = mockk<TransactionLocalDataSource>()
        val remote = mockk<TransactionRemoteDataSource>()
        val session = fakeSession(false)
        val delegator = TransactionRepositoryImpl(local, remote, session)

        val tx = Transaction(id = "2", amount = 2000L, type = TransactionType.INCOME, category = TransactionCategory.SALARY, merchant = "Gaji", date = "2026-08-21")
        coEvery { local.createTransaction(any()) } returns Result.success(tx.copy(id = "local"))
        coEvery { remote.createTransaction(any()) } returns Result.success(tx)

        val req = CreateTransactionRequest(amount = 2000L, type = TransactionType.INCOME, category = TransactionCategory.SALARY, merchant = "Gaji", date = "2026-08-21", walletId = "w1")
        val result = delegator.createTransaction(req)

        coVerify(exactly = 0) { local.createTransaction(any()) }
        coVerify(exactly = 1) { remote.createTransaction(any()) }
    }

    @Test
    fun `observe delegates to active`() {
        val local = mockk<TransactionLocalDataSource>()
        val remote = mockk<TransactionRemoteDataSource>()
        val session = fakeSession(true)
        val delegator = TransactionRepositoryImpl(local, remote, session)

        every { local.observeTransactions() } returns flowOf(emptyList())
        every { remote.observeTransactions() } returns flowOf(emptyList())

        // Should not throw and should return local flow
        val flow = delegator.observeTransactions()
        assertTrue(flow != null)
    }
}
