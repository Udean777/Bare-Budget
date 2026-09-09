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
            var nextDateStr = template.nextOccurrenceDate ?: continue

            var iterations = 0
            // Prevent infinite loop if dates are far behind
            while (nextDateStr <= todayIso && iterations < 30) {
                iterations++
                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = template.amount,
                    type = template.type,
                    category = template.category,
                    merchant = template.merchant,
                    date = nextDateStr,
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
                nextDateStr = calculateNextOccurrence(nextDateStr, template.recurringInterval)
            }

            if (iterations > 0 && template.id != null) {
                updatedTemplates.add(
                    TemplateUpdate(
                        templateId = template.id,
                        nextOccurrenceDate = nextDateStr
                    )
                )
            }
        }

        return RolloverResult(newTransactions, updatedTemplates)
    }

    private fun calculateNextOccurrence(currentDateStr: String, interval: RecurringInterval): String {
        return try {
            val date = LocalDate.parse(currentDateStr.take(10))
            val next = when (interval) {
                RecurringInterval.WEEKLY -> date.plusWeeks(1)
                RecurringInterval.MONTHLY -> date.plusMonths(1)
                RecurringInterval.YEARLY -> date.plusYears(1)
                RecurringInterval.NONE -> date
            }
            next.toString()
        } catch (_: Exception) {
            currentDateStr
        }
    }
}
