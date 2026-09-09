package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.OutstandingBillsSummary

object GetOutstandingBillsSummaryUseCase {
    operator fun invoke(bills: List<DueBill>): OutstandingBillsSummary {
        val unpaid = bills.filter { it.status == DueBillStatus.UNPAID }
        return OutstandingBillsSummary(
            unpaidTotal = unpaid.fold(0L) { total, bill -> Math.addExact(total, bill.totalAmount) },
            unpaidCount = unpaid.size,
        )
    }
}

