package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.DueBillDao
import com.ssajudn.bareuang.data.local.room.GoalDao
import com.ssajudn.bareuang.data.local.room.LocalDueBillEntity
import com.ssajudn.bareuang.data.local.room.LocalGoalEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.data.local.room.TransactionDao
import com.ssajudn.bareuang.data.local.room.MonthlyCashflowProjection
import com.ssajudn.bareuang.data.local.room.CategoryAggregateProjection
import com.ssajudn.bareuang.data.local.room.WalletDao
import com.ssajudn.bareuang.data.local.room.BudgetDao
import com.ssajudn.bareuang.data.local.room.LocalBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalWalletEntity
import com.ssajudn.bareuang.domain.model.CreateGoalRequest
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.data.datasource.local.DueBillLocalDataSource
import com.ssajudn.bareuang.data.datasource.local.GoalLocalDataSource
import com.ssajudn.bareuang.data.service.WalletBalanceService
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Minimal fakes — only methods used by LocalGoal/DueBill
private class FakeWalletDao2 : WalletDao {
    val balances = mutableMapOf<String, Long>()
    override fun getAllWallets(): List<LocalWalletEntity> = balances.map { (id, bal) ->
        LocalWalletEntity(id = id, name = id, balance = bal, colorHex = "#000", iconName = "wallet", createdAt = "2026-08-21", isSynced = false)
    }
    override fun observeAllWallets(): Flow<List<LocalWalletEntity>> = flowOf(getAllWallets())
    override fun getFirstWallet(): LocalWalletEntity? = null
    override fun getWalletById(id: String): LocalWalletEntity? = getAllWallets().firstOrNull { it.id == id }
    override fun insertWallet(wallet: LocalWalletEntity) { balances[wallet.id] = wallet.balance }
    override fun insertWallets(wallets: List<LocalWalletEntity>) { wallets.forEach { balances[it.id] = it.balance } }
    override fun updateBalance(id: String, amount: Long) { balances[id] = (balances[id] ?: 0L) + amount }
    override fun deleteWallet(id: String) { balances.remove(id) }
    override fun clearAll() { balances.clear() }
}

private class FakeGoalDao2 : GoalDao {
    val goals = mutableMapOf<String, LocalGoalEntity>()
    override fun getAllGoals(): List<LocalGoalEntity> = goals.values.toList()
    override fun observeAllGoals(): Flow<List<LocalGoalEntity>> = flowOf(getAllGoals())
    override fun getGoalById(id: String): LocalGoalEntity? = goals[id]
    override fun insertGoal(goal: LocalGoalEntity) { goals[goal.id] = goal }
    override fun insertGoals(goals: List<LocalGoalEntity>) { goals.forEach { this.goals[it.id] = it } }
    override fun depositToGoal(id: String, amount: Long) { goals[id]?.let { goals[id] = it.copy(currentAmount = it.currentAmount + amount) } }
    override fun updateGoal(id: String, name: String, targetAmount: Long, targetDate: String?, colorHex: String, notes: String?, isSynced: Boolean) {}
    override fun deleteGoal(id: String) { goals.remove(id) }
    override fun clearAll() { goals.clear() }
}

private class FakeDueBillDao2 : DueBillDao {
    val bills = mutableMapOf<String, LocalDueBillEntity>()
    override fun getAllDueBills(): List<LocalDueBillEntity> = bills.values.toList()
    override fun observeAllDueBills(): Flow<List<LocalDueBillEntity>> = flowOf(getAllDueBills())
    override fun getDueBillsByStatus(status: String): List<LocalDueBillEntity> = bills.values.filter { it.status == status }
    override fun getUnpaidTotal(): Long = bills.values.filter { it.status == DueBillStatus.UNPAID.name }.sumOf { it.totalAmount }
    override fun getDueBillById(id: String): LocalDueBillEntity? = bills[id]
    override fun insertDueBill(bill: LocalDueBillEntity) { bills[bill.id] = bill }
    override fun insertDueBills(bills: List<LocalDueBillEntity>) { bills.forEach { this.bills[it.id] = it } }
    override fun updateDueBillStatus(id: String, status: String, paidWalletId: String?) { bills[id]?.let { bills[id] = it.copy(status = status, paidWalletId = paidWalletId) } }
    override fun updateDueBill(id: String, providerName: String, providerIconUrl: String?, totalAmount: Long, dueDate: String, isRecurring: Boolean, recurringInterval: String, notes: String, isSynced: Boolean) {}
    override fun deleteDueBill(id: String) { bills.remove(id) }
    override fun clearAll() { bills.clear() }
}

