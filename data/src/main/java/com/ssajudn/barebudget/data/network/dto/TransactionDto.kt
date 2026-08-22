package com.ssajudn.barebudget.data.network.dto

import com.google.gson.annotations.SerializedName
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.data.repository.DomainMappers

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: String,
    @SerializedName("wallet_id") val walletId: String?,
    @SerializedName("to_wallet_id") val toWalletId: String? = null,
    @SerializedName("category") val category: String,
    @SerializedName("merchant") val merchant: String?,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("receipt_url") val receiptUrl: String?
) {
    fun toDomain(): Transaction = Transaction(id = id, amount = amount, type = DomainMappers.safeTransactionType(type), category = DomainMappers.safeCategory(category), merchant = merchant, date = date, notes = notes, receiptUrl = receiptUrl, walletId = walletId, toWalletId = toWalletId)
}

data class CreateTransactionRequestDto(
    @SerializedName("amount") val amount: Long,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("merchant") val merchant: String,
    @SerializedName("date") val date: String,
    @SerializedName("notes") val notes: String,
    @SerializedName("receipt_url") val receiptUrl: String? = null,
    @SerializedName("wallet_id") val walletId: String?,
    @SerializedName("to_wallet_id") val toWalletId: String? = null
)
