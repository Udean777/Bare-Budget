package com.ssajudn.barebudget.ui.transaction

import app.cash.turbine.test
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Proof-of-testability for [AddTransactionViewModel].
 *
 * NOTE: This test mocks [WalletRepositoryContract] and [TransactionRepositoryContract]
 * interfaces directly via Mockk — no concrete-class subclassing needed.
 *
 * What this test locks in:
 *  - `init` triggers `loadWallets()` and populates default wallet selection.
 *  - `onAmountChange` strips non-digits and parses to Long.
 *  - `onTransactionTypeChange` swaps default category per type.
 *  - `saveTransaction()` validation rejects zero amount, missing wallet,
 *    and same-source/destination transfer.
 *  - `saveTransaction()` success path flips `isSuccess = true`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)

    private fun walletsFixture(): List<Wallet> = listOf(
        Wallet(id = "w1", name = "Cash", balance = 100_000L),
        Wallet(id = "w2", name = "Bank", balance = 500_000L)
    )

    @Test
    fun `init loads wallets and selects first as default source`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        assertEquals(walletsFixture(), vm.uiState.value.wallets)
        assertEquals("w1", vm.uiState.value.selectedWalletId)
        // Transfer default target falls back to second wallet (or first if absent)
        assertEquals("w2", vm.uiState.value.selectedToWalletId)
    }

    @Test
    fun `init handles empty wallet list without crashing`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.wallets.isEmpty())
        assertNull(vm.uiState.value.selectedWalletId)
    }

    @Test
    fun `onAmountChange strips non-digits and parses to Long`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("Rp 50.000")

        assertEquals("50000", vm.uiState.value.rawAmount)
        assertEquals(50_000L, vm.uiState.value.parsedAmount)
    }

    @Test
    fun `onAmountChange caps input at 12 digits`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("1234567890123456789")

        // take(12)
        assertEquals(12, vm.uiState.value.rawAmount.length)
        assertEquals("123456789012", vm.uiState.value.rawAmount)
    }

    @Test
    fun `onAmountChange with no digits yields zero parsed`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("abc")

        assertEquals("", vm.uiState.value.rawAmount)
        assertEquals(0L, vm.uiState.value.parsedAmount)
    }

    @Test
    fun `onTransactionTypeChange to INCOME selects SALARY category`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.INCOME)

        assertEquals(TransactionType.INCOME, vm.uiState.value.transactionType)
        assertEquals(TransactionCategory.SALARY, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `onTransactionTypeChange to TRANSFER selects TRANSFER category`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)

        assertEquals(TransactionCategory.TRANSFER, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `saveTransaction rejects zero amount with error message`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.saveTransaction()

        assertEquals(
            "Tolong masukkan jumlah nominal yang valid",
            vm.uiState.value.errorMessage
        )
        assertFalse(vm.uiState.value.isSuccess)
    }

    @Test
    fun `saveTransaction rejects missing wallet selection`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("10000")
        vm.saveTransaction()

        assertEquals(
            "Tolong pilih dompet terlebih dahulu",
            vm.uiState.value.errorMessage
        )
    }

    @Test
    fun `saveTransaction rejects transfer with missing destination wallet`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(listOf(
            Wallet(id = "w1", name = "Cash", balance = 0L)
        ))
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        // destination wallet not set (only one wallet, so selectedToWalletId == w1 == selectedWalletId)
        vm.onToWalletChange("w1")
        vm.saveTransaction()

        // Either "missing destination" or "same source/destination" is acceptable
        val msg = vm.uiState.value.errorMessage
        assertTrue(
            "Expected transfer validation error, got: $msg",
            msg == "Tolong pilih dompet tujuan transfer" ||
                msg == "Dompet asal dan dompet tujuan tidak boleh sama"
        )
    }

    @Test
    fun `saveTransaction rejects transfer where source equals destination`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.onWalletChange("w1")
        vm.onToWalletChange("w1")
        vm.saveTransaction()

        assertEquals(
            "Dompet asal dan dompet tujuan tidak boleh sama",
            vm.uiState.value.errorMessage
        )
    }

    @Test
    fun `saveTransaction success flips isSuccess true and clears error`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        coEvery {
            transactionRepository.createTransaction(any<CreateTransactionRequest>())
        } returns Result.success(
            Transaction(
                id = "new-tx",
                amount = 50_000L,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                merchant = "Test",
                date = "2026-08-19"
            )
        )

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isSuccess)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `saveTransaction failure surfaces error message and clears loading`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        coEvery {
            transactionRepository.createTransaction(any<CreateTransactionRequest>())
        } returns Result.failure(RuntimeException("Network down"))

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSuccess)
        assertEquals("Network down", vm.uiState.value.errorMessage)
    }

    @Test
    fun `saveTransaction with blank merchant falls back to category display name`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val captured = mutableListOf<CreateTransactionRequest>()
        coEvery {
            transactionRepository.createTransaction(capture(captured))
        } returns Result.success(
            Transaction(
                id = "x", amount = 1L, type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD, merchant = "", date = "2026-08-19"
            )
        )

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onAmountChange("10000")
        // merchant left blank
        vm.saveTransaction()
        advanceUntilIdle()

        assertEquals(1, captured.size)
        // Default merchant for EXPENSE is the category's displayName ("Food & Beverage")
        assertEquals(
            TransactionCategory.FOOD.displayName,
            captured.first().merchant
        )
    }

    @Test
    fun `saveTransaction transfer uses synthesized merchant when blank`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val captured = mutableListOf<CreateTransactionRequest>()
        coEvery {
            transactionRepository.createTransaction(capture(captured))
        } returns Result.success(
            Transaction(
                id = "x", amount = 1L, type = TransactionType.TRANSFER,
                category = TransactionCategory.TRANSFER, merchant = "", date = "2026-08-19"
            )
        )

        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.onWalletChange("w1")
        vm.onToWalletChange("w2")
        vm.saveTransaction()
        advanceUntilIdle()

        assertEquals(1, captured.size)
        assertEquals("Transfer Cash ke Bank", captured.first().merchant)
    }

    @Test
    fun `uiState exposes StateFlow that emits state changes`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = AddTransactionViewModel(walletRepository, transactionRepository)
        advanceUntilIdle()

        vm.uiState.test {
            // Initial emission after wallets loaded
            assertEquals("w1", awaitItem().selectedWalletId)

            vm.onAmountChange("1000")
            val amountState = awaitItem()
            assertEquals("1000", amountState.rawAmount)
            assertEquals(1_000L, amountState.parsedAmount)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
