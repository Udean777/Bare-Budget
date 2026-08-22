package com.ssajudn.barebudget.data.network.dto

import com.google.gson.annotations.SerializedName
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.data.repository.DomainMappers

data class DueBillDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("provider_icon_url") val providerIconUrl: String?,
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("paid_wallet_id") val paidWalletId: String? = null,
    @SerializedName("is_recurring") val isRecurring: Boolean,
    @SerializedName("recurring_interval") val recurringInterval: String,
    @SerializedName("notes") val notes: String?
) { fun toDomain(): DueBill = DueBill(id = id, providerName = providerName, providerIconUrl = providerIconUrl, totalAmount = totalAmount, dueDate = dueDate, status = DomainMappers.safeDueBillStatus(status), paidWalletId = paidWalletId, isRecurring = isRecurring, recurringInterval = DomainMappers.safeRecurringInterval(recurringInterval), notes = notes) }

data class CreateDueBillRequestDto(@SerializedName("provider_name") val providerName: String, @SerializedName("provider_icon_url") val providerIconUrl: String?, @SerializedName("total_amount") val totalAmount: Long, @SerializedName("due_date") val dueDate: String, @SerializedName("is_recurring") val isRecurring: Boolean, @SerializedName("recurring_interval") val recurringInterval: String, @SerializedName("notes") val notes: String)
data class UpdateDueBillRequestDto(@SerializedName("provider_name") val providerName: String, @SerializedName("provider_icon_url") val providerIconUrl: String?, @SerializedName("total_amount") val totalAmount: Long, @SerializedName("due_date") val dueDate: String, @SerializedName("is_recurring") val isRecurring: Boolean, @SerializedName("recurring_interval") val recurringInterval: String, @SerializedName("notes") val notes: String)
