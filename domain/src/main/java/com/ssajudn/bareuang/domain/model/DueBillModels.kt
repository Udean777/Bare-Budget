package com.ssajudn.bareuang.domain.model

enum class DueBillStatus {
    UNPAID,
    PAID
}

data class DueBill(
    val id: String? = null,
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val status: DueBillStatus = DueBillStatus.UNPAID,
    val paidWalletId: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String? = null,
    val createdAt: String? = null
)

data class CreateDueBillRequest(
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String = ""
)

data class UpdateDueBillRequest(
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String = ""
)
