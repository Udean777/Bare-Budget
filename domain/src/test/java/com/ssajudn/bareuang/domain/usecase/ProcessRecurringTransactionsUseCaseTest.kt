package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessRecurringTransactionsUseCaseTest {

    private val useCase = ProcessRecurringTransactionsUseCase()

    @Test
    fun `ignores non-recurring templates`() {
        val nonRecurring = Transaction(
            id = "tx-1",
            amount = 50_000L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            date = "2026-08-01",
            isRecurringParent = false,
            recurringInterval = RecurringInterval.NONE
        )

        val result = useCase(listOf(nonRecurring), "2026-08-23")
        assertTrue(result.newTransactions.isEmpty())
        assertTrue(result.updatedTemplates.isEmpty())
    }

    @Test
    fun `generates next transaction and rolls over monthly date`() {
        val monthlyTemplate = Transaction(
            id = "template-salary",
            amount = 10_000_000L,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            date = "2026-07-25",
            isRecurringParent = true,
            recurringInterval = RecurringInterval.MONTHLY,
            nextOccurrenceDate = "2026-08-25"
        )

        // Today is before next occurrence -> no rollover
        val beforeResult = useCase(listOf(monthlyTemplate), "2026-08-24")
        assertTrue(beforeResult.newTransactions.isEmpty())

        // Today is on or after next occurrence -> generates 1 tx and advances to 2026-09-25
        val onResult = useCase(listOf(monthlyTemplate), "2026-08-25")
        assertEquals(1, onResult.newTransactions.size)
        assertEquals(10_000_000L, onResult.newTransactions.first().amount)
        assertEquals("2026-08-25", onResult.newTransactions.first().date)
        assertEquals("template-salary", onResult.newTransactions.first().parentRecurringId)

        assertEquals(1, onResult.updatedTemplates.size)
        assertEquals("template-salary", onResult.updatedTemplates.first().templateId)
        assertEquals("2026-09-25", onResult.updatedTemplates.first().nextOccurrenceDate)
    }

    @Test
    fun `rolls over weekly interval correctly`() {
        val weeklyTemplate = Transaction(
            id = "template-weekly",
            amount = 200_000L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.SHOPPING,
            date = "2026-08-10",
            isRecurringParent = true,
            recurringInterval = RecurringInterval.WEEKLY,
            nextOccurrenceDate = "2026-08-17"
        )

        val result = useCase(listOf(weeklyTemplate), "2026-08-17")
        assertEquals(1, result.newTransactions.size)
        assertEquals("2026-08-17", result.newTransactions.first().date)
        assertEquals("2026-08-24", result.updatedTemplates.first().nextOccurrenceDate)
    }

    @Test
    fun `ignores template with invalid next occurrence date`() {
        val invalidTemplate = Transaction(
            id = "template-invalid",
            amount = 100_000L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.BILLS,
            date = "2026-08-01",
            isRecurringParent = true,
            recurringInterval = RecurringInterval.MONTHLY,
            nextOccurrenceDate = "not-a-date",
        )

        val result = useCase(listOf(invalidTemplate), "2026-08-31")

        assertTrue(result.newTransactions.isEmpty())
        assertTrue(result.updatedTemplates.isEmpty())
    }
}