private class FakeTxDao2 : TransactionDao {
    val txs = mutableListOf<LocalTransactionEntity>()
    override fun getAllTransactions(): List<LocalTransactionEntity> = txs.toList()
    override fun getTransactionsPaged(limit: Int, offset: Int): List<LocalTransactionEntity> = txs.drop(offset).take(limit)
    override fun observeAllTransactions(): Flow<List<LocalTransactionEntity>> = flowOf(txs.toList())
    override fun getTransactionsByCategory(category: String): List<LocalTransactionEntity> = txs.filter { it.category == category }
    override fun getTransactionsByCategoryPaged(category: String, limit: Int, offset: Int): List<LocalTransactionEntity> = txs.filter { it.category == category }.drop(offset).take(limit)
    override fun getTransactionsByDateRange(fromDate: String, toDate: String, limit: Int): List<LocalTransactionEntity> =
        txs.filter { it.date >= fromDate && it.date < toDate }.sortedByDescending { it.date }.take(limit)
    override fun getTransactionById(id: String): LocalTransactionEntity? = txs.find { it.id == id }
    override fun insertTransaction(transaction: LocalTransactionEntity) { txs.add(transaction) }
    override fun insertTransactions(transactions: List<LocalTransactionEntity>) { txs.addAll(transactions) }
    override fun getByDates(dates: List<String>): List<LocalTransactionEntity> = txs.filter { it.date in dates }
    override fun getRecurringTemplates(): List<LocalTransactionEntity> = txs.filter { it.isRecurringParent }
    override fun getCashflowByMonth(fromDate: String, toDate: String): List<MonthlyCashflowProjection> = emptyList()
    override fun getExpenseByCategory(fromDate: String, toDate: String): List<CategoryAggregateProjection> = emptyList()
    override fun getExpenseTotal(fromDate: String, toDate: String): Long = 0L
    override fun getDiscretionaryExpenseTotal(fromDate: String, toDate: String): Long = 0L
    override fun getDiscretionaryExpenseTotalForDay(fromDate: String, toDate: String): Long = 0L
    override fun getIncomeTotal(fromDate: String, toDate: String): Long = 0L
    override fun updateNextOccurrence(id: String, nextDate: String) {
        val index = txs.indexOfFirst { it.id == id }
        if (index != -1) {
            txs[index] = txs[index].copy(nextOccurrenceDate = nextDate)
        }
    }
    override fun deleteTransaction(id: String) { txs.removeIf { it.id == id } }
    override fun clearAll() { txs.clear() }
}

private class FakeBudgetDao2 : BudgetDao {
    override fun getAllBudgets(): List<LocalBudgetEntity> = emptyList()
    override fun getBudget(monthYear: String): LocalBudgetEntity? = null
    override fun observeBudget(monthYear: String): Flow<LocalBudgetEntity?> = flowOf(null)
    override fun insertBudget(budget: LocalBudgetEntity) {}
    override fun insertBudgets(budgets: List<LocalBudgetEntity>) {}
    override fun clearAll() {}
    override fun getAllCategoryBudgets(): List<com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity> = emptyList()
    override fun getCategoryBudgets(monthYear: String): List<com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity> = emptyList()
    override fun observeCategoryBudgets(monthYear: String): Flow<List<com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity>> = flowOf(emptyList())
    override fun insertCategoryBudget(categoryBudget: com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity) {}
    override fun insertCategoryBudgets(categoryBudgets: List<com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity>) {}
    override fun deleteCategoryBudget(monthYear: String, category: String) {}
    override fun clearAllCategoryBudgets() {}
}

private fun fakeDb(
    walletDao: WalletDao,
    goalDao: GoalDao,
    dueBillDao: DueBillDao,
    txDao: TransactionDao
): AppDatabase {
    val db = mockk<AppDatabase>(relaxed = true)
    io.mockk.every { db.walletDao() } returns walletDao
    io.mockk.every { db.goalDao() } returns goalDao
    io.mockk.every { db.dueBillDao() } returns dueBillDao
    io.mockk.every { db.transactionDao() } returns txDao
    io.mockk.every { db.budgetDao() } returns FakeBudgetDao2()
    io.mockk.every { db.runInTransaction(any<Runnable>()) } answers {
        val runnable = firstArg<Runnable>()
        runnable.run()
    }
    return db
}

class LocalGoalDueBillTest {

    @Test
    fun `depositToGoal subtracts wallet and creates transaction`() = runTest {
        val walletDao = FakeWalletDao2().apply { balances["w1"] = 500_000L }
        val goalDao = FakeGoalDao2().apply {
            goals["g1"] = LocalGoalEntity(id = "g1", name = "Tabungan", targetAmount = 1_000_000L, currentAmount = 100_000L, targetDate = null, colorHex = "#000", notes = null, isSynced = false)
        }
        val txDao = FakeTxDao2()
        val db = fakeDb(walletDao, goalDao, FakeDueBillDao2(), txDao)
        val svc = WalletBalanceService(walletDao)
        val repo = GoalLocalDataSource(db, svc)

        val result = repo.depositToGoal("g1", 50_000L, "w1")

        assertTrue(result.isSuccess)
        assertEquals(450_000L, walletDao.balances["w1"])
        assertEquals(150_000L, goalDao.goals["g1"]?.currentAmount)
        assertEquals(1, txDao.txs.size)
        // Deposit creates EXPENSE transaction
        assertEquals(50_000L, txDao.txs.first().amount)
    }

