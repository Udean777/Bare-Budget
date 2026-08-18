package com.ssajudn.barebudget.data.model

import com.google.gson.annotations.SerializedName

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

    @SerializedName("OTHER")
    OTHER("Other", "category")
}

data class Transaction(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("amount") val amount: Long,
    @SerializedName("category") val category: TransactionCategory,
    @SerializedName("merchant") val merchant: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("receipt_url") val receiptUrl: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateTransactionRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("category") val category: TransactionCategory,
    @SerializedName("merchant") val merchant: String,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("receipt_url") val receiptUrl: String = ""
)

enum class DueBillStatus {
    @SerializedName("UNPAID")
    UNPAID,

    @SerializedName("PAID")
    PAID
}

data class DueBill(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: DueBillStatus = DueBillStatus.UNPAID,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateDueBillRequest(
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("notes") val notes: String = ""
)

data class UpdateDueBillStatusRequest(
    @SerializedName("status") val status: DueBillStatus
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
