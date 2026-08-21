package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.UpdateDueBillRequest
import com.ssajudn.barebudget.data.datasource.local.DueBillLocalDataSource
import com.ssajudn.barebudget.data.datasource.remote.DueBillRemoteDataSource
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DueBillRepositoryImpl @Inject constructor(
    private val local: DueBillLocalDataSource,
    private val remote: DueBillRemoteDataSource,
    private val sessionManager: UserSessionManager
) : DueBillRepository {

    override suspend fun getDueBills(status: String?): Result<List<DueBill>> =
        if (sessionManager.isGuestMode) local.getDueBills(status)
        else remote.getDueBills(status)

    override suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill> =
        if (sessionManager.isGuestMode) local.createDueBill(request)
        else remote.createDueBill(request)

    override suspend fun updateDueBill(id: String, request: UpdateDueBillRequest): Result<Boolean> =
        if (sessionManager.isGuestMode) local.updateDueBill(id, request)
        else remote.updateDueBill(id, request)

    override suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String?): Result<Boolean> =
        if (sessionManager.isGuestMode) local.updateDueBillStatus(id, status, walletId)
        else remote.updateDueBillStatus(id, status, walletId)

    override suspend fun deleteDueBill(id: String): Result<Boolean> =
        if (sessionManager.isGuestMode) local.deleteDueBill(id)
        else remote.deleteDueBill(id)

    override fun observeDueBills(): Flow<List<DueBill>> =
        if (sessionManager.isGuestMode) local.observeDueBills()
        else remote.observeDueBills()
}
