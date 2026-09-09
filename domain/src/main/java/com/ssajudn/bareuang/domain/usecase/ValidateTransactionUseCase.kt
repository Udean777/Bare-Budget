package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import javax.inject.Inject

enum class TransactionValidationError {
    INVALID_AMOUNT, WALLET_REQUIRED, TO_WALLET_REQUIRED, SAME_WALLET, INSUFFICIENT_BALANCE
}

class ValidateTransactionUseCase @Inject constructor() {
    operator fun invoke(
        type: TransactionType,
        amount: Long,
        sourceWalletId: String?,
        targetWalletId: String?,
        wallets: List<Wallet>
    ): TransactionValidationError? {
        if (amount <= 0) return TransactionValidationError.INVALID_AMOUNT
        if (sourceWalletId == null) return TransactionValidationError.WALLET_REQUIRED
        if (type == TransactionType.TRANSFER) {
            if (targetWalletId == null) return TransactionValidationError.TO_WALLET_REQUIRED
            if (sourceWalletId == targetWalletId) return TransactionValidationError.SAME_WALLET
        }
        if (type == TransactionType.EXPENSE || type == TransactionType.TRANSFER) {
            val wallet = wallets.firstOrNull { it.id == sourceWalletId }
            if (wallet != null && wallet.balance < amount) return TransactionValidationError.INSUFFICIENT_BALANCE
        }
        return null
    }
}
