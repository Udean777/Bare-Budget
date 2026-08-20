package com.ssajudn.barebudget.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.toHct
import com.ssajudn.barebudget.data.model.TransactionCategory

enum class ThemeColorMode {
    System,
    Brand,
}

enum class ThemeDarkMode {
    FollowSystem,
    Light,
    Dark,
}

val DefaultThemeColor = Color(0xFFED5564)

private fun paletteStyleFor(seedColor: Color): PaletteStyle {
    val chroma = seedColor.toHct().chroma
    return when {
        chroma < 4.0 -> PaletteStyle.Monochrome
        chroma < 12.0 -> PaletteStyle.Neutral
        else -> PaletteStyle.TonalSpot
    }
}

val LocalIsDynamicColor = staticCompositionLocalOf { false }

private val LocalCategoryColors = staticCompositionLocalOf { LightCategoryColors }

val categoryColors: CategoryColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCategoryColors.current

@Composable
fun BareBudgetTheme(
    colorMode: ThemeColorMode = ThemeColorMode.Brand,
    darkMode: ThemeDarkMode = ThemeDarkMode.FollowSystem,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = when (darkMode) {
        ThemeDarkMode.FollowSystem -> isSystemInDarkTheme()
        ThemeDarkMode.Light -> false
        ThemeDarkMode.Dark -> true
    }

    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val useDynamic = colorMode == ThemeColorMode.System && supportsDynamic

    val baseColorScheme = if (useDynamic) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        dynamicColorScheme(
            seedColor = themeColor,
            isDark = darkTheme,
            contrastLevel = 0.0,
            style = paletteStyleFor(themeColor),
        )
    }

    CompositionLocalProvider(
        LocalCategoryColors provides if (darkTheme) DarkCategoryColors else LightCategoryColors,
        LocalIsDynamicColor provides useDynamic,
    ) {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
