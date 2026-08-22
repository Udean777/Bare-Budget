package com.ssajudn.barebudget.data.network

import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.data.model.CashflowResponse
import com.ssajudn.barebudget.data.model.DepositGoalRequest
import com.ssajudn.barebudget.data.model.NetWorthResponse
import com.ssajudn.barebudget.data.model.SetBudgetRequest
import com.ssajudn.barebudget.data.model.UpdateDueBillStatusRequest
import com.ssajudn.barebudget.data.network.dto.CreateDueBillRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateGoalRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateTransactionRequestDto
import com.ssajudn.barebudget.data.network.dto.CreateWalletRequestDto
import com.ssajudn.barebudget.data.network.dto.DueBillDto
import com.ssajudn.barebudget.data.network.dto.DueBillListResponseDto
import com.ssajudn.barebudget.data.network.dto.GoalDto
import com.ssajudn.barebudget.data.network.dto.GoalListResponseDto
import com.ssajudn.barebudget.data.network.dto.TransactionDto
import com.ssajudn.barebudget.data.network.dto.TransactionListResponseDto
import com.ssajudn.barebudget.data.network.dto.UpdateDueBillRequestDto
import com.ssajudn.barebudget.data.network.dto.UpdateGoalRequestDto
import com.ssajudn.barebudget.data.network.dto.WalletDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Dashboard & Budget
    @GET("api/v1/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @POST("api/v1/budget")
    suspend fun setBudget(@Body request: SetBudgetRequest): Response<Map<String, String>>

    // Analytics
    @GET("api/v1/analytics/cashflow")
    suspend fun getCashflowAnalytics(): Response<CashflowResponse>

    @GET("api/v1/analytics/networth")
    suspend fun getNetWorthAnalytics(): Response<NetWorthResponse>

    // Wallets
    @GET("api/v1/wallets")
    suspend fun getWallets(): Response<List<WalletDto>>

    @POST("api/v1/wallets")
    suspend fun createWallet(@Body request: CreateWalletRequestDto): Response<WalletDto>

    @DELETE("api/v1/wallets/{id}")
    suspend fun deleteWallet(@Path("id") id: String): Response<Map<String, String>>

    // Transactions
    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("category") category: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TransactionListResponseDto>

    @POST("api/v1/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequestDto): Response<TransactionDto>

    @DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Map<String, String>>

    // Due Bills Tracker
    @GET("api/v1/due-bills")
    suspend fun getDueBills(@Query("status") status: String? = null): Response<DueBillListResponseDto>

    @POST("api/v1/due-bills")
    suspend fun createDueBill(@Body request: CreateDueBillRequestDto): Response<DueBillDto>

    @PATCH("api/v1/due-bills/{id}/status")
    suspend fun updateDueBillStatus(
        @Path("id") id: String,
        @Body request: UpdateDueBillStatusRequest
    ): Response<Map<String, String>>

    @PATCH("api/v1/due-bills/{id}")
    suspend fun updateDueBill(
        @Path("id") id: String,
        @Body request: UpdateDueBillRequestDto
    ): Response<DueBillDto>

    @DELETE("api/v1/due-bills/{id}")
    suspend fun deleteDueBill(@Path("id") id: String): Response<Map<String, String>>

    // Savings Goals
    @GET("api/v1/goals")
    suspend fun getGoals(): Response<GoalListResponseDto>

    @POST("api/v1/goals")
    suspend fun createGoal(@Body request: CreateGoalRequestDto): Response<GoalDto>

    @POST("api/v1/goals/{id}/deposit")
    suspend fun depositToGoal(
        @Path("id") id: String,
        @Body request: DepositGoalRequest
    ): Response<Map<String, String>>

    @PATCH("api/v1/goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: String,
        @Body request: UpdateGoalRequestDto
    ): Response<GoalDto>

    @DELETE("api/v1/goals/{id}")
    suspend fun deleteGoal(@Path("id") id: String): Response<Map<String, String>>
}
