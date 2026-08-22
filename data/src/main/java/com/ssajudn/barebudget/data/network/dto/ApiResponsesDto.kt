package com.ssajudn.barebudget.data.network.dto

import com.google.gson.annotations.SerializedName

data class TransactionListResponseDto(@SerializedName("data") val data: List<TransactionDto>, @SerializedName("total") val total: Long, @SerializedName("page") val page: Int, @SerializedName("limit") val limit: Int)
data class DueBillListResponseDto(@SerializedName("data") val data: List<DueBillDto>)
data class GoalListResponseDto(@SerializedName("data") val data: List<GoalDto>)
