package com.ssajudn.bareuang.ui.transaction

import app.cash.turbine.test
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import io.mockk.*
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
    private val budgetRepository: BudgetRepository = mockk()
    private val checkDailyBudget: CheckDailyBudgetUseCase = mockk(relaxed = true)

    private fun createVm(monthlyBudget: Long = 2_000_000L): AddTransactionViewModel {
        coEvery { budgetRepository.getMonthlyBudget(any()) } returns Result.success(monthlyBudget)
        every { budgetRepository.getCategoryBudgets(any()) } returns kotlinx.coroutines.flow.flowOf(emptyList())
        val hasMonthlyBudget = com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase(budgetRepository)
        // Daily-budget gate defaults to allowed; individual tests override as needed.
        coEvery { checkDailyBudget(any(), any(), any(), any()) } returns Result.success(Unit)
        return AddTransactionViewModel(
            walletRepository, budgetRepository, hasMonthlyBudget, checkDailyBudget,
            com.ssajudn.bareuang.domain.usecase.CreateTransactionUseCase(transactionRepository),
            com.ssajudn.bareuang.domain.usecase.ValidateTransactionUseCase()
        )
    }

    private fun walletsFixture(): List<Wallet> = listOf(
        Wallet(id = "w1", name = "Cash", balance = 100_000L),
        Wallet(id = "w2", name = "Bank", balance = 500_000L)
    )

    @Test
    fun `init loads wallets and selects first as default source`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())

        val vm = createVm()
        advanceUntilIdle()

        assertEquals(walletsFixture(), vm.uiState.value.wallets)
        assertEquals("w1", vm.uiState.value.selectedWalletId)
        // Transfer default target falls back to second wallet (or first if absent)
        assertEquals("w2", vm.uiState.value.selectedToWalletId)
    }

    @Test
    fun `init handles empty wallet list without crashing`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())

        val vm = createVm()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.wallets.isEmpty())
        assertNull(vm.uiState.value.selectedWalletId)
    }

    @Test
    fun `onAmountChange strips non-digits and parses to Long`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onAmountChange("Rp 50.000")

        assertEquals("50000", vm.uiState.value.rawAmount)
        assertEquals(50_000L, vm.uiState.value.parsedAmount)
    }

    @Test
    fun `onAmountChange caps input at 12 digits`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onAmountChange("1234567890123456789")

        // take(12)
        assertEquals(12, vm.uiState.value.rawAmount.length)
        assertEquals("123456789012", vm.uiState.value.rawAmount)
    }

    @Test
    fun `onAmountChange with no digits yields zero parsed`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onAmountChange("abc")

        assertEquals("", vm.uiState.value.rawAmount)
        assertEquals(0L, vm.uiState.value.parsedAmount)
    }

    @Test
    fun `onTransactionTypeChange to INCOME selects SALARY category`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.INCOME)

        assertEquals(TransactionType.INCOME, vm.uiState.value.transactionType)
        assertEquals(TransactionCategory.SALARY, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `onTransactionTypeChange to TRANSFER selects TRANSFER category`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)

        assertEquals(TransactionCategory.TRANSFER, vm.uiState.value.selectedCategory)
    }

    @Test
    fun `saveTransaction rejects zero amount with error message`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = createVm()
        advanceUntilIdle()

        vm.saveTransaction()

        assertEquals(AddTransactionError.INVALID_AMOUNT, vm.uiState.value.validationError)
        assertFalse(vm.uiState.value.isSuccess)
    }

    @Test
    fun `saveTransaction rejects missing wallet selection`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())
        val vm = createVm()
        advanceUntilIdle()

        vm.onAmountChange("10000")
        vm.saveTransaction()

        assertEquals(AddTransactionError.WALLET_REQUIRED, vm.uiState.value.validationError)
    }

    @Test
    fun `saveTransaction rejects transfer with missing destination wallet`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(listOf(
            Wallet(id = "w1", name = "Cash", balance = 0L)
        ))
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.onToWalletChange("w1")
        vm.saveTransaction()

        val err = vm.uiState.value.validationError
        assertTrue(
            "Expected transfer validation error, got: $err",
            err == AddTransactionError.TO_WALLET_REQUIRED ||
                err == AddTransactionError.SAME_WALLET
        )
    }

    @Test
    fun `saveTransaction rejects transfer where source equals destination when only one wallet exists`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(listOf(
            Wallet(id = "w1", name = "Cash", balance = 100_000L)
        ))
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.onWalletChange("w1")
        vm.onToWalletChange("w1")
        vm.saveTransaction()

        assertEquals(AddTransactionError.SAME_WALLET, vm.uiState.value.validationError)
    }

    @Test
    fun `onWalletChange smart switches destination wallet when selecting current destination`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        assertEquals("w1", vm.uiState.value.selectedWalletId)
        assertEquals("w2", vm.uiState.value.selectedToWalletId)

        // User picks "w2" as source wallet (which matches current destination "w2")
        vm.onWalletChange("w2")

        // Destination should smart-switch to "w1"
        assertEquals("w2", vm.uiState.value.selectedWalletId)
        assertEquals("w1", vm.uiState.value.selectedToWalletId)
    }

    @Test
    fun `onToWalletChange smart switches source wallet when selecting current source`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        assertEquals("w1", vm.uiState.value.selectedWalletId)
        assertEquals("w2", vm.uiState.value.selectedToWalletId)

        // User picks "w1" as destination wallet (which matches current source "w1")
        vm.onToWalletChange("w1")

        // Source should smart-switch to "w2"
        assertEquals("w2", vm.uiState.value.selectedWalletId)
        assertEquals("w1", vm.uiState.value.selectedToWalletId)
    }

    @Test
    fun `swapWallets correctly swaps source and destination wallets`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        assertEquals("w1", vm.uiState.value.selectedWalletId)
        assertEquals("w2", vm.uiState.value.selectedToWalletId)

        vm.swapWallets()

        assertEquals("w2", vm.uiState.value.selectedWalletId)
        assertEquals("w1", vm.uiState.value.selectedToWalletId)
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

        val vm = createVm()
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

        val vm = createVm()
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

        val vm = createVm()
        advanceUntilIdle()

        vm.onAmountChange("10000")
        // merchant left blank
        vm.saveTransaction()
        advanceUntilIdle()

        assertEquals(1, captured.size)
        // Default merchant for EXPENSE is the semantic category name.
        assertEquals(
            TransactionCategory.FOOD.name,
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

        val vm = createVm()
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.onWalletChange("w1")
        vm.onToWalletChange("w2")
        vm.saveTransaction()
        advanceUntilIdle()

        assertEquals(1, captured.size)
        assertEquals("Cash \u2192 Bank", captured.first().merchant)
    }

    @Test
    fun `uiState exposes StateFlow that emits state changes`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        val vm = createVm()
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

    @Test
    fun `saveTransaction expense blocked when monthly budget not set`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())

        val vm = createVm(monthlyBudget = 0L)
        advanceUntilIdle()

        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()

        assertEquals(AddTransactionError.BUDGET_REQUIRED, vm.uiState.value.validationError)
        assertFalse(vm.uiState.value.isSuccess)
        coVerify(exactly = 0) { transactionRepository.createTransaction(any()) }
    }

    @Test
    fun `saveTransaction transfer allowed when monthly budget not set`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        coEvery {
            transactionRepository.createTransaction(any<CreateTransactionRequest>())
        } returns Result.success(
            Transaction(
                id = "x", amount = 1L, type = TransactionType.TRANSFER,
                category = TransactionCategory.TRANSFER, merchant = "", date = "2026-08-19"
            )
        )

        val vm = createVm(monthlyBudget = 0L)
        advanceUntilIdle()

        vm.onTransactionTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("10000")
        vm.saveTransaction()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSuccess)
    }

    @Test
    fun `init flags isBudgetMissing when budget not set`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(emptyList())

        val vm = createVm(monthlyBudget = 0L)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isBudgetMissing)
    }

    @Test
    fun `saveTransaction expense over daily budget shows soft-nudge prompt and does not save yet`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())

        val vm = createVm()
        advanceUntilIdle()

        // Override the default pass-through stub: today's remaining allowance is less than 50000.
        coEvery { checkDailyBudget(any(), any(), any(), any()) } returns Result.failure(
            AppException.DataException("Melebihi jatah harian. Sisa hari ini Rp 10.000.")
        )

        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()

        // Soft nudge: not blocked outright — prompts for confirmation instead.
        assertTrue(vm.uiState.value.pendingDailyOverride)
        assertEquals("Melebihi jatah harian. Sisa hari ini Rp 10.000.", vm.uiState.value.pendingDailyMessage)
        assertFalse(vm.uiState.value.isSuccess)
        coVerify(exactly = 0) { transactionRepository.createTransaction(any()) }
    }

    @Test
    fun `confirmDailyOverride saves the transaction after the prompt`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())
        coEvery {
            transactionRepository.createTransaction(any<CreateTransactionRequest>())
        } returns Result.success(
            Transaction(
                id = "new-tx", amount = 50_000L, type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD, merchant = "Test", date = "2026-08-19"
            )
        )

        val vm = createVm()
        advanceUntilIdle()

        coEvery { checkDailyBudget(any(), any(), any(), any()) } returns Result.failure(
            AppException.DataException("Melebihi jatah harian. Sisa hari ini Rp 10.000.")
        )
        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.pendingDailyOverride)

        vm.confirmDailyOverride()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.pendingDailyOverride)
        assertTrue(vm.uiState.value.isSuccess)
        coVerify(exactly = 1) { transactionRepository.createTransaction(any()) }
    }

    @Test
    fun `dismissDailyOverride cancels without saving`() = runTest {
        coEvery { walletRepository.getWallets() } returns Result.success(walletsFixture())

        val vm = createVm()
        advanceUntilIdle()

        coEvery { checkDailyBudget(any(), any(), any(), any()) } returns Result.failure(
            AppException.DataException("Melebihi jatah harian. Sisa hari ini Rp 10.000.")
        )
        vm.onAmountChange("50000")
        vm.saveTransaction()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.pendingDailyOverride)

        vm.dismissDailyOverride()

        assertFalse(vm.uiState.value.pendingDailyOverride)
        assertFalse(vm.uiState.value.isSuccess)
        coVerify(exactly = 0) { transactionRepository.createTransaction(any()) }
    }
}
