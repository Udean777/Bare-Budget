package com.ssajudn.barebudget.domain.repository

interface MigrationRepository {
    suspend fun migrateGuestData(guestUserId: String): Result<Boolean>
}
