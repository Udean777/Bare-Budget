package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CreateWalletRequest
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.data.datasource.local.WalletLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.WalletRemoteDataSource
import com.ssajudn.barebudget.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val local: WalletLocalDataSource,
    private val remote: WalletRemoteDataSource,
    private val sessionManager: UserSessionManager
) : WalletRepository {

    override suspend fun getWallets(): Result<List<Wallet>> =
        if (sessionManager.isGuestMode) local.getWallets()
        else remote.getWallets()

    override suspend fun createWallet(request: CreateWalletRequest): Result<Wallet> =
        if (sessionManager.isGuestMode) local.createWallet(request)
        else remote.createWallet(request)

    override suspend fun deleteWallet(id: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.deleteWallet(id)
        else remote.deleteWallet(id)

    override fun observeWallets(): Flow<List<Wallet>> =
        if (sessionManager.isGuestMode) local.observeWallets()
        else remote.observeWallets()
}
