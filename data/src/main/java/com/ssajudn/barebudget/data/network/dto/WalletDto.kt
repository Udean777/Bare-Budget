package com.ssajudn.barebudget.data.network.dto

import com.google.gson.annotations.SerializedName
import com.ssajudn.barebudget.domain.model.Wallet
import java.util.UUID

data class WalletDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("balance") val balance: Long,
    @SerializedName("color_hex") val colorHex: String,
    @SerializedName("icon_name") val iconName: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String? = null
) {
    fun toDomain(): Wallet = Wallet(id = id, name = name, balance = balance, colorHex = colorHex, iconName = iconName, createdAt = createdAt)
    companion object { fun fromDomain(w: Wallet) = WalletDto(id = w.id ?: UUID.randomUUID().toString(), userId = null, name = w.name, balance = w.balance, colorHex = w.colorHex, iconName = w.iconName, createdAt = w.createdAt) }
}

data class CreateWalletRequestDto(@SerializedName("name") val name: String, @SerializedName("balance") val balance: Long, @SerializedName("color_hex") val colorHex: String, @SerializedName("icon_name") val iconName: String)
