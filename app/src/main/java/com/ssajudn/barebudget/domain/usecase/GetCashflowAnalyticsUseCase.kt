package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.ssajudn.barebudget.domain.error.AppException
import javax.inject.Inject

class GetCashflowAnalyticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): Result<List<CashflowDataPoint>> {
        return try {
            val transactions = transactionRepository.getTransactions(limit = 500).getOrDefault(emptyList())
            val points = mutableListOf<CashflowDataPoint>()
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val labelFormat = SimpleDateFormat("MMM", Locale("id", "ID"))

            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                val monthKey = monthFormat.format(cal.time)
                val label = labelFormat.format(cal.time)

                val monthTxs = transactions.filter { it.date.startsWith(monthKey) }
                val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                points.add(CashflowDataPoint(month = monthKey, label = label, income = income, expense = expense))
            }
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(AppException.UnknownError(e.message, e))
        }
    }
}
