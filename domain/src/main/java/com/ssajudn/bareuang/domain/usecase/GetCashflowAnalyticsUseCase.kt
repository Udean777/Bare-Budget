package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import javax.inject.Inject
import java.time.Clock

class GetCashflowAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<List<CashflowDataPoint>> =
        analyticsRepository.getCashflowAnalytics(clock)
}
