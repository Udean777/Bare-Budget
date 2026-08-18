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
