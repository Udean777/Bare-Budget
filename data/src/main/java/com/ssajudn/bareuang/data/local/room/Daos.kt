package com.ssajudn.bareuang.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class MonthlyCashflowProjection(
    val month: String,
    val income: Long?,
    val expense: Long?
)

data class CategoryAggregateProjection(
    val category: String,
    val total: Long?,
    val count: Long
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM local_transactions ORDER BY date DESC")
    fun getAllTransactions(): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getTransactionsPaged(limit: Int, offset: Int): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions ORDER BY date DESC")
    fun observeAllTransactions(): Flow<List<LocalTransactionEntity>>

    @Query("SELECT * FROM local_transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE category = :category ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getTransactionsByCategoryPaged(category: String, limit: Int, offset: Int): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE date >= :fromDate AND date < :toDate AND isRecurringParent = 0 ORDER BY date DESC LIMIT :limit")
    fun getTransactionsByDateRange(fromDate: String, toDate: String, limit: Int): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: String): LocalTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: LocalTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(transactions: List<LocalTransactionEntity>)

    @Query("SELECT * FROM local_transactions WHERE isRecurringParent = 1 AND recurringInterval != 'NONE'")
    fun getRecurringTemplates(): List<LocalTransactionEntity>

    @Query("""
        SELECT substr(date, 1, 7) AS month,
               SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS income,
               SUM(CASE WHEN type = 'EXPENSE' OR type = '' THEN amount ELSE 0 END) AS expense
        FROM local_transactions
        WHERE date >= :fromDate AND date < :toDate
          AND isRecurringParent = 0
          AND type IN ('INCOME', 'EXPENSE', '')
        GROUP BY substr(date, 1, 7)
        ORDER BY month ASC
    """)
    fun getCashflowByMonth(fromDate: String, toDate: String): List<MonthlyCashflowProjection>

    @Query("""
        SELECT category, SUM(amount) AS total, COUNT(*) AS count
        FROM local_transactions
        WHERE date >= :fromDate AND date < :toDate
          AND isRecurringParent = 0 AND type = 'EXPENSE'
          AND category != 'BILLS'
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getExpenseByCategory(fromDate: String, toDate: String): List<CategoryAggregateProjection>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM local_transactions
        WHERE date >= :fromDate AND date < :toDate
          AND isRecurringParent = 0 AND type = 'EXPENSE'
    """)
    fun getExpenseTotal(fromDate: String, toDate: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM local_transactions WHERE date >= :fromDate AND date < :toDate AND isRecurringParent = 0 AND type = 'EXPENSE' AND category != 'BILLS'")
    fun getDiscretionaryExpenseTotal(fromDate: String, toDate: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM local_transactions WHERE date >= :fromDate AND date < :toDate AND isRecurringParent = 0 AND type = 'EXPENSE' AND category != 'BILLS'")
    fun getDiscretionaryExpenseTotalForDay(fromDate: String, toDate: String): Long

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM local_transactions
        WHERE date >= :fromDate AND date < :toDate
          AND isRecurringParent = 0 AND type = 'INCOME'
    """)
    fun getIncomeTotal(fromDate: String, toDate: String): Long

    @Query("UPDATE local_transactions SET nextOccurrenceDate = :nextDate WHERE id = :id")
    fun updateNextOccurrence(id: String, nextDate: String)

    @Query("DELETE FROM local_transactions WHERE id = :id")
    fun deleteTransaction(id: String)

    @Query("SELECT * FROM local_transactions WHERE date IN (:dates)")
    fun getByDates(dates: List<String>): List<LocalTransactionEntity>

    @Query("DELETE FROM local_transactions")
    fun clearAll()
}

@Dao
interface DueBillDao {
    @Query("SELECT * FROM local_due_bills ORDER BY dueDate ASC")
    fun getAllDueBills(): List<LocalDueBillEntity>

    @Query("SELECT * FROM local_due_bills ORDER BY dueDate ASC")
    fun observeAllDueBills(): Flow<List<LocalDueBillEntity>>

    @Query("SELECT * FROM local_due_bills WHERE status = :status ORDER BY dueDate ASC")
    fun getDueBillsByStatus(status: String): List<LocalDueBillEntity>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM local_due_bills WHERE status = 'UNPAID'")
    fun getUnpaidTotal(): Long

    @Query("SELECT * FROM local_due_bills WHERE id = :id LIMIT 1")
    fun getDueBillById(id: String): LocalDueBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDueBill(bill: LocalDueBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDueBills(bills: List<LocalDueBillEntity>)

    @Query("UPDATE local_due_bills SET status = :status, paidWalletId = :paidWalletId WHERE id = :id")
    fun updateDueBillStatus(id: String, status: String, paidWalletId: String?)

    @Query(
        "UPDATE local_due_bills SET providerName = :providerName, providerIconUrl = :providerIconUrl, " +
            "totalAmount = :totalAmount, dueDate = :dueDate, isRecurring = :isRecurring, " +
            "recurringInterval = :recurringInterval, notes = :notes, isSynced = :isSynced WHERE id = :id"
    )
    fun updateDueBill(
        id: String,
        providerName: String,
        providerIconUrl: String?,
        totalAmount: Long,
        dueDate: String,
        isRecurring: Boolean,
        recurringInterval: String,
        notes: String,
        isSynced: Boolean
    )

    @Query("DELETE FROM local_due_bills WHERE id = :id")
    fun deleteDueBill(id: String)

    @Query("DELETE FROM local_due_bills")
    fun clearAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM local_budgets ORDER BY monthYear DESC")
    fun getAllBudgets(): List<LocalBudgetEntity>

    @Query("SELECT * FROM local_budgets WHERE monthYear = :monthYear LIMIT 1")
    fun getBudget(monthYear: String): LocalBudgetEntity?

    @Query("SELECT * FROM local_budgets WHERE monthYear = :monthYear LIMIT 1")
    fun observeBudget(monthYear: String): Flow<LocalBudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudget(budget: LocalBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudgets(budgets: List<LocalBudgetEntity>)

    @Query("DELETE FROM local_budgets")
    fun clearAll()

    @Query("SELECT * FROM local_category_budgets WHERE monthYear = :monthYear")
    fun getCategoryBudgets(monthYear: String): List<LocalCategoryBudgetEntity>

    @Query("SELECT * FROM local_category_budgets ORDER BY monthYear DESC")
    fun getAllCategoryBudgets(): List<LocalCategoryBudgetEntity>

    @Query("SELECT * FROM local_category_budgets WHERE monthYear = :monthYear")
    fun observeCategoryBudgets(monthYear: String): Flow<List<LocalCategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoryBudget(categoryBudget: LocalCategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoryBudgets(categoryBudgets: List<LocalCategoryBudgetEntity>)

    @Query("DELETE FROM local_category_budgets WHERE monthYear = :monthYear AND category = :category")
    fun deleteCategoryBudget(monthYear: String, category: String)

    @Query("DELETE FROM local_category_budgets")
    fun clearAllCategoryBudgets()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM local_goals")
    fun getAllGoals(): List<LocalGoalEntity>

    @Query("SELECT * FROM local_goals")
    fun observeAllGoals(): Flow<List<LocalGoalEntity>>

    @Query("SELECT * FROM local_goals WHERE id = :id LIMIT 1")
    fun getGoalById(id: String): LocalGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGoal(goal: LocalGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGoals(goals: List<LocalGoalEntity>)

    @Query("UPDATE local_goals SET currentAmount = currentAmount + :amount, isSynced = 0 WHERE id = :id")
    fun depositToGoal(id: String, amount: Long)

    @Query("UPDATE local_goals SET name = :name, targetAmount = :targetAmount, targetDate = :targetDate, colorHex = :colorHex, notes = :notes, isSynced = :isSynced WHERE id = :id")
    fun updateGoal(id: String, name: String, targetAmount: Long, targetDate: String?, colorHex: String, notes: String?, isSynced: Boolean)

    @Query("DELETE FROM local_goals WHERE id = :id")
    fun deleteGoal(id: String)

    @Query("DELETE FROM local_goals")
    fun clearAll()
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM local_wallets ORDER BY createdAt ASC")
    fun getAllWallets(): List<LocalWalletEntity>

    @Query("SELECT * FROM local_wallets ORDER BY createdAt ASC")
    fun observeAllWallets(): Flow<List<LocalWalletEntity>>

    @Query("SELECT * FROM local_wallets ORDER BY createdAt ASC LIMIT 1")
    fun getFirstWallet(): LocalWalletEntity?

    @Query("SELECT * FROM local_wallets WHERE id = :id LIMIT 1")
    fun getWalletById(id: String): LocalWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallet(wallet: LocalWalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallets(wallets: List<LocalWalletEntity>)

    @Query("UPDATE local_wallets SET balance = balance + :amount WHERE id = :id")
    fun updateBalance(id: String, amount: Long)

    @Query("DELETE FROM local_wallets WHERE id = :id")
    fun deleteWallet(id: String)

    @Query("DELETE FROM local_wallets")
    fun clearAll()
}
