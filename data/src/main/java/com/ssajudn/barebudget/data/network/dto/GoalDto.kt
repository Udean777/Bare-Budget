package com.ssajudn.barebudget.data.network.dto

import com.google.gson.annotations.SerializedName
import com.ssajudn.barebudget.domain.model.Goal

data class GoalDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Long,
    @SerializedName("current_amount") val currentAmount: Long,
    @SerializedName("target_date") val targetDate: String?,
    @SerializedName("color_hex") val colorHex: String,
    @SerializedName("notes") val notes: String?
) { fun toDomain(): Goal = Goal(id = id, name = name, targetAmount = targetAmount, currentAmount = currentAmount, targetDate = targetDate, colorHex = colorHex, notes = notes) }

data class CreateGoalRequestDto(@SerializedName("name") val name: String, @SerializedName("target_amount") val targetAmount: Long, @SerializedName("target_date") val targetDate: String, @SerializedName("color_hex") val colorHex: String, @SerializedName("notes") val notes: String)
data class UpdateGoalRequestDto(@SerializedName("name") val name: String, @SerializedName("target_amount") val targetAmount: Long, @SerializedName("target_date") val targetDate: String?, @SerializedName("color_hex") val colorHex: String, @SerializedName("notes") val notes: String?)
