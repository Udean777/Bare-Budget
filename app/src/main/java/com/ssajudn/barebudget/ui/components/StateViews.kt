package com.ssajudn.barebudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssajudn.barebudget.ui.theme.Spacing

/**
 * Shared loading / empty / error views.
 *
 * These replace four different empty-state designs, four near-identical error
 * blocks and six bare centred spinners. Consistency here is a UX property, not
 * just a code-duplication one: a user should not have to re-learn what "nothing
 * here yet" looks like on each screen.
 */

/** Centred progress indicator, for a first load with nothing to show yet. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Empty state: icon, title, explanation, and an optional call to action.
 *
 * [actionLabel] and [onAction] must be supplied together. Prefer giving an
 * action — an empty screen that only says "no data" leaves the user with no
 * obvious next step.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StateLayout(
        modifier = modifier,
        icon = icon,
        iconContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        title = title,
        description = description,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

/**
 * Error state with a retry affordance.
 *
 * [onRetry] is required, not optional: an error the user cannot act on is a
 * dead end.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Terjadi kesalahan",
    retryLabel: String = "Coba lagi",
    icon: ImageVector = Icons.Default.CloudOff,
) {
    StateLayout(
        modifier = modifier,
        icon = icon,
        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
        iconTint = MaterialTheme.colorScheme.onErrorContainer,
        title = title,
        description = message,
        actionLabel = retryLabel,
        onAction = onRetry,
    )
}

@Composable
private fun StateLayout(
    icon: ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(IconContainerSize)
                .clip(CircleShape)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                // Decorative: the title and description below carry the meaning.
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(IconSize),
            )
        }

        Spacer(Modifier.height(Spacing.Medium))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.Small))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.Large))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

private val IconContainerSize = 72.dp
private val IconSize = 36.dp
