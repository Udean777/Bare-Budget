package com.ssajudn.barebudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.barebudget.data.local.ThemePreferences
import com.ssajudn.barebudget.ui.navigation.AppNavigation
import com.ssajudn.barebudget.ui.theme.BareBudgetTheme
import com.ssajudn.barebudget.ui.theme.ThemeDarkMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePrefs = ThemePreferences.getInstance(applicationContext)
            val colorMode by themePrefs.colorMode.collectAsStateWithLifecycle()
            val darkMode by themePrefs.darkMode.collectAsStateWithLifecycle()

            val darkTheme = when (darkMode) {
                ThemeDarkMode.FollowSystem -> isSystemInDarkTheme()
                ThemeDarkMode.Light -> false
                ThemeDarkMode.Dark -> true
            }

            // Status/navigation bar icon contrast lives here, in the Activity,
            // because it needs the Window. Setting statusBarColor is deliberately
            // avoided — it is deprecated and a no-op from API 35; the bars are
            // transparent and Compose draws behind them, so only the icon
            // appearance needs to follow the theme.
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
            }

            BareBudgetTheme(
                colorMode = colorMode,
                darkMode = darkMode,
            ) {
                AppNavigation()
            }
        }
    }
}
