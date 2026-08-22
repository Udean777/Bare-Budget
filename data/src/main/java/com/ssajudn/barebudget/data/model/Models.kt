package com.ssajudn.barebudget.data.model

import com.google.gson.annotations.SerializedName
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint

data class UpdateDueBillStatusRequest(
    @SerializedName("status") val status: DueBillStatus,
    @SerializedName("wallet_id") val walletId: String? = null
)

data class SetBudgetRequest(
    @SerializedName("monthly_limit") val monthlyLimit: Long,
    @SerializedName("month_year") val monthYear: String = ""
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

data class DepositGoalRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("wallet_id") val walletId: String
)

data class GoalListResponse(
    @SerializedName("data") val data: List<Goal>
)

data class CashflowResponse(
    @SerializedName("data") val data: List<CashflowDataPoint>
)

data class NetWorthResponse(
    @SerializedName("data") val data: List<NetWorthDataPoint>
)
