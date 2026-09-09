package com.ssajudn.bareuang.ui.budget

import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: BudgetRepository = mockk(relaxed = true)
    private val categoryBudgetsFlow = MutableStateFlow<List<CategoryBudget>>(emptyList())
    private val dailyTarget = MutableStateFlow<Long?>(null)

    private val dailyPacing = object : DailyPacingPreferencesPort {
        override val customTarget = dailyTarget
        override val lastCustomTarget = dailyTarget
        override fun setCustomTarget(amount: Long?) { dailyTarget.value = amount }
        override fun reset() { dailyTarget.value = null }
    }

    private fun createVm(existingBudget: Long = 0L): BudgetViewModel {
        coEvery { repository.getMonthlyBudget(any()) } returns Result.success(existingBudget)
        every { repository.getCategoryBudgets(any()) } returns categoryBudgetsFlow
        return BudgetViewModel(repository, dailyPacing)
    }

    @Test
    fun `initial load sets currentLimit and observes category budgets`() = runTest {
        categoryBudgetsFlow.value = listOf(
            CategoryBudget(category = TransactionCategory.FOOD, limitAmount = 1_000_000L, spentAmount = 250_000L)
        )

        val vm = createVm(existingBudget = 3_000_000L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3_000_000L, state.currentLimit)
        assertEquals(3_000_000L, state.parsedAmount)
        assertTrue(state.isLocked)
        assertEquals(1, state.categoryBudgets.size)
        assertEquals(1_000_000L, state.totalAllocatedCategory)
        assertFalse(state.isOverAllocated)
    }

    @Test
    fun `isOverAllocated returns true when category budgets exceed total limit`() = runTest {
        categoryBudgetsFlow.value = listOf(
            CategoryBudget(category = TransactionCategory.FOOD, limitAmount = 2_000_000L),
            CategoryBudget(category = TransactionCategory.SHOPPING, limitAmount = 2_000_000L)
        )

        val vm = createVm(existingBudget = 3_000_000L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(4_000_000L, state.totalAllocatedCategory)
        assertTrue(state.isOverAllocated)
    }

    @Test
    fun `setCategoryBudget delegates to repository`() = runTest {
        coEvery { repository.setCategoryBudget(any(), any(), any()) } returns Result.success(true)

        val vm = createVm()
        advanceUntilIdle()

        vm.setCategoryBudget(TransactionCategory.TRANSPORT, 500_000L)
        advanceUntilIdle()

        coVerify { repository.setCategoryBudget(TransactionCategory.TRANSPORT, 500_000L, any()) }
    }

    @Test
    fun `deleteCategoryBudget delegates to repository`() = runTest {
        coEvery { repository.deleteCategoryBudget(any(), any()) } returns Result.success(true)

        val vm = createVm()
        advanceUntilIdle()

        vm.deleteCategoryBudget(TransactionCategory.FOOD)
        advanceUntilIdle()

        coVerify { repository.deleteCategoryBudget(TransactionCategory.FOOD, any()) }
    }
}