    @Test
    fun `withdraw from goal adds to wallet`() = runTest {
        val walletDao = FakeWalletDao2().apply { balances["w1"] = 100_000L }
        val goalDao = FakeGoalDao2().apply {
            goals["g1"] = LocalGoalEntity(id = "g1", name = "Tabungan", targetAmount = 1_000_000L, currentAmount = 200_000L, targetDate = null, colorHex = "#000", notes = null, isSynced = false)
        }
        val txDao = FakeTxDao2()
        val db = fakeDb(walletDao, goalDao, FakeDueBillDao2(), txDao)
        val repo = GoalLocalDataSource(db, WalletBalanceService(walletDao))

        val result = repo.depositToGoal("g1", -30_000L, "w1")

        assertTrue(result.isSuccess)
        assertEquals(130_000L, walletDao.balances["w1"])
        assertEquals(170_000L, goalDao.goals["g1"]?.currentAmount)
    }

    @Test
    fun `mark due bill paid subtracts wallet and creates transaction`() = runTest {
        val walletDao = FakeWalletDao2().apply { balances["w1"] = 300_000L }
        val dueBillDao = FakeDueBillDao2().apply {
            bills["b1"] = LocalDueBillEntity(id = "b1", providerName = "PLN", providerIconUrl = null, totalAmount = 100_000L, dueDate = "2026-08-30", status = DueBillStatus.UNPAID.name, isRecurring = false, recurringInterval = RecurringInterval.NONE.name, notes = null, isSynced = false)
        }
        val txDao = FakeTxDao2()
        val db = fakeDb(walletDao, FakeGoalDao2(), dueBillDao, txDao)
        val currencyPrefs3 = run {
            val prefs = io.mockk.mockk<android.content.SharedPreferences>(relaxed = true) { io.mockk.every { getString(any(), any()) } returns "IDR" }
            val ctx = io.mockk.mockk<android.content.Context>(relaxed = true) { io.mockk.every { getSharedPreferences(any(), any()) } returns prefs; io.mockk.every { applicationContext } returns this }
            com.ssajudn.bareuang.data.local.CurrencyPreferences(ctx)
        }
        val repo = DueBillLocalDataSource(db, WalletBalanceService(walletDao), currencyPrefs3)

        val result = repo.updateDueBillStatus("b1", DueBillStatus.PAID, "w1")

        assertTrue(result.isSuccess)
        assertEquals(200_000L, walletDao.balances["w1"])
        assertEquals(DueBillStatus.PAID.name, dueBillDao.bills["b1"]?.status)
        assertEquals(1, txDao.txs.size)
    }

    @Test
    fun `unmark paid refunds wallet`() = runTest {
        val walletDao = FakeWalletDao2().apply { balances["w1"] = 200_000L }
        val dueBillDao = FakeDueBillDao2().apply {
            bills["b1"] = LocalDueBillEntity(id = "b1", providerName = "PLN", providerIconUrl = null, totalAmount = 80_000L, dueDate = "2026-08-30", status = DueBillStatus.PAID.name, paidWalletId = "w1", isRecurring = false, recurringInterval = RecurringInterval.NONE.name, notes = null, isSynced = false)
        }
        val txDao = FakeTxDao2()
        val db = fakeDb(walletDao, FakeGoalDao2(), dueBillDao, txDao)
        val currencyPrefs4 = run {
            val prefs = io.mockk.mockk<android.content.SharedPreferences>(relaxed = true) { io.mockk.every { getString(any(), any()) } returns "IDR" }
            val ctx = io.mockk.mockk<android.content.Context>(relaxed = true) { io.mockk.every { getSharedPreferences(any(), any()) } returns prefs; io.mockk.every { applicationContext } returns this }
            com.ssajudn.bareuang.data.local.CurrencyPreferences(ctx)
        }
        val repo = DueBillLocalDataSource(db, WalletBalanceService(walletDao), currencyPrefs4)

        val result = repo.updateDueBillStatus("b1", DueBillStatus.UNPAID, null)

        assertTrue(result.isSuccess)
        assertEquals(280_000L, walletDao.balances["w1"])
        assertEquals(DueBillStatus.UNPAID.name, dueBillDao.bills["b1"]?.status)
        // Refund creates INCOME transaction
        assertEquals(1, txDao.txs.size)
    }
}
