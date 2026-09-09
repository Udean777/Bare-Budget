package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.Wallet

object CalculateNetWorthUseCase {
    operator fun invoke(wallets: List<Wallet>): Long =
        wallets.fold(0L) { total, wallet -> Math.addExact(total, wallet.balance) }
}

