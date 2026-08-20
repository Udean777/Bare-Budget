package com.ssajudn.barebudget.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssajudn.barebudget.data.model.DueBill
import com.ssajudn.barebudget.data.model.DueBillStatus
import com.ssajudn.barebudget.data.model.Transaction
import com.ssajudn.barebudget.data.model.TransactionCategory
import java.util.UUID

@Entity(tableName = "local_transactions")
data class LocalTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,
    val category: String,
    val merchant: String?,
    val date: String,
    val notes: String?,
    val receiptUrl: String?,
    val isSynced: Boolean = false
) {
    fun toTransaction(): Transaction {
        val cat = try {
            TransactionCategory.valueOf(category)
        } catch (e: Exception) {
            TransactionCategory.OTHER
        }
        return Transaction(
            id = id,
            amount = amount,
            category = cat,
            merchant = merchant,
            date = date,
            notes = notes,
            receiptUrl = receiptUrl
        )
    }

    companion object {
        fun fromTransaction(tx: Transaction, isSynced: Boolean = false): LocalTransactionEntity {
            return LocalTransactionEntity(
                id = tx.id ?: UUID.randomUUID().toString(),
                amount = tx.amount,
                category = tx.category.name,
                merchant = tx.merchant,
                date = tx.date,
                notes = tx.notes,
                receiptUrl = tx.receiptUrl,
                isSynced = isSynced
            )
        }
    }
}

@Entity(tableName = "local_due_bills")
data class LocalDueBillEntity(
    @PrimaryKey val id: String,
    val providerName: String,
    val providerIconUrl: String?,
    val totalAmount: Long,
    val dueDate: String,
    val status: String,
    val isRecurring: Boolean = false,
    val recurringInterval: String = "NONE",
    val notes: String?,
    val isSynced: Boolean = false
) {
    fun toDueBill(): DueBill {
        val s = try {
            DueBillStatus.valueOf(status)
        } catch (e: Exception) {
            DueBillStatus.UNPAID
        }
        val interval = try {
            com.ssajudn.barebudget.data.model.RecurringInterval.valueOf(recurringInterval)
        } catch (e: Exception) {
            com.ssajudn.barebudget.data.model.RecurringInterval.NONE
        }
        return DueBill(
            id = id,
            providerName = providerName,
            providerIconUrl = providerIconUrl,
            totalAmount = totalAmount,
            dueDate = dueDate,
            status = s,
            isRecurring = isRecurring,
            recurringInterval = interval,
            notes = notes
        )
    }

    companion object {
        fun fromDueBill(bill: DueBill, isSynced: Boolean = false): LocalDueBillEntity {
            return LocalDueBillEntity(
                id = bill.id ?: UUID.randomUUID().toString(),
                providerName = bill.providerName,
                providerIconUrl = bill.providerIconUrl,
                totalAmount = bill.totalAmount,
                dueDate = bill.dueDate,
                status = bill.status.name,
                isRecurring = bill.isRecurring,
                recurringInterval = bill.recurringInterval.name,
                notes = bill.notes,
                isSynced = isSynced
            )
        }
    }
}

@Entity(tableName = "local_budgets")
data class LocalBudgetEntity(
    @PrimaryKey val monthYear: String,
    val monthlyLimit: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "local_goals")
data class LocalGoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val targetDate: String?,
    val colorHex: String = "#4E73DF",
    val notes: String?,
    val isSynced: Boolean = false
) {
    fun toGoal(): com.ssajudn.barebudget.data.model.Goal {
        return com.ssajudn.barebudget.data.model.Goal(
            id = id,
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate,
            colorHex = colorHex,
            notes = notes
        )
    }

    companion object {
        fun fromGoal(goal: com.ssajudn.barebudget.data.model.Goal, isSynced: Boolean = false): LocalGoalEntity {
            return LocalGoalEntity(
                id = goal.id ?: UUID.randomUUID().toString(),
                name = goal.name,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                targetDate = goal.targetDate,
                colorHex = goal.colorHex,
                notes = goal.notes,
                isSynced = isSynced
            )
        }
    }
}
