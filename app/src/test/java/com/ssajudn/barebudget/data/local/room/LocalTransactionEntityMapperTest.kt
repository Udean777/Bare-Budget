package com.ssajudn.barebudget.data.local.room

import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Characterization test for [LocalTransactionEntity] mapper functions.
 * Locks the (de)serialization contract between the Room entity and the
 * domain model, including the fallback behavior for invalid enum strings.
 */
class LocalTransactionEntityMapperTest {

    @Test
    fun `roundtrip preserves all fields for a complete transaction`() {
        val original = Transaction(
            id = "tx-123",
            amount = 50_000L,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            merchant = "Employer",
            date = "2026-08-19",
            notes = "August salary",
            receiptUrl = "https://example.com/r.pdf",
            walletId = "wallet-1",
            toWalletId = null
        )

        val entity = LocalTransactionEntity.fromTransaction(original, isSynced = true)
        val back = entity.toTransaction()

        assertEquals(original.id, back.id)
        assertEquals(original.amount, back.amount)
        assertEquals(original.type, back.type)
        assertEquals(original.category, back.category)
        assertEquals(original.merchant, back.merchant)
        assertEquals(original.date, back.date)
        assertEquals(original.notes, back.notes)
        assertEquals(original.receiptUrl, back.receiptUrl)
        assertEquals(original.walletId, back.walletId)
        assertEquals(original.toWalletId, back.toWalletId)
        assertEquals(true, entity.isSynced)
    }

    @Test
    fun `roundtrip preserves transfer with toWalletId`() {
        val original = Transaction(
            id = "tx-456",
            amount = 10_000L,
            type = TransactionType.TRANSFER,
            category = TransactionCategory.TRANSFER,
            merchant = "Transfer",
            date = "2026-08-19",
            notes = null,
            receiptUrl = null,
            walletId = "wallet-1",
            toWalletId = "wallet-2"
        )

        val back = LocalTransactionEntity.fromTransaction(original).toTransaction()
        assertEquals("wallet-2", back.toWalletId)
        assertEquals(TransactionType.TRANSFER, back.type)
    }

    @Test
    fun `toTransaction falls back to EXPENSE for invalid type string`() {
        val entity = LocalTransactionEntity(
            id = "x",
            amount = 1L,
            type = "INVALID_TYPE",
            category = "FOOD",
            merchant = null,
            date = "2026-08-19",
            notes = null,
            receiptUrl = null
        )

        assertEquals(TransactionType.EXPENSE, entity.toTransaction().type)
    }

    @Test
    fun `toTransaction falls back to OTHER for invalid category string`() {
        val entity = LocalTransactionEntity(
            id = "x",
            amount = 1L,
            type = "EXPENSE",
            category = "NOPE",
            merchant = null,
            date = "2026-08-19",
            notes = null,
            receiptUrl = null
        )

        assertEquals(TransactionCategory.OTHER, entity.toTransaction().category)
    }

    @Test
    fun `fromTransaction generates UUID when id is null`() {
        val original = Transaction(
            id = null,
            amount = 1L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            merchant = null,
            date = "2026-08-19",
            notes = null,
            receiptUrl = null
        )

        val entity = LocalTransactionEntity.fromTransaction(original)
        assertNotNull(entity.id)
        // Generated UUID should be a non-empty string
        assert(entity.id.isNotEmpty())
    }

    @Test
    fun `default isSynced is false when not specified`() {
        val original = Transaction(
            id = "x",
            amount = 1L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            merchant = null,
            date = "2026-08-19",
            notes = null,
            receiptUrl = null
        )

        val entity = LocalTransactionEntity.fromTransaction(original)
        assertEquals(false, entity.isSynced)
    }

    @Test
    fun `nullable merchant notes and receiptUrl are preserved as null`() {
        val original = Transaction(
            id = "x",
            amount = 1L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            merchant = null,
            date = "2026-08-19",
            notes = null,
            receiptUrl = null
        )

        val back = LocalTransactionEntity.fromTransaction(original).toTransaction()
        assertNull(back.merchant)
        assertNull(back.notes)
        assertNull(back.receiptUrl)
    }
}
