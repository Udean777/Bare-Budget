package com.ssajudn.barebudget.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted tour-guide state.
 *
 * Own SharedPreferences file (not the session one) for the same reason as
 * ThemePreferences: UserSessionManager.clearSession wipes its prefs file,
 * and the tour-completed flag must survive sign-out.
 */
@Singleton
class TourPreferences @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val isTourCompleted: Boolean
        get() = prefs.getBoolean(KEY_TOUR_COMPLETED, false)

    fun markTourCompleted() {
        prefs.edit { putBoolean(KEY_TOUR_COMPLETED, true) }
    }

    fun resetTour() {
        prefs.edit { putBoolean(KEY_TOUR_COMPLETED, false) }
    }

    companion object {
        private const val PREF_NAME = "bare_budget_tour"
        private const val KEY_TOUR_COMPLETED = "tour_completed"

        @Volatile
        private var instance: TourPreferences? = null

        /** Same pattern as ThemePreferences.getInstance: shared instance for Compose callers outside DI. */
        fun getInstance(context: Context): TourPreferences =
            instance ?: synchronized(this) {
                instance ?: TourPreferences(context.applicationContext).also { instance = it }
            }
    }
}
