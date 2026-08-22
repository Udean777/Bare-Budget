package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalWalletEntity
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.data.repository.DomainMappers
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val sessionManager: com.ssajudn.barebudget.data.local.UserSessionManager? = null,
    private val outboxScheduler: com.ssajudn.barebudget.data.sync.OutboxScheduler? = null
) {

    suspend fun getWallets(): Result<List<Wallet>> = withContext(Dispatchers.IO) {
        try {
            val local = db.walletDao().getAllWallets().map { it.toWallet() }
            if (local.isEmpty()) {
                val defaultWallet = Wallet(
                    id = UUID.randomUUID().toString(),
                    name = DomainMappers.DEFAULT_WALLET_NAME,
                    balance = 0L,
                    colorHex = DomainMappers.DEFAULT_WALLET_COLOR,
                    iconName = DomainMappers.DEFAULT_ICON
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
                    // Jika ada duplikasi di DB lokal, bersihkan duplikat
                    db.walletDao().clearAll()
                    uniqueWallets.forEach { w ->
                        db.walletDao().insertWallet(
                            LocalWalletEntity.fromWallet(w, isSynced = false)
                        )
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
            val wallet = Wallet(
                id = UUID.randomUUID().toString(),
                name = request.name,
                balance = request.balance,
                colorHex = request.colorHex,
                iconName = request.iconName
            )
            val ownerId = sessionManager?.userId ?: ""
            db.walletDao().insertWallet(LocalWalletEntity.fromWallet(wallet, isSynced = false).copy(ownerId = ownerId))
            if (sessionManager != null && !sessionManager.isGuestMode && outboxScheduler != null) {
                val dto = com.ssajudn.barebudget.data.network.dto.CreateWalletRequestDto(request.name, request.balance, request.colorHex, request.iconName)
                try { outboxScheduler.enqueue(ownerId, "wallet", wallet.id!!, dto) } catch (_: Exception) {}
            }
            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun updateWallet(wallet: Wallet): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = db.walletDao().getWalletById(wallet.id!!)
                ?: return@withContext Result.failure(Exception("Dompet tidak ditemukan"))
            db.walletDao().insertWallet(
                existing.copy(name = wallet.name, colorHex = wallet.colorHex, isSynced = false)
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