package com.ssajudn.bareuang.data.notification

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dedup store: satu kombinasi bill+dueDate+urgency hanya dinotifikasikan sekali,
 * sehingga worker periodik tidak spam. Key menyertakan dueDate, jadi tagihan
 * recurring periode berikutnya otomatis memicu pengingat baru.
 */
@Singleton
class BillReminderPrefs @Inject constructor(
    @ApplicationContext context: Context
) : com.ssajudn.bareuang.domain.port.BillReminderPreferencesPort {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    fun alreadyShown(key: String): Boolean = prefs.getStringSet(KEY_SHOWN, emptySet()).orEmpty().contains(key)

    fun markShown(key: String) {
        val current = prefs.getStringSet(KEY_SHOWN, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(key)
        // Trim agar set tidak tumbuh tanpa batas
        while (current.size > MAX_ENTRIES) {
            current.remove(current.first())
        }
        prefs.edit { putStringSet(KEY_SHOWN, current) }
    }

    companion object {
        private const val PREF_NAME = "bill_reminder_prefs"
        private const val KEY_SHOWN = "shown_reminder_keys"
        private const val KEY_PERM_ASKED = "notif_perm_asked"
        private const val KEY_ENABLED = "reminders_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        private const val MAX_ENTRIES = 300

        fun markPermissionAsked(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_PERM_ASKED, true) }
        }

        fun isPermissionAsked(context: Context): Boolean =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PERM_ASKED, false)

        fun setRemindersEnabled(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit { putBoolean(KEY_ENABLED, enabled) }
        }
    }

    override fun notificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_ENABLED, true)

    // Default 00:00 (12 malam)
    override fun reminderHour(): Int = prefs.getInt(KEY_HOUR, 0)

    override fun reminderMinute(): Int = prefs.getInt(KEY_MINUTE, 0)

    override fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit { putInt(KEY_HOUR, hour); putInt(KEY_MINUTE, minute) }
    }
}
