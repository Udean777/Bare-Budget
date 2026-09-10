package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.RecurringInterval
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class ProcessRecurringTransactionsUseCase @Inject constructor() {

    data class RolloverResult(
        val newTransactions: List<Transaction>,
        val updatedTemplates: List<TemplateUpdate>
    )

    data class TemplateUpdate(
        val templateId: String,
        val nextOccurrenceDate: String
    )

    operator fun invoke(
        templates: List<Transaction>,
        todayIso: String
    ): RolloverResult {
        val newTransactions = mutableListOf<Transaction>()
        val updatedTemplates = mutableListOf<TemplateUpdate>()

        for (template in templates) {
            if (!template.isRecurringParent || template.recurringInterval == RecurringInterval.NONE) continue
            var nextDate = template.nextOccurrenceDate
                ?.take(10)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: continue

            var iterations = 0
            // Prevent infinite loop if dates are far behind
            while (nextDate.toString() <= todayIso && iterations < 30) {
                iterations++
                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = template.amount,
                    type = template.type,
                    category = template.category,
                    merchant = template.merchant,
                    date = nextDate.toString(),
                    notes = template.notes,
                    receiptUrl = template.receiptUrl,
                    walletId = template.walletId,
                    toWalletId = template.toWalletId,
                    recurringInterval = RecurringInterval.NONE,
                    isRecurringParent = false,
                    parentRecurringId = template.id,
                    nextOccurrenceDate = null
                )
                newTransactions.add(newTx)

                // Advance date
                nextDate = calculateNextOccurrence(nextDate, template.recurringInterval)
            }

            if (iterations > 0 && template.id != null) {
                updatedTemplates.add(
                    TemplateUpdate(
                        templateId = template.id,
                        nextOccurrenceDate = nextDate.toString()
                    )
                )
            }
        }

        return RolloverResult(newTransactions, updatedTemplates)
    }

    private fun calculateNextOccurrence(currentDate: LocalDate, interval: RecurringInterval): LocalDate {
        return when (interval) {
            RecurringInterval.WEEKLY -> currentDate.plusWeeks(1)
            RecurringInterval.MONTHLY -> currentDate.plusMonths(1)
            RecurringInterval.YEARLY -> currentDate.plusYears(1)
            RecurringInterval.NONE -> currentDate
        }
    }
}
