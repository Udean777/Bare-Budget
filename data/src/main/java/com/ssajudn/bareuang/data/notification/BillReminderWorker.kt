package com.ssajudn.bareuang.data.notification

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.usecase.BuildBillRemindersUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Memeriksa tagihan UNPAID yang mendekati/lewat jatuh tempo dan menampilkan
 * notifikasi ringkasan. Aman dijalankan berkali-kali: dedup via [BillReminderPrefs].
 */
@HiltWorker
class BillReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dueBillRepository: DueBillRepository,
    private val buildReminders: BuildBillRemindersUseCase,
    private val prefs: BillReminderPrefs,
    private val notifier: BillNotificationHelper,
    private val scheduler: BillReminderScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Perpanjang rantai harian untuk besok, di semua jalur keluar.
        scheduler.scheduleDailyAt(prefs.reminderHour(), prefs.reminderMinute())

        if (!prefs.notificationsEnabled()) {
            Log.d(TAG, "Skip: reminders disabled in prefs")
            return Result.success()
        }

        // Jangan bakar key dedup kalau izin notifikasi mati — pengingat akan
        // muncul begitu izin diberikan, bukan hilang selamanya.
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            Log.d(TAG, "Skip: POST_NOTIFICATIONS tidak granted")
            return Result.success()
        }

        val bills = dueBillRepository.getDueBills(DueBillStatus.UNPAID.name)
            .getOrDefault(emptyList())
        Log.d(TAG, "Unpaid bills: ${bills.size} -> ${bills.map { "${it.providerName} due=${it.dueDate}" }}")

        val reminders = buildReminders(bills) { bill ->
            runCatching { com.ssajudn.bareuang.domain.utils.DateUtils.getDaysUntilDue(bill.dueDate) }
                .getOrDefault(Long.MAX_VALUE)
        }
        Log.d(TAG, "Reminders in window: ${reminders.map { "${it.providerName} daysLeft=${it.daysLeft} urgency=${it.urgency}" }}")

        val fresh = reminders.filter { !prefs.alreadyShown(dedupeKey(it.billId, it.dueDateIso, it.urgency.name)) }

        // Tandai shown HANYA setelah notifikasi benar-benar tampil.
        if (fresh.isNotEmpty() && notifier.showSummary(fresh)) {
            fresh.forEach { prefs.markShown(dedupeKey(it.billId, it.dueDateIso, it.urgency.name)) }
        } else if (fresh.isEmpty() && reminders.isNotEmpty()) {
            Log.d(TAG, "Semua reminder sudah pernah tampil (dedup)")
        }
        return Result.success()
    }

    private fun dedupeKey(billId: String, dueDateIso: String, urgency: String) =
        "$billId:$dueDateIso:$urgency"

    companion object {
        private const val TAG = "BillReminder"
        const val UNIQUE_ONE_TIME = "bill_reminder_once"
        const val UNIQUE_DAILY = "bill_reminder_daily"
    }
}
