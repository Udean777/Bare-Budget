package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class BulkCreateTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        drafts: List<ImportDraft>,
        walletId: String,
        currency: AppCurrency,
        // Soft daily-budget nudge: when false the daily gate trips and returns a
        // DailyBudgetExceededException so the UI can ask before overriding; when
        // true (user confirmed) the gate is skipped.
        force: Boolean = false
    ): Result<Int> {
        if (walletId.isBlank()) return Result.failure(AppException.DataException("Dompet wajib dipilih"))
        val selected = drafts.filter { it.isSelected && !it.isDuplicate }
        if (selected.isEmpty()) return Result.failure(AppException.DataException("Tidak ada transaksi yang dipilih"))
        if (selected.any { it.amount <= 0 }) return Result.failure(AppException.DataException("Jumlah transaksi harus lebih dari 0"))
        // Budget gate — single check for all
        if (!hasMonthlyBudget()) return Result.failure(AppException.DataException("Budget bulan ini belum diatur"))
        // Daily gate — check the complete today's batch once so each item cannot
        // reuse the same remaining allowance independently.
        if (!force) {
            val todayIso = LocalDate.now(clock).toString()
            val todayExpense = try {
                selected.filter {
                    it.type == TransactionType.EXPENSE &&
                        it.category != TransactionCategory.BILLS &&
                        it.date.take(10) == todayIso
                }.fold(0L) { total, draft -> Math.addExact(total, draft.amount) }
            } catch (e: ArithmeticException) {
                return Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
            }
            if (todayExpense > 0L) {
                val dailyCheck = checkDailyBudget(todayExpense, todayIso, currency)
                if (dailyCheck.isFailure) {
                    return Result.failure(
                        AppException.DailyBudgetExceededException(dailyCheck.exceptionOrNull()?.message ?: "Target pacing hari ini terlampaui")
                    )
                }
            }
        }
        // Saldo check — sum of expenses vs wallet balance
        val totalExpense = try {
            selected.filter { it.type != TransactionType.INCOME }
                .fold(0L) { total, draft -> Math.addExact(total, draft.amount) }
        } catch (e: ArithmeticException) {
            return Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
        }
        if (totalExpense > 0) {
            val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == walletId }
            if (wallet != null && wallet.balance < totalExpense) {
                return Result.failure(AppException.DataException("Saldo dompet tidak cukup. Butuh ${DomainCurrencyFormatter.format(totalExpense, currency)}, saldo ${DomainCurrencyFormatter.format(wallet.balance, currency)}"))
            }
        }
        val requests = selected.map { d ->
            CreateTransactionRequest(
                amount = d.amount,
                type = d.type,
                category = d.category,
                merchant = d.merchant,
                date = d.date,
                walletId = walletId
            )
        }
        return try {
            transactionRepository.bulkCreate(requests)
        } catch (e: ArithmeticException) {
            Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
        }
    }
}
