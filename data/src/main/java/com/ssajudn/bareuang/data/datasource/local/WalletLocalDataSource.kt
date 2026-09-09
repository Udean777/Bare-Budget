package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalWalletEntity
import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.data.mapper.PersistenceMappers
import com.ssajudn.bareuang.data.error.ApiErrorParser
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletLocalDataSource @Inject constructor(private val db: AppDatabase) {

    suspend fun getWallets(): Result<List<Wallet>> = withContext(Dispatchers.IO) {
        try {
            val local = db.walletDao().getAllWallets().map { it.toWallet() }
            if (local.isEmpty()) {
                val defaultWallet = Wallet(
                    id = UUID.randomUUID().toString(),
                    name = PersistenceMappers.DEFAULT_WALLET_NAME,
                    balance = 0L,
                    colorHex = PersistenceMappers.DEFAULT_WALLET_COLOR,
                    iconName = PersistenceMappers.DEFAULT_ICON
                )
                db.walletDao().insertWallet(
                    LocalWalletEntity.fromWallet(
                        defaultWallet,
                        isSynced = false
                    )
                )
                Result.success(listOf(defaultWallet))
            } else {
                // Mencegah dan membersihkan duplikasi otomatis jika ada nama dompet yang sama persis
                val uniqueWallets = local.distinctBy { it.name }
                if (uniqueWallets.size < local.size) {
                    db.withTransaction {
                        db.walletDao().clearAll()
                        uniqueWallets.forEach { w ->
                            db.walletDao().insertWallet(
                                LocalWalletEntity.fromWallet(w, isSynced = false)
                            )
                        }
                    }
                }
                Result.success(uniqueWallets)
            }
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createWallet(request: CreateWalletRequest): Result<Wallet> = withContext(Dispatchers.IO) {
        try {
            if (request.name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Nama dompet tidak boleh kosong"))
            if (request.balance < 0) return@withContext Result.failure(IllegalArgumentException("Saldo tidak boleh negatif"))
            val wallet = Wallet(
                id = UUID.randomUUID().toString(),
                name = request.name.trim(),
                balance = request.balance,
                colorHex = request.colorHex,
                iconName = request.iconName
            )
            db.walletDao().insertWallet(LocalWalletEntity.fromWallet(wallet, isSynced = false))
            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun updateWallet(wallet: Wallet): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (wallet.name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Nama dompet tidak boleh kosong"))
            val existing = db.walletDao().getWalletById(wallet.id!!)
                ?: return@withContext Result.failure(Exception("Dompet tidak ditemukan"))
            db.walletDao().insertWallet(
                existing.copy(name = wallet.name.trim(), colorHex = wallet.colorHex, isSynced = false)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun deleteWallet(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.walletDao().deleteWallet(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeWallets(): Flow<List<Wallet>> =
        db.walletDao().observeAllWallets().map { list -> list.map { it.toWallet() } }
}
