package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.model.*
import com.ssajudn.barebudget.data.network.ApiClient
import com.ssajudn.barebudget.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BudgetRepository(private val api: ApiService = ApiClient.apiService) {

    suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setBudget(monthlyLimit: Long, monthYear: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.setBudget(SetBudgetRequest(monthlyLimit, monthYear))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to set budget"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(category: String? = null, page: Int = 1, limit: Int = 50): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTransactions(category = category, page = page, limit = limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            val response = api.createTransaction(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteTransaction(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDueBills(status: String? = null): Result<List<DueBill>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDueBills(status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch due bills"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill> = withContext(Dispatchers.IO) {
        try {
            val response = api.createDueBill(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create due bill"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDueBillStatus(id: String, status: DueBillStatus): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateDueBillStatus(id, UpdateDueBillStatusRequest(status))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to update due bill status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDueBill(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteDueBill(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete due bill"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
