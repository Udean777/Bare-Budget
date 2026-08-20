package com.ssajudn.barebudget.data.model

import com.google.gson.annotations.SerializedName

enum class TransactionType {
    @SerializedName("INCOME")
    INCOME,

    @SerializedName("EXPENSE")
    EXPENSE,

    @SerializedName("TRANSFER")
    TRANSFER
}

enum class TransactionCategory(val displayName: String, val iconName: String) {
    @SerializedName("FOOD")
    FOOD("Food & Beverage", "restaurant"),

    @SerializedName("TRANSPORT")
    TRANSPORT("Transportation", "directions_car"),

    @SerializedName("BILLS")
    BILLS("Bills & Utilities", "receipt_long"),

    @SerializedName("SHOPPING")
    SHOPPING("Shopping & Groceries", "shopping_bag"),

    @SerializedName("ENTERTAINMENT")
    ENTERTAINMENT("Entertainment & Gaming", "sports_esports"),

    @SerializedName("SOCIAL")
    SOCIAL("Social & Gatherings", "groups"),

    @SerializedName("SALARY")
    SALARY("Salary & Wage", "payments"),

    @SerializedName("BONUS")
    BONUS("Bonus & Reward", "redeem"),

    @SerializedName("INVESTMENT")
    INVESTMENT("Investment Returns", "trending_up"),

    @SerializedName("TRANSFER")
    TRANSFER("Wallet Transfer", "swap_horiz"),

    @SerializedName("OTHER")
    OTHER("Other", "category")
}

data class Wallet(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("balance") val balance: Long = 0L,
    @SerializedName("color_hex") val colorHex: String = "#4E73DF",
    @SerializedName("icon_name") val iconName: String = "account_balance_wallet",
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateWalletRequest(
    @SerializedName("name") val name: String,
    @SerializedName("balance") val balance: Long = 0L,
    @SerializedName("color_hex") val colorHex: String = "#4E73DF",
    @SerializedName("icon_name") val iconName: String = "account_balance_wallet"
)

data class Transaction(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: TransactionType = TransactionType.EXPENSE,
    @SerializedName("category") val category: TransactionCategory,
    @SerializedName("merchant") val merchant: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("receipt_url") val receiptUrl: String? = null,
    @SerializedName("wallet_id") val walletId: String? = null,
    @SerializedName("to_wallet_id") val toWalletId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateTransactionRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: TransactionType = TransactionType.EXPENSE,
    @SerializedName("category") val category: TransactionCategory,
    @SerializedName("merchant") val merchant: String,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("receipt_url") val receiptUrl: String = "",
    @SerializedName("wallet_id") val walletId: String? = null,
    @SerializedName("to_wallet_id") val toWalletId: String? = null
)

enum class DueBillStatus {
    @SerializedName("UNPAID")
    UNPAID,

    @SerializedName("PAID")
    PAID
}

enum class RecurringInterval(val displayName: String) {
    @SerializedName("NONE")
    NONE("One-time"),

    @SerializedName("WEEKLY")
    WEEKLY("Weekly"),

    @SerializedName("MONTHLY")
    MONTHLY("Monthly"),

    @SerializedName("YEARLY")
    YEARLY("Yearly")
}

data class DueBill(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("provider_icon_url") val providerIconUrl: String? = null,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: DueBillStatus = DueBillStatus.UNPAID,
    @SerializedName("paid_wallet_id") val paidWalletId: String? = null,
    @SerializedName("is_recurring") val isRecurring: Boolean = false,
    @SerializedName("recurring_interval") val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateDueBillRequest(
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("provider_icon_url") val providerIconUrl: String? = null,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("is_recurring") val isRecurring: Boolean = false,
    @SerializedName("recurring_interval") val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    @SerializedName("notes") val notes: String = ""
)

data class UpdateDueBillStatusRequest(
    @SerializedName("status") val status: DueBillStatus,
    @SerializedName("wallet_id") val walletId: String? = null
)

data class UpdateDueBillRequest(
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("provider_icon_url") val providerIconUrl: String? = null,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("is_recurring") val isRecurring: Boolean = false,
    @SerializedName("recurring_interval") val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    @SerializedName("notes") val notes: String = ""
)

data class SetBudgetRequest(
    @SerializedName("monthly_limit") val monthlyLimit: Long,
    @SerializedName("month_year") val monthYear: String = ""
)

data class CategorySummary(
    @SerializedName("category") val category: TransactionCategory,
    @SerializedName("total") val total: Long,
    @SerializedName("count") val count: Long
)

data class DashboardSummary(
    @SerializedName("monthly_budget") val monthlyBudget: Long,
    @SerializedName("total_spent") val totalSpent: Long,
    @SerializedName("remaining_budget") val remainingBudget: Long,
    @SerializedName("days_passed") val daysPassed: Int,
    @SerializedName("days_in_month") val daysInMonth: Int,
    @SerializedName("average_daily_spend") val averageDailySpend: Long,
    @SerializedName("estimated_death_day") val estimatedDeathDay: Int,
    @SerializedName("runway_message") val runwayMessage: String,
    @SerializedName("top_categories") val topCategories: List<CategorySummary>?,
    @SerializedName("unpaid_due_bills_sum") val unpaidDueBillsSum: Long,
    @SerializedName("net_worth") val netWorth: Long = 0L,

    @SerializedName("recent_transactions") val recentTransactions: List<Transaction>?
)

data class TransactionListResponse(
    @SerializedName("data") val data: List<Transaction>,
    @SerializedName("total") val total: Long,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int
)

data class DueBillListResponse(
    @SerializedName("data") val data: List<DueBill>
)

data class Goal(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Long,
    @SerializedName("current_amount") val currentAmount: Long = 0L,
    @SerializedName("target_date") val targetDate: String? = null,
    @SerializedName("color_hex") val colorHex: String = "#4E73DF",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingAmount: Long
        get() = (targetAmount - currentAmount).coerceAtLeast(0L)
}

data class CreateGoalRequest(
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Long,
    @SerializedName("target_date") val targetDate: String = "",
    @SerializedName("color_hex") val colorHex: String = "#4E73DF",
    @SerializedName("notes") val notes: String = ""
)

data class DepositGoalRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("wallet_id") val walletId: String
)

data class UpdateGoalRequest(
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Long,
    @SerializedName("target_date") val targetDate: String = "",
    @SerializedName("color_hex") val colorHex: String = "#4E73DF",
    @SerializedName("notes") val notes: String = ""
)

data class GoalListResponse(
    @SerializedName("data") val data: List<Goal>
)

data class CashflowDataPoint(
    @SerializedName("month") val month: String, // "2026-06"
    @SerializedName("label") val label: String, // "Jun"
    @SerializedName("income") val income: Long,
    @SerializedName("expense") val expense: Long
)

data class NetWorthDataPoint(
    @SerializedName("month") val month: String,
    @SerializedName("label") val label: String,
    @SerializedName("net_worth") val netWorth: Long
)

data class CashflowResponse(
    @SerializedName("data") val data: List<CashflowDataPoint>
)

data class NetWorthResponse(
    @SerializedName("data") val data: List<NetWorthDataPoint>
)
