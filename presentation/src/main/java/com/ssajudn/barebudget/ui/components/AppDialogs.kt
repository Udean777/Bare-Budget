package com.ssajudn.barebudget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssajudn.barebudget.ui.theme.Spacing

/**
 * A form dialog: title, optional icon, arbitrary content, confirm/dismiss.
 *
 * Uses a custom [Dialog] to provide larger spacing, a wider form factor,
 * and a more refined Material 3 Expressive look than standard AlertDialogs.
 */
@Composable
fun AppFormDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    confirmButtonText: String = "Simpan",
    dismissButtonText: String? = "Batal",
    confirmButtonContainerColor: Color = MaterialTheme.colorScheme.primary,
    confirmButtonContentColor: Color = contentColorForContainer(confirmButtonContainerColor),
    isConfirmEnabled: Boolean = true,
    contentSpacing: androidx.compose.ui.unit.Dp = Spacing.Medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 640.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = Spacing.Small)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(contentSpacing),
                    content = content
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissButtonText != null) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(end = Spacing.Small)
                        ) {
                            Text(dismissButtonText)
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = isConfirmEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonContainerColor,
                            contentColor = confirmButtonContentColor,
                        )
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for a destructive or otherwise irreversible action.
 */
@Composable
fun AppConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Hapus item ini?",
    message: String = "Tindakan ini tidak bisa dibatalkan.",
    confirmButtonText: String = "Hapus",
    dismissButtonText: String = "Batal",
    icon: ImageVector = Icons.Default.DeleteOutline,
    isDestructive: Boolean = true,
) {
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = containerColor,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = Spacing.Small)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Medium))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = Spacing.Small)
                    ) {
                        Text(dismissButtonText)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColorForContainer(containerColor),
                        )
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}

/**
 * Picks the matching `on*` role for a container colour.
 */
@Composable
private fun contentColorForContainer(container: Color): Color {
    val scheme = MaterialTheme.colorScheme
    return when (container) {
        scheme.primary -> scheme.onPrimary
        scheme.secondary -> scheme.onSecondary
        scheme.tertiary -> scheme.onTertiary
        scheme.error -> scheme.onError
        scheme.primaryContainer -> scheme.onPrimaryContainer
        scheme.secondaryContainer -> scheme.onSecondaryContainer
        scheme.tertiaryContainer -> scheme.onTertiaryContainer
        scheme.errorContainer -> scheme.onErrorContainer
        else -> scheme.onPrimary
    }
}
