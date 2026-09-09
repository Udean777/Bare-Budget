package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalBudgetEntity
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.data.mapper.PersistenceMappers
import com.ssajudn.bareuang.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetLocalDataSource @Inject constructor(private val db: AppDatabase) {

    suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                if (monthlyLimit <= 0) return@withContext Result.failure(IllegalArgumentException("Budget harus lebih dari 0"))
                val my = if (monthYear.isBlank()) {
                    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                } else monthYear
                val existing = db.budgetDao().getBudget(my)
                if (existing != null) {
                    return@withContext Result.failure(
                        com.ssajudn.bareuang.domain.error.AppException.DataException(
                            "Budget bulan $my sudah diatur. Hanya bisa diubah bulan depan."
                        )
                    )
                }
                db.budgetDao().insertBudget(LocalBudgetEntity(monthYear = my, monthlyLimit = monthlyLimit, isSynced = false))
                Result.success(true)
            } catch (e: Exception) {
                if (e is com.ssajudn.bareuang.domain.error.AppException) Result.failure(e)
                else Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun getMonthlyBudget(monthYear: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
            } else monthYear
            val budget = db.budgetDao().getBudget(my)
            Result.success(budget?.monthlyLimit ?: 0L)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeCategoryBudgets(monthYear: String): kotlinx.coroutines.flow.Flow<List<com.ssajudn.bareuang.domain.model.CategoryBudget>> {
        val my = if (monthYear.isBlank()) {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        } else monthYear

        return kotlinx.coroutines.flow.combine(
            db.budgetDao().observeCategoryBudgets(my),
            db.transactionDao().observeAllTransactions()
        ) { categoryEntities, transactions ->
            val monthTx = transactions.filter {
                it.date.startsWith(my) &&
                PersistenceMappers.safeTransactionType(it.type) == TransactionType.EXPENSE
            }
            val spentPerCategory = monthTx.groupBy { it.category }
                .mapValues { (_, txList) -> txList.sumOf { it.amount } }

            categoryEntities.map { entity ->
                val cat = PersistenceMappers.safeCategory(entity.category)
                com.ssajudn.bareuang.domain.model.CategoryBudget(
                    category = cat,
                    limitAmount = entity.limitAmount,
                    spentAmount = spentPerCategory[entity.category] ?: 0L,
                    monthYear = entity.monthYear
                )
            }
        }
    }

    suspend fun setCategoryBudget(
        category: com.ssajudn.bareuang.domain.model.TransactionCategory,
        limitAmount: Long,
        monthYear: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (limitAmount <= 0) return@withContext Result.failure(IllegalArgumentException("Batas kategori harus lebih dari 0"))
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
            } else monthYear
            db.budgetDao().insertCategoryBudget(
                com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity(
                    monthYear = my,
                    category = category.name,
                    limitAmount = limitAmount,
                    isSynced = false
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun deleteCategoryBudget(
        category: com.ssajudn.bareuang.domain.model.TransactionCategory,
        monthYear: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val my = if (monthYear.isBlank()) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
            } else monthYear
            db.budgetDao().deleteCategoryBudget(my, category.name)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }
}
