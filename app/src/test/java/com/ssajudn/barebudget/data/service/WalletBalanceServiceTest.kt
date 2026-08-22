package com.ssajudn.barebudget.data.service

import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.data.local.room.WalletDao
import com.ssajudn.barebudget.data.local.room.LocalWalletEntity
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeWalletDao : WalletDao {
    val balances = mutableMapOf<String, Long>()
    val calls = mutableListOf<Pair<String, Long>>()

    override fun getAllWallets(): List<LocalWalletEntity> = emptyList()
    override fun getWalletsByOwner(ownerId: String): List<LocalWalletEntity> = emptyList()
    override fun observeAllWallets(): Flow<List<LocalWalletEntity>> = flowOf(emptyList())
    override fun observeWalletsByOwner(ownerId: String): Flow<List<LocalWalletEntity>> = flowOf(emptyList())
    override fun getFirstWallet(): LocalWalletEntity? = null
    override fun getWalletById(id: String): LocalWalletEntity? = null
    override fun insertWallet(wallet: LocalWalletEntity) {}
    override fun insertWallets(wallets: List<LocalWalletEntity>) {}
    override fun updateBalance(id: String, amount: Long) {
        calls.add(id to amount)
        balances[id] = (balances[id] ?: 0L) + amount
    }
    override fun deleteWallet(id: String) {}
    override fun clearAll() {}
}

class WalletBalanceServiceTest {

    @Test
    fun `add ignores blank id and zero amount`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        svc.add(null, 100L)
        svc.add("", 100L)
        svc.add("w1", 0L)

        assertEquals(0, dao.calls.size)
    }

    @Test
    fun `adjustForCreate expense subtracts from wallet`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        svc.adjustForCreate(
            CreateTransactionRequest(
                amount = 50000L,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                merchant = "Test",
                date = "2026-08-21",
                walletId = "w1"
            )
        )

        assertEquals(listOf("w1" to -50000L), dao.calls)
    }

    @Test
    fun `adjustForCreate income adds to wallet`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        svc.adjustForCreate(
            CreateTransactionRequest(
                amount = 100000L,
                type = TransactionType.INCOME,
                category = TransactionCategory.SALARY,
                merchant = "Gaji",
                date = "2026-08-21",
                walletId = "w1"
            )
        )

        assertEquals(listOf("w1" to 100000L), dao.calls)
    }

    @Test
    fun `adjustForCreate transfer moves between wallets`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        svc.adjustForCreate(
            CreateTransactionRequest(
                amount = 25000L,
                type = TransactionType.TRANSFER,
                category = TransactionCategory.TRANSFER,
                merchant = "Transfer",
                date = "2026-08-21",
                walletId = "w1",
                toWalletId = "w2"
            )
        )

        assertEquals(listOf("w1" to -25000L, "w2" to 25000L), dao.calls)
    }

    @Test
    fun `revert expense adds back to wallet`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        val tx = LocalTransactionEntity(
            id = "tx1",
            amount = 30000L,
            type = TransactionType.EXPENSE.name,
            category = TransactionCategory.FOOD.name,
            merchant = "Makan",
            date = "2026-08-21T00:00:00Z",
            notes = null,
            receiptUrl = null,
            walletId = "w1",
            isSynced = false
        )

        svc.revert(tx)

        assertEquals(listOf("w1" to 30000L), dao.calls)
    }

    @Test
    fun `revert income subtracts from wallet`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        val tx = LocalTransactionEntity(
            id = "tx2",
            amount = 70000L,
            type = TransactionType.INCOME.name,
            category = TransactionCategory.SALARY.name,
            merchant = "Gaji",
            date = "2026-08-21T00:00:00Z",
            notes = null,
            receiptUrl = null,
            walletId = "w1",
            isSynced = false
        )

        svc.revert(tx)

        assertEquals(listOf("w1" to -70000L), dao.calls)
    }

    @Test
    fun `revert transfer reverses both wallets`() {
        val dao = FakeWalletDao()
        val svc = WalletBalanceService(dao)

        val tx = LocalTransactionEntity(
            id = "tx3",
            amount = 40000L,
            type = TransactionType.TRANSFER.name,
            category = TransactionCategory.TRANSFER.name,
            merchant = "Transfer",
            date = "2026-08-21T00:00:00Z",
            notes = null,
            receiptUrl = null,
            walletId = "w1",
            toWalletId = "w2",
            isSynced = false
        )

        svc.revert(tx)

        assertEquals(listOf("w1" to 40000L, "w2" to -40000L), dao.calls)
    }
}
