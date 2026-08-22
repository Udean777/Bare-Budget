package com.ssajudn.barebudget.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE state IN ('PENDING','FAILED_RETRYABLE') AND (nextRetryAt IS NULL OR nextRetryAt <= :now) ORDER BY createdAt ASC LIMIT :limit")
    fun getPending(now: Long = System.currentTimeMillis(), limit: Int = 20): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox WHERE state IN ('PENDING','FAILED_RETRYABLE','IN_FLIGHT')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE state IN ('PENDING','FAILED_RETRYABLE','IN_FLIGHT')")
    fun getPendingCount(): Int

    @Query("UPDATE outbox SET state = :state, attempts = :attempts, nextRetryAt = :nextRetryAt WHERE id = :id")
    fun updateState(id: String, state: String, attempts: Int, nextRetryAt: Long?)

    @Query("DELETE FROM outbox WHERE id = :id")
    fun delete(id: String)

    @Query("DELETE FROM outbox WHERE state = 'DONE' AND createdAt < :before")
    fun deleteDoneBefore(before: Long)

    @Query("DELETE FROM outbox")
    fun clearAll()
}
