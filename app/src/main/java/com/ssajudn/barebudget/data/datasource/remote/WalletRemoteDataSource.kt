package com.ssajudn.barebudget.data.datasource.remote

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalWalletEntity
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.data.network.ApiService
import com.ssajudn.barebudget.domain.repository.WalletRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase
) {

    suspend fun getWallets(): Result<List<Wallet>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getWallets()
            if (response.isSuccessful && response.body() != null) {
                val wallets = response.body()!!
                db.walletDao().clearAll()
                db.walletDao().insertWallets(wallets.map { LocalWalletEntity.fromWallet(it, isSynced = true) })
                Result.success(wallets)
            } else {
                val cached = db.walletDao().getAllWallets().map { it.toWallet() }
                if (cached.isNotEmpty()) Result.success(cached)
                else Result.failure(ApiErrorParser.parse(response))
            }
        } catch (e: Exception) {
            val cached = db.walletDao().getAllWallets().map { it.toWallet() }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createWallet(request: CreateWalletRequest): Result<Wallet> = withContext(Dispatchers.IO) {
        try {
            val response = api.createWallet(request)
            if (response.isSuccessful && response.body() != null) {
                val wallet = response.body()!!
                db.walletDao().insertWallet(LocalWalletEntity.fromWallet(wallet, isSynced = true))
                Result.success(wallet)
            } else {
                Result.failure(ApiErrorParser.parse(response))
            }
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun deleteWallet(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteWallet(id)
            if (response.isSuccessful) {
                db.walletDao().deleteWallet(id)
                Result.success(true)
            } else {
                Result.failure(ApiErrorParser.parse(response))
            }
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeWallets(): Flow<List<Wallet>> =
        db.walletDao().observeAllWallets().map { list -> list.map { it.toWallet() } }
}