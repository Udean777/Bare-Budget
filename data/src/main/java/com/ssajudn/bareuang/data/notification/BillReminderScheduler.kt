package com.ssajudn.bareuang.data.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Menjadwalkan pengingat harian pada jam pilihan user (default 00:00),
 * plus one-time setiap app dibuka agar notifikasi muncul tanpa menunggu jadwal.
 * Rantai harian diperpanjang ulang oleh [BillReminderWorker] setiap selesai jalan.
 */
@Singleton
class BillReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort {

    override fun scheduleDailyAt(hour: Int, minute: Int) {
        val request = OneTimeWorkRequestBuilder<BillReminderWorker>()
            .setInitialDelay(millisUntil(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            BillReminderWorker.UNIQUE_DAILY,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun runNow() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            BillReminderWorker.UNIQUE_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<BillReminderWorker>().build()
        )
    }

    private fun millisUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis - now.timeInMillis
    }
}
