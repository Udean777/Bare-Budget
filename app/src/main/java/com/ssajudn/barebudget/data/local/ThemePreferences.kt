package com.ssajudn.barebudget.data.local

import android.content.Context
import androidx.core.content.edit
import com.ssajudn.barebudget.ui.theme.ThemeColorMode
import com.ssajudn.barebudget.ui.theme.ThemeDarkMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted appearance settings.
 *
 * Deliberately backed by its own SharedPreferences file rather than the session
 * one: [UserSessionManager.clearSession] calls `edit { clear() }`, so storing
 * theme choices alongside the session would silently reset the user's appearance
 * settings every time they signed out.
 *
 * Exposed as [StateFlow]s so the theme recomposes the moment a setting changes,
 * with no activity restart.
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _colorMode = MutableStateFlow(readColorMode())
    val colorMode: StateFlow<ThemeColorMode> = _colorMode.asStateFlow()

    private val _darkMode = MutableStateFlow(readDarkMode())
    val darkMode: StateFlow<ThemeDarkMode> = _darkMode.asStateFlow()

    fun setColorMode(mode: ThemeColorMode) {
        prefs.edit { putString(KEY_COLOR_MODE, mode.name) }
        _colorMode.value = mode
    }

    fun setDarkMode(mode: ThemeDarkMode) {
        prefs.edit { putString(KEY_DARK_MODE, mode.name) }
        _darkMode.value = mode
    }

    private fun readColorMode(): ThemeColorMode =
        prefs.getString(KEY_COLOR_MODE, null)
            ?.let { name -> ThemeColorMode.entries.firstOrNull { it.name == name } }
            ?: ThemeColorMode.System

    private fun readDarkMode(): ThemeDarkMode =
        prefs.getString(KEY_DARK_MODE, null)
            ?.let { name -> ThemeDarkMode.entries.firstOrNull { it.name == name } }
            ?: ThemeDarkMode.FollowSystem

    companion object {
        private const val PREF_NAME = "bare_budget_appearance"
        private const val KEY_COLOR_MODE = "color_mode"
        private const val KEY_DARK_MODE = "dark_mode"

        @Volatile
        private var instance: ThemePreferences? = null

        /**
         * Single shared instance. Retained for Compose callers that cannot
         * easily receive Hilt injection (e.g. a top-level composable reading
         * the theme from `LocalContext`). The Hilt-provided singleton and this
         * accessor return the *same* instance because the module binds the
         * same object. To be removed in Phase 8 once all callers move to DI.
         */
        fun getInstance(context: Context): ThemePreferences =
            instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
    }
}

