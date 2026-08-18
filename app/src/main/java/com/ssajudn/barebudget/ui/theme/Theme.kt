package com.ssajudn.barebudget.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PastelMintDark,
    onPrimary = Color(0xFF0F291E),
    primaryContainer = PastelMintDarkBg,
    onPrimaryContainer = PastelMintDark,
    secondary = PastelBlueDark,
    onSecondary = Color(0xFF0D2538),
    secondaryContainer = Color(0xFF1E2F3D),
    onSecondaryContainer = PastelBlueDark,
    tertiary = PastelLavenderDark,
    error = PastelCoralDark,
    onError = Color(0xFF370B0E),
    errorContainer = PastelCoralDarkBg,
    onErrorContainer = PastelCoralDark,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PastelMintLight,
    onPrimary = Color.White,
    primaryContainer = PastelMintLightBg,
    onPrimaryContainer = Color(0xFF2E6B47),
    secondary = PastelBlueLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF1565C0),
    tertiary = PastelLavenderLight,
    error = PastelCoralLight,
    onError = Color.White,
    errorContainer = PastelCoralLightBg,
    onErrorContainer = Color(0xFFC62828),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

@Composable
fun BareBudgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
