package com.ssajudn.barebudget.domain.repository

import com.ssajudn.barebudget.domain.model.CreateDueBillRequest
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.UpdateDueBillRequest
import kotlinx.coroutines.flow.Flow

interface DueBillRepository {
    suspend fun getDueBills(status: String? = null): Result<List<DueBill>>
    suspend fun createDueBill(request: CreateDueBillRequest): Result<DueBill>
    suspend fun updateDueBill(id: String, request: UpdateDueBillRequest): Result<Boolean>
    suspend fun updateDueBillStatus(id: String, status: DueBillStatus, walletId: String? = null): Result<Boolean>
    suspend fun deleteDueBill(id: String): Result<Boolean>
    fun observeDueBills(): Flow<List<DueBill>>
}
