package com.ssajudn.barebudget.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OutboxState { PENDING, IN_FLIGHT, DONE, FAILED_RETRYABLE, CONFLICT }

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val entityType: String, // transaction, wallet, goal_deposit, duebill_status, budget
    val entityId: String,
    val payloadJson: String,
    val idempotencyKey: String,
    val state: String = OutboxState.PENDING.name,
    val attempts: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val nextRetryAt: Long? = null
)
