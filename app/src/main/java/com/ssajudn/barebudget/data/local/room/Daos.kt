package com.ssajudn.barebudget.data.local.room

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM local_transactions ORDER BY date DESC")
    fun getAllTransactions(): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): List<LocalTransactionEntity>

    @Query("SELECT * FROM local_transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: String): LocalTransactionEntity?

    @Query("SELECT * FROM local_transactions WHERE isSynced = 0")
    fun getUnsyncedTransactions(): List<LocalTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: LocalTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(transactions: List<LocalTransactionEntity>)

    @Query("DELETE FROM local_transactions WHERE id = :id")
    fun deleteTransaction(id: String)

    @Query("DELETE FROM local_transactions")
    fun clearAll()
}

@Dao
interface DueBillDao {
    @Query("SELECT * FROM local_due_bills ORDER BY dueDate ASC")
    fun getAllDueBills(): List<LocalDueBillEntity>

    @Query("SELECT * FROM local_due_bills WHERE status = :status ORDER BY dueDate ASC")
    fun getDueBillsByStatus(status: String): List<LocalDueBillEntity>

    @Query("SELECT * FROM local_due_bills WHERE isSynced = 0")
    fun getUnsyncedDueBills(): List<LocalDueBillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDueBill(bill: LocalDueBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDueBills(bills: List<LocalDueBillEntity>)

    @Query("UPDATE local_due_bills SET status = :status WHERE id = :id")
    fun updateDueBillStatus(id: String, status: String)

    @Query("DELETE FROM local_due_bills WHERE id = :id")
    fun deleteDueBill(id: String)

    @Query("DELETE FROM local_due_bills")
    fun clearAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM local_budgets WHERE monthYear = :monthYear LIMIT 1")
    fun getBudget(monthYear: String): LocalBudgetEntity?

    @Query("SELECT * FROM local_budgets WHERE isSynced = 0")
    fun getUnsyncedBudgets(): List<LocalBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudget(budget: LocalBudgetEntity)

    @Query("DELETE FROM local_budgets")
    fun clearAll()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM local_goals")
    fun getAllGoals(): List<LocalGoalEntity>

    @Query("SELECT * FROM local_goals WHERE id = :id LIMIT 1")
    fun getGoalById(id: String): LocalGoalEntity?

    @Query("SELECT * FROM local_goals WHERE isSynced = 0")
    fun getUnsyncedGoals(): List<LocalGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGoal(goal: LocalGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGoals(goals: List<LocalGoalEntity>)

    @Query("UPDATE local_goals SET currentAmount = currentAmount + :amount, isSynced = 0 WHERE id = :id")
    fun depositToGoal(id: String, amount: Long)

    @Query("DELETE FROM local_goals WHERE id = :id")
    fun deleteGoal(id: String)

    @Query("DELETE FROM local_goals")
    fun clearAll()
}
