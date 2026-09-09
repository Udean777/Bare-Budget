package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import javax.inject.Inject
import java.time.Clock

class GetNetWorthAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<List<NetWorthDataPoint>> =
        analyticsRepository.getNetWorthAnalytics(clock)
}
