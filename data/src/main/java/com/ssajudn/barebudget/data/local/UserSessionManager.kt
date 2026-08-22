package com.ssajudn.barebudget.data.local

import android.content.Context
import android.content.SharedPreferences
import com.ssajudn.barebudget.domain.AppConfig
import java.util.UUID
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "bare_budget_session"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, value) }

    var isGuestMode: Boolean
        get() = prefs.getBoolean(KEY_IS_GUEST, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_GUEST, value) }

    /**
     * The current user id. Read by [com.ssajudn.barebudget.data.network.AuthInterceptor]
     * on every request, so a login/logout takes effect immediately without
     * the old `ApiClient.authToken` mutable global.
     */
    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit { putString(KEY_USER_ID, value) }

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit { putString(KEY_USER_EMAIL, value) }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_USER_NAME, value) }

    fun startGuestSession() {
        val guestId = AppConfig.GUEST_PREFIX + UUID.randomUUID().toString().take(12)
        isOnboardingCompleted = true
        isGuestMode = true
        userId = guestId
        userEmail = AppConfig.GUEST_EMAIL
        userName = "Guest User"
    }

    fun startUserSession(uid: String, email: String, name: String) {
        isOnboardingCompleted = true
        isGuestMode = false
        userId = uid
        userEmail = email
        userName = name
    }

    fun clearSession(preserveOnboarding: Boolean = true) {
        val onboardingDone = if (preserveOnboarding) isOnboardingCompleted else false
        prefs.edit { clear() }
        if (onboardingDone) {
            isOnboardingCompleted = true
        }
    }
}

