package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateTransactionUseCaseTest {
    private val validate = ValidateTransactionUseCase()
    private val wallets = listOf(Wallet(id = "cash", name = "Cash", balance = 100_000L), Wallet(id = "bank", name = "Bank", balance = 500_000L))

    @Test fun rejectsInvalidAmount() = assertEquals(TransactionValidationError.INVALID_AMOUNT, validate(TransactionType.EXPENSE, 0, "cash", null, wallets))
    @Test fun rejectsSameTransferWallet() = assertEquals(TransactionValidationError.SAME_WALLET, validate(TransactionType.TRANSFER, 10_000, "cash", "cash", wallets))
    @Test fun rejectsInsufficientBalance() = assertEquals(TransactionValidationError.INSUFFICIENT_BALANCE, validate(TransactionType.EXPENSE, 100_001, "cash", null, wallets))
    @Test fun acceptsIncomeWithoutWalletBalanceCheck() = assertNull(validate(TransactionType.INCOME, 1_000_000, "cash", null, wallets))
}
