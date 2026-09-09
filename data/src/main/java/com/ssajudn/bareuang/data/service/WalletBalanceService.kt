package com.ssajudn.bareuang.data.service

import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.data.local.room.WalletDao
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.data.mapper.PersistenceMappers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain service — satu-satunya tempat yang boleh memanggil `walletDao.updateBalance`.
 * Semua repository (Local/Remote Transaction/Goal/DueBill) harus via service ini (SRP).
 * Lokasi di `data/service` (bukan `data/repository`) agar tidak depend siklus ke `WalletRepository`.
 */
@Singleton
class WalletBalanceService @Inject constructor(
    private val walletDao: WalletDao
) {
    fun add(walletId: String?, amount: Long) {
        if (!walletId.isNullOrBlank() && amount != 0L) walletDao.updateBalance(walletId, amount)
    }

    fun adjustForCreate(request: CreateTransactionRequest) {
        if (request.type == TransactionType.TRANSFER) {
            add(request.walletId, -request.amount)
            add(request.toWalletId, request.amount)
        } else {
            val adj = if (request.type == TransactionType.INCOME) request.amount else -request.amount
            add(request.walletId, adj)
        }
    }

    fun revert(tx: LocalTransactionEntity) {
        val type = PersistenceMappers.safeTransactionType(tx.type)
        if (type == TransactionType.TRANSFER) {
            add(tx.walletId, tx.amount)
            add(tx.toWalletId, -tx.amount)
        } else if (tx.walletId != null) {
            val adj = if (type == TransactionType.INCOME) -tx.amount else tx.amount
            add(tx.walletId, adj)
        }
    }
}
