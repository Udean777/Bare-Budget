package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CreateDueBillRequest
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.UpdateDueBillRequest
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.Clock

private class FakeBudgetRepo(var budget: Long = 0L) : BudgetRepository {
    override suspend fun setBudget(monthlyLimit: Long, monthYear: String) = Result.success(true)
    override suspend fun getMonthlyBudget(monthYear: String) = Result.success(budget)
    override fun getCategoryBudgets(monthYear: String): Flow<List<com.ssajudn.bareuang.domain.model.CategoryBudget>> = flowOf(emptyList())
    override suspend fun setCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory, limitAmount: Long, monthYear: String) = Result.success(true)
    override suspend fun deleteCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory, monthYear: String) = Result.success(true)
}

private class FakeTxRepo(var txs: List<Transaction> = emptyList()) : TransactionRepository {
    var requestedLimit: Int = 0
    override suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> {
        requestedLimit = limit
        return Result.success(txs)
    }
    override suspend fun createTransaction(request: CreateTransactionRequest) = Result.success(txs.firstOrNull()!!)
    override suspend fun bulkCreate(requests: List<CreateTransactionRequest>) = Result.success(requests.size)
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

private class FakeDailyPacingPrefs : DailyPacingPreferencesPort {
    override val customTarget = MutableStateFlow<Long?>(null)
    override val lastCustomTarget = MutableStateFlow<Long?>(null)
    override fun setCustomTarget(amount: Long?) { customTarget.value = amount }
    override fun reset() { customTarget.value = null }
}

class GetDashboardSummaryUseCaseTest {

    private fun useCase(transactions: List<Transaction>, budget: Long = 1_000_000L) =
        GetDashboardSummaryUseCase(
            budgetRepository = FakeBudgetRepo(budget),
            transactionRepository = FakeTxRepo(transactions),
            walletRepository = FakeWalletRepo(),
            dueBillRepository = FakeDueBillRepo(),
            dailyPacingPreferences = FakeDailyPacingPrefs(),
            clock = Clock.systemDefaultZone(),
        )

    @Test
    fun `empty database produces zeroed summary`() = runTest {
        val summary = useCase(emptyList())().getOrThrow()

        assertEquals(0L, summary.totalSpent)
        assertEquals(1_000_000L, summary.remainingBudget)
        assertEquals(0L, summary.netWorth)
        assertEquals(0L, summary.unpaidDueBillsSum)
    }

    @Test
    fun `overflow is mapped to data error`() = runTest {
        val month = java.time.YearMonth.now().toString()
        val transactions = listOf(
            Transaction("1", Long.MAX_VALUE, TransactionType.EXPENSE, TransactionCategory.FOOD, date = "$month-01"),
            Transaction("2", 1L, TransactionType.EXPENSE, TransactionCategory.FOOD, date = "$month-02"),
        )

        val result = useCase(transactions)()

        assertTrue(result.exceptionOrNull() is com.ssajudn.bareuang.domain.error.AppException.DataException)
    }

    @Test
    fun `large transaction collection remains accurate`() = runTest {
        val month = java.time.YearMonth.now().toString()
        val transactions = List(10_000) { index ->
            Transaction(
                id = index.toString(),
                amount = 1L,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                date = "$month-01",
            )
        }

        val result = useCase(transactions)()

        assertTrue(result.isSuccess)
        assertEquals(10_000L, result.getOrThrow().totalSpent)
    }

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

        val txRepo = FakeTxRepo(txs)
        val useCase = GetDashboardSummaryUseCase(
            budgetRepository = FakeBudgetRepo(budget = 1_000_000L),
            transactionRepository = txRepo,
            walletRepository = FakeWalletRepo(wallets),
            dueBillRepository = FakeDueBillRepo(bills),
            dailyPacingPreferences = FakeDailyPacingPrefs(),
            clock = Clock.systemDefaultZone(),
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
        assertEquals(Int.MAX_VALUE, txRepo.requestedLimit)
    }
}
