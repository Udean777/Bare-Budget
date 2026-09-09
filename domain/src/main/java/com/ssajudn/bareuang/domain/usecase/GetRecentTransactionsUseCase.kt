package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.Transaction

object GetRecentTransactionsUseCase {
    operator fun invoke(transactions: List<Transaction>, limit: Int = 5): List<Transaction> =
        transactions.asReversed().sortedByDescending { it.date }.take(limit)
}

