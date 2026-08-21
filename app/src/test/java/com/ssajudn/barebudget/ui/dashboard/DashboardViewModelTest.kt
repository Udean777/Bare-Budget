package com.ssajudn.barebudget.ui.dashboard

import app.cash.turbine.test
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.usecase.GetDashboardSummaryUseCase
import com.ssajudn.barebudget.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSummary: GetDashboardSummaryUseCase = mockk()
    private val budgetRepo: BudgetRepository = mockk(relaxed = true)
    private val txRepo: TransactionRepository = mockk(relaxed = true)

    private fun summaryFixture() = DashboardSummary(
        monthlyBudget = 500_000L,
        totalSpent = 100_000L,
        remainingBudget = 400_000L,
        daysPassed = 10,
        daysInMonth = 30,
        averageDailySpend = 10_000L,
        estimatedDeathDay = 25,
        runwayMessage = "HEALTHY",
        topCategories = emptyList(),
        unpaidDueBillsSum = 0L,
        netWorth = 1_000_000L,
        recentTransactions = emptyList()
    )

    @Test
    fun `load success emits Success`() = runTest {
        coEvery { getSummary() } returns Result.success(summaryFixture())

        val vm = DashboardViewModel(getSummary, budgetRepo, txRepo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is DashboardUiState.Success)
        assertEquals(500_000L, (vm.uiState.value as DashboardUiState.Success).summary.monthlyBudget)
    }

    @Test
    fun `load failure emits Error`() = runTest {
        coEvery { getSummary() } returns Result.failure(RuntimeException("Network down"))

        val vm = DashboardViewModel(getSummary, budgetRepo, txRepo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is DashboardUiState.Error)
        assertEquals("Network down", (vm.uiState.value as DashboardUiState.Error).message)
    }

    @Test
    fun `isRefreshing true during pull to refresh`() = runTest {
        coEvery { getSummary() } returns Result.success(summaryFixture())
        val vm = DashboardViewModel(getSummary, budgetRepo, txRepo)
        advanceUntilIdle()
        // Re-mock to delay next call so we can observe true state
        coEvery { getSummary() } coAnswers { kotlinx.coroutines.delay(100); Result.success(summaryFixture()) }

        vm.isRefreshing.test {
            assertEquals(false, awaitItem())
            vm.loadDashboardData(isPullToRefresh = true)
            assertEquals(true, awaitItem())
            // Advance virtual time to complete the delayed getSummary
            mainDispatcherRule.dispatcher.scheduler.advanceTimeBy(100)
            advanceUntilIdle()
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateBudget triggers reload`() = runTest {
        coEvery { getSummary() } returns Result.success(summaryFixture())
        coEvery { budgetRepo.setBudget(any()) } returns Result.success(true)

        val vm = DashboardViewModel(getSummary, budgetRepo, txRepo)
        advanceUntilIdle()

        vm.updateBudget(600_000L)
        advanceUntilIdle()

        coVerify { budgetRepo.setBudget(600_000L) }
        coVerify(atLeast = 2) { getSummary() }
    }

    @Test
    fun `deleteTransaction triggers reload`() = runTest {
        coEvery { getSummary() } returns Result.success(summaryFixture())
        coEvery { txRepo.deleteTransaction(any()) } returns Result.success(true)

        val vm = DashboardViewModel(getSummary, budgetRepo, txRepo)
        advanceUntilIdle()

        vm.deleteTransaction("tx1")
        advanceUntilIdle()

        coVerify { txRepo.deleteTransaction("tx1") }
    }
}
