package com.ssajudn.barebudget.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.RecurringInterval
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import java.util.UUID

import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.data.repository.DomainMappers

@Entity(tableName = "local_transactions")
data class LocalTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,
    val type: String = TransactionType.EXPENSE.name,
    val category: String,
    val merchant: String?,
    val date: String,
    val notes: String?,
    val receiptUrl: String?,
    val walletId: String? = null,
    val toWalletId: String? = null,
    val isSynced: Boolean = false
) {
    fun toTransaction(): Transaction {
        val cat = DomainMappers.safeCategory(category)
        val txType = DomainMappers.safeTransactionType(type)
        return Transaction(
            id = id,
            amount = amount,
            type = txType,
            category = cat,
            merchant = merchant,
            date = date,
            notes = notes,
            receiptUrl = receiptUrl,
            walletId = walletId,
            toWalletId = toWalletId
        )
    }

    companion object {
        fun fromTransaction(tx: Transaction, isSynced: Boolean = false): LocalTransactionEntity {
            return LocalTransactionEntity(
                id = tx.id ?: UUID.randomUUID().toString(),
                amount = tx.amount,
                type = tx.type.name,
                category = tx.category.name,
                merchant = tx.merchant,
                date = tx.date,
                notes = tx.notes,
                receiptUrl = tx.receiptUrl,
                walletId = tx.walletId,
                toWalletId = tx.toWalletId,
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
    val paidWalletId: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String = "NONE",
    val notes: String?,
    val isSynced: Boolean = false
) {
    fun toDueBill(): DueBill {
        val s = runCatching { DueBillStatus.valueOf(status) }
            .onFailure { println("[Entities] Unknown DueBillStatus: $status: ${it.message}") }
            .getOrDefault(DueBillStatus.UNPAID)
        val interval = DomainMappers.safeRecurringInterval(recurringInterval)
        return DueBill(
            id = id,
            providerName = providerName,
            providerIconUrl = providerIconUrl,
            totalAmount = totalAmount,
            dueDate = dueDate,
            status = s,
            paidWalletId = paidWalletId,
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
                paidWalletId = bill.paidWalletId,
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
    fun toGoal(): Goal {
        return Goal(
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
        fun fromGoal(goal: Goal, isSynced: Boolean = false): LocalGoalEntity {
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

@Entity(tableName = "local_wallets")
data class LocalWalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Long,
    val colorHex: String,
    val iconName: String,
    val createdAt: String,
    val isSynced: Boolean = false
) {
    fun toWallet(): Wallet {
        return Wallet(
            id = id,
            name = name,
            balance = balance,
            colorHex = colorHex,
            iconName = iconName,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromWallet(wallet: Wallet, isSynced: Boolean = false): LocalWalletEntity {
            return LocalWalletEntity(
                id = wallet.id ?: UUID.randomUUID().toString(),
                name = wallet.name,
                balance = wallet.balance,
                colorHex = wallet.colorHex,
                iconName = wallet.iconName,
                createdAt = wallet.createdAt ?: com.ssajudn.barebudget.utils.DateUtils.getCurrentDateISO(),
                isSynced = isSynced
            )
        }
    }
}
