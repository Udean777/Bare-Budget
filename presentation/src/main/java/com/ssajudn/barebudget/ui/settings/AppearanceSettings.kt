package com.ssajudn.barebudget.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.ui.graphics.Color
import com.ssajudn.barebudget.ui.theme.Spacing
import com.ssajudn.barebudget.domain.model.AppThemeColorMode
import com.ssajudn.barebudget.domain.model.AppThemeDarkMode

/**
 * Appearance controls: light/dark preference and dynamic vs brand colour.
 *
 * These settings previously had no UI at all — `BareBudgetTheme` accepted the
 * parameters but `dynamicColor` was hardcoded false, so Material You was
 * unreachable dead code.
 */
@Composable
fun AppearanceSettingsGroup(
    colorMode: AppThemeColorMode,
    darkMode: AppThemeDarkMode,
    onColorModeChange: (AppThemeColorMode) -> Unit,
    onDarkModeChange: (AppThemeDarkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Material You needs the platform palette, which only exists on Android 12+.
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Tampilan",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                start = Spacing.ScreenHorizontal,
                bottom = Spacing.Small,
            ),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.Small),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small),
                ) {
                    Text(
                        text = "Mode gelap",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // SingleChoiceSegmentedButtonRow rather than three radio rows:
                    // the options are mutually exclusive and short, so this reads
                    // in one glance and takes one row instead of three.
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        DarkModeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = darkMode == option.mode,
                                onClick = { onDarkModeChange(option.mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = DarkModeOptions.size,
                                ),
                                label = {
                                    Text(
                                        text = option.label,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }
                }

                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                        )
                    },
                    headlineContent = { Text("Warna dinamis") },
                    supportingContent = {
                        Text(
                            if (dynamicColorSupported) {
                                "Ikuti palet wallpaper (Material You)"
                            } else {
                                "Perlu Android 12 atau lebih baru"
                            },
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = colorMode == AppThemeColorMode.System && dynamicColorSupported,
                            onCheckedChange = { useDynamic ->
                                onColorModeChange(
                                    if (useDynamic) AppThemeColorMode.System else AppThemeColorMode.Brand,
                                )
                            },
                            enabled = dynamicColorSupported,
                        )
                    },
                    modifier = Modifier.toggleableItem(
                        enabled = dynamicColorSupported,
                        checked = colorMode == AppThemeColorMode.System && dynamicColorSupported,
                        onCheckedChange = { useDynamic ->
                            onColorModeChange(
                                if (useDynamic) AppThemeColorMode.System else AppThemeColorMode.Brand,
                            )
                        },
                    ),
                )
            }
        }
    }
}

/**
 * Makes the whole row toggle the switch, as one accessibility node.
 *
 * Without merged semantics a screen reader sees the label and the switch as two
 * unrelated targets and announces the toggle without saying what it controls.
 */
private fun Modifier.toggleableItem(
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): Modifier = this.toggleable(
    value = checked,
    enabled = enabled,
    role = Role.Switch,
    onValueChange = onCheckedChange,
)

private data class DarkModeOption(val mode: AppThemeDarkMode, val label: String)

private val DarkModeOptions = listOf(
    DarkModeOption(AppThemeDarkMode.FollowSystem, "Sistem"),
    DarkModeOption(AppThemeDarkMode.Light, "Terang"),
    DarkModeOption(AppThemeDarkMode.Dark, "Gelap"),
)
