package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.RecurringInterval
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.UpdateDueBillRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private class FakeBudgetRepo(var budget: Long = 0L) : BudgetRepository {
    @Suppress("DEPRECATION")
    override suspend fun getDashboardSummary(): Result<DashboardSummary> = Result.success(
        DashboardSummary(
            monthlyBudget = budget, totalSpent = 0, remainingBudget = budget,
            daysPassed = 1, daysInMonth = 30, averageDailySpend = 0,
            estimatedDeathDay = 30, runwayMessage = "", topCategories = emptyList(),
            unpaidDueBillsSum = 0, netWorth = 0, recentTransactions = emptyList()
        )
    )
    override suspend fun setBudget(monthlyLimit: Long, monthYear: String) = Result.success(true)
    override suspend fun getMonthlyBudget(monthYear: String) = Result.success(budget)
}

private class FakeTxRepo(var txs: List<Transaction> = emptyList()) : TransactionRepository {
    override suspend fun getTransactions(category: String?, page: Int, limit: Int) = Result.success(txs)
    override suspend fun createTransaction(request: CreateTransactionRequest) = Result.success(txs.firstOrNull()!!)
    override suspend fun deleteTransaction(id: String) = Result.success(true)
    override fun observeTransactions(): Flow<List<Transaction>> = flowOf(txs)
}

private class FakeWalletRepo(var wallets: List<Wallet> = emptyList()) : WalletRepository {
    override suspend fun getWallets() = Result.success(wallets)
    override suspend fun createWallet(request: CreateWalletRequest) = Result.success(wallets.firstOrNull()!!)
    override suspend fun updateWallet(wallet: Wallet) = Result.success(Unit)
    override suspend fun deleteWallet(id: String) = Result.success(true)
    override fun observeWallets(): Flow<List<Wallet>> = flowOf(wallets)
}

private class FakeDueBillRepo(var bills: List<DueBill> = emptyList()) : DueBillRepository {
    override suspend fun getDueBills(status: String?) = Result.success(bills)
    override suspend fun createDueBill(request: CreateDueBillRequest) = Result.success(bills.firstOrNull()!!)
    override suspend fun updateDueBill(id: String, request: UpdateDueBillRequest) = Result.success(true)
    override suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String?) = Result.success(true)
    override suspend fun deleteDueBill(id: String) = Result.success(true)
    override fun observeDueBills(): Flow<List<DueBill>> = flowOf(bills)
}

class GetDashboardSummaryUseCaseTest {

    @Test
    fun `dashboard calculations with transactions`() = runTest {
        val now = Calendar.getInstance()
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysPassed = now.get(Calendar.DAY_OF_MONTH)

        val txs = listOf(
            Transaction(id = "1", amount = 100_000L, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, date = "$monthYear-01"),
            Transaction(id = "2", amount = 50_000L, type = TransactionType.EXPENSE, category = TransactionCategory.TRANSPORT, date = "$monthYear-02"),
            Transaction(id = "3", amount = 500_000L, type = TransactionType.INCOME, category = TransactionCategory.SALARY, date = "$monthYear-01")
        )
        val wallets = listOf(Wallet(id = "w1", name = "Main", balance = 1_000_000L))
        val bills = listOf(DueBill(id = "b1", providerName = "Listrik", totalAmount = 200_000L, dueDate = "$monthYear-25", status = DueBillStatus.UNPAID))

        val useCase = GetDashboardSummaryUseCase(
            budgetRepository = FakeBudgetRepo(budget = 1_000_000L),
            transactionRepository = FakeTxRepo(txs),
            walletRepository = FakeWalletRepo(wallets),
            dueBillRepository = FakeDueBillRepo(bills)
        )

        val res = useCase()
        assertTrue(res.isSuccess)
        val summary = res.getOrThrow()

        assertEquals(1_000_000L, summary.monthlyBudget)
        assertEquals(150_000L, summary.totalSpent)
        assertEquals(850_000L, summary.remainingBudget)
        assertEquals(daysPassed, summary.daysPassed)
        assertEquals(daysInMonth, summary.daysInMonth)
        assertEquals(1_000_000L, summary.netWorth)
        assertEquals(200_000L, summary.unpaidDueBillsSum)
    }
}
