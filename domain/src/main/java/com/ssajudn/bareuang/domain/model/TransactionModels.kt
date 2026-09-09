package com.ssajudn.bareuang.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class TransactionCategory(val iconName: String) {
    FOOD("restaurant"), TRANSPORT("directions_car"), BILLS("receipt_long"),
    SHOPPING("shopping_bag"), ENTERTAINMENT("sports_esports"), SOCIAL("groups"),
    SALARY("payments"), BONUS("redeem"), INVESTMENT("trending_up"),
    TRANSFER("swap_horiz"), OTHER("category")
}

data class Transaction(
    val id: String? = null,
    val amount: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory,
    val merchant: String? = null,
    val date: String,
    val notes: String? = null,
    val receiptUrl: String? = null,
    val walletId: String? = null,
    val toWalletId: String? = null,
    val createdAt: String? = null,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val isRecurringParent: Boolean = false,
    val parentRecurringId: String? = null,
    val nextOccurrenceDate: String? = null
)

enum class RecurringInterval { NONE, WEEKLY, MONTHLY, YEARLY }

data class CreateTransactionRequest(
    val amount: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory,
    val merchant: String,
    val date: String,
    val notes: String = "",
    val receiptUrl: String = "",
    val walletId: String? = null,
    val toWalletId: String? = null,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE
)
