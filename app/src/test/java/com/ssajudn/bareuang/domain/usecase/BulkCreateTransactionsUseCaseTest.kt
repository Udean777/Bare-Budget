package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Clock

class BulkCreateTransactionsUseCaseTest {
    @Test
    fun `daily gate checks complete selected batch once`() = runTest {
        val budget = mockk<BudgetRepository>()
        val transactions = mockk<TransactionRepository>(relaxed = true)
        val wallets = mockk<WalletRepository>()
        val daily = mockk<CheckDailyBudgetUseCase>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        coEvery { budget.getMonthlyBudget(any()) } returns Result.success(1_000_000L)
        coEvery { wallets.getWallets() } returns Result.success(listOf(Wallet("w1", "Cash", 500_000L)))
        coEvery { daily.invoke(any(), any(), any(), any()) } returns Result.success(Unit)
        coEvery { transactions.bulkCreate(any()) } returns Result.success(2)

        val useCase = BulkCreateTransactionsUseCase(
            transactions,
            wallets,
            HasMonthlyBudgetUseCase(budget),
            daily,
            Clock.systemDefaultZone(),
        )
        val drafts = listOf(
            ImportDraft("1", 40_000L, TransactionType.EXPENSE, TransactionCategory.FOOD, "A", today, today),
            ImportDraft("2", 60_000L, TransactionType.EXPENSE, TransactionCategory.TRANSPORT, "B", today, today)
        )

        assertTrue(useCase(drafts, "w1", AppCurrency.IDR).isSuccess)
        coVerify(exactly = 1) { daily.invoke(100_000L, today, AppCurrency.IDR, TransactionCategory.OTHER) }
    }
}
