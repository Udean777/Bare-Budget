package com.ssajudn.bareuang.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ssajudn.bareuang.data.R
import com.ssajudn.bareuang.domain.usecase.BillReminder
import com.ssajudn.bareuang.domain.usecase.BillReminderUrgency
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Membangun dan menampilkan satu notifikasi ringkasan pengingat tagihan.
 * Semua teks diambil dari resource agar mengikuti bahasa aplikasi (id/en).
 */
@Singleton
class BillNotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val currencyPreferences: com.ssajudn.bareuang.data.local.CurrencyPreferences
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.bill_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.bill_reminder_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** @return true jika notifikasi berhasil diposting (izin ada, tidak error). */
    fun showSummary(reminders: List<BillReminder>): Boolean {
        if (reminders.isEmpty()) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false

        ensureChannel()

        val overdueCount = reminders.count { it.urgency == BillReminderUrgency.OVERDUE }
        val title = when {
            overdueCount > 0 -> context.resources.getQuantityString(R.plurals.bill_reminder_title_overdue, overdueCount, overdueCount)
            else -> context.resources.getQuantityString(R.plurals.bill_reminder_title_upcoming, reminders.size, reminders.size)
        }

        val preview = reminders.take(MAX_LINES).joinToString("\n") { reminder ->
            "• ${reminder.providerName} · ${formatAmount(reminder.amount)} (${dueLabel(reminder)})"
        }
        val extra = reminders.size - MAX_LINES
        val body = if (extra > 0) {
            "$preview\n" + context.resources.getQuantityString(R.plurals.bill_reminder_more, extra, extra)
        } else {
            preview
        }

        // Membuka aplikasi; pengingat tagihan tetap diproses lokal.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bill_reminder)
            .setColor(ACCENT_COLOR)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.isSuccess
    }

    private fun formatAmount(amount: Long): String =
        DomainCurrencyFormatter.format(amount, currencyPreferences.getCurrency())

    private fun dueLabel(reminder: BillReminder): String = when (reminder.urgency) {
        BillReminderUrgency.OVERDUE -> context.resources.getQuantityString(R.plurals.bill_reminder_overdue_by, (-reminder.daysLeft).toInt(), -reminder.daysLeft)
        BillReminderUrgency.TODAY -> context.getString(R.string.bill_reminder_due_today)
        BillReminderUrgency.TOMORROW -> context.getString(R.string.bill_reminder_due_tomorrow)
        BillReminderUrgency.SOON -> context.resources.getQuantityString(R.plurals.bill_reminder_due_in, reminder.daysLeft.toInt(), reminder.daysLeft)
    }

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val NOTIFICATION_ID = 4201
        private const val MAX_LINES = 3

        // Warna aksen Bareuang (honey) untuk notifikasi — selaras DESIGN.MD primary-container
        private const val ACCENT_COLOR = 0xFF845400.toInt()
    }
}
