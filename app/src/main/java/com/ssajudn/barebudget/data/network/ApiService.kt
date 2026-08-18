package com.ssajudn.barebudget.data.network

import com.ssajudn.barebudget.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Dashboard & Budget
    @GET("api/v1/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummary>

    @POST("api/v1/budget")
    suspend fun setBudget(@Body request: SetBudgetRequest): Response<Map<String, String>>

    // Transactions
    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("category") category: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TransactionListResponse>

    @POST("api/v1/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<Transaction>

    @DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Map<String, String>>

    // Due Bills Tracker
    @GET("api/v1/due-bills")
    suspend fun getDueBills(@Query("status") status: String? = null): Response<DueBillListResponse>

    @POST("api/v1/due-bills")
    suspend fun createDueBill(@Body request: CreateDueBillRequest): Response<DueBill>

    @PATCH("api/v1/due-bills/{id}/status")
    suspend fun updateDueBillStatus(
        @Path("id") id: String,
        @Body request: UpdateDueBillStatusRequest
    ): Response<Map<String, String>>

    @DELETE("api/v1/due-bills/{id}")
    suspend fun deleteDueBill(@Path("id") id: String): Response<Map<String, String>>
}
