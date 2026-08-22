package com.ssajudn.barebudget.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.OutboxEntity
import com.ssajudn.barebudget.data.local.room.OutboxState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboxScheduler @Inject constructor(@ApplicationContext private val ctx: Context, private val db: AppDatabase) {
    private val gson = Gson()

    fun enqueue(ownerId: String, type: String, entityId: String, payload: Any) {
        val json = gson.toJson(payload)
        val key = "$ownerId:$type:$entityId:${UUID.randomUUID().toString().take(8)}"
        db.outboxDao().insert(OutboxEntity(id = UUID.randomUUID().toString(), ownerId = ownerId, entityType = type, entityId = entityId, payloadJson = json, idempotencyKey = key, state = OutboxState.PENDING.name))
        schedule()
    }

    fun schedule() {
        val req = OneTimeWorkRequestBuilder<OutboxWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(ctx).enqueueUniqueWork("outbox_sync", ExistingWorkPolicy.APPEND, req)
    }
}
