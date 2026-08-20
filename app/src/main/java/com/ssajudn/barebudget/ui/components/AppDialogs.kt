package com.ssajudn.barebudget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssajudn.barebudget.ui.theme.Spacing

/**
 * A form dialog: title, optional icon, arbitrary content, confirm/dismiss.
 *
 * Thin wrapper over M3 [AlertDialog] — it only supplies house defaults for
 * spacing and button labels.
 *
 * @param confirmButtonContainerColor container colour for the confirm button.
 *   Use [MaterialTheme.colorScheme.error] for destructive actions.
 * @param contentSpacing vertical gap between children of [content]. Callers
 *   should rely on this rather than inserting their own `Spacer`s; doing both is
 *   what previously produced ~20dp gaps where 10dp was intended.
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
    contentSpacing: androidx.compose.ui.unit.Dp = Spacing.MediumSmall,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon?.let {
            {
                Icon(imageVector = it, contentDescription = null, tint = iconTint)
            }
        },
        title = { Text(text = title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
                content = content,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                // The previous version accepted a confirmButtonColor parameter and
                // then never passed it to the Button, so a destructive confirm
                // ("Tarik Dana") silently rendered in primary instead of error.
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmButtonContainerColor,
                    contentColor = confirmButtonContentColor,
                ),
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = onDismissRequest) { Text(it) }
            }
        },
    )
}

/**
 * Confirmation dialog for a destructive or otherwise irreversible action.
 *
 * [confirmButtonText] is a parameter, not a constant. It used to be hardcoded to
 * "Hapus" while title and message were overridable, so the Settings sign-out
 * dialog asked the user to confirm by pressing "Hapus" (Delete).
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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = containerColor,
            )
        },
        title = { Text(text = title) },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColorForContainer(containerColor),
                ),
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(dismissButtonText) }
        },
    )
}

/**
 * Picks the matching `on*` role for a container colour, so callers get readable
 * text without having to pass both halves of the pair.
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
