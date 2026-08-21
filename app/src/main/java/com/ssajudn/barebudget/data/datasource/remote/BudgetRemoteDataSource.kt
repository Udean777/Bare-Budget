package com.ssajudn.barebudget.data.datasource.remote

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalBudgetEntity
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.data.model.SetBudgetRequest
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase
) {

    @Suppress("DEPRECATION")
    suspend fun getDashboardSummary(): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiErrorParser.parse(response))
            }
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val my = if (monthYear.isBlank()) {
                    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                } else monthYear
                // optimistic local check to avoid round-trip
                if (db.budgetDao().getBudget(my) != null) {
                    return@withContext Result.failure(
                        com.ssajudn.barebudget.domain.error.AppException.DataException(
                            "Budget bulan $my sudah diatur. Hanya bisa diubah bulan depan."
                        )
                    )
                }
                val response = api.setBudget(SetBudgetRequest(monthlyLimit, my))
                if (response.isSuccessful) {
                    db.budgetDao().insertBudget(
                        LocalBudgetEntity(monthYear = my, monthlyLimit = monthlyLimit, isSynced = true)
                    )
                    Result.success(true)
                } else {
                    Result.failure(ApiErrorParser.parse(response))
                }
            } catch (e: Exception) {
                if (e is com.ssajudn.barebudget.domain.error.AppException) Result.failure(e)
                else Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun getMonthlyBudget(monthYear: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(java.util.Calendar.getInstance().time)
            } else monthYear
            val response = api.getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.monthlyBudget)
            } else {
                val local = db.budgetDao().getBudget(my)
                Result.success(local?.monthlyLimit ?: 0L)
            }
        } catch (e: Exception) {
            try {
                val my = if (monthYear.isBlank()) {
                    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                } else monthYear
                val local = db.budgetDao().getBudget(my)
                Result.success(local?.monthlyLimit ?: 0L)
            } catch (ex: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }
    }
}