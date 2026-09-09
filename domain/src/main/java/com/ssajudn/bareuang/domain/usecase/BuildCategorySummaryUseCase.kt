package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CategorySummary
import com.ssajudn.bareuang.domain.model.Transaction

object BuildCategorySummaryUseCase {
    operator fun invoke(expenses: List<Transaction>): List<CategorySummary> =
        expenses.groupBy { it.category }
            .map { (category, transactions) ->
                CategorySummary(
                    category = category,
                    total = transactions.fold(0L) { total, tx -> Math.addExact(total, tx.amount) },
                    count = transactions.size.toLong(),
                )
            }
            .sortedByDescending { it.total }
}

