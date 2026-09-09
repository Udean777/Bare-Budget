package com.ssajudn.bareuang.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SpeedDialItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val enabled: Boolean = true,
    val disabledLabel: String? = null,
)

@Composable
fun AppSpeedDialFab(
    items: List<SpeedDialItem>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
        // Sub-items — fan-out arc with per-item spring delay + rotate
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEachIndexed { index, item ->
                val delay = index * 55
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = delay)) +
                            scaleIn(initialScale = 0.4f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) +
                            slideInVertically(initialOffsetY = { it / 2 + (2 - index) * 12 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 100)) + scaleOut(targetScale = 0.4f, animationSpec = tween(durationMillis = 100)) + slideOutVertically(targetOffsetY = { it / 3 }, animationSpec = tween(durationMillis = 100))
                ) {
                    val subInteraction = remember { MutableInteractionSource() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.then(
                            if (item.enabled) Modifier.clickable(interactionSource = subInteraction, indication = null) { onExpandedChange(false); item.onClick() }
                            else Modifier
                        )
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            tonalElevation = 3.dp, shadowElevation = 2.dp,
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                item.label + if (!item.enabled) " • ${item.disabledLabel ?: "offline"}" else "",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (item.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = { if (item.enabled) { onExpandedChange(false); item.onClick() } },
                            interactionSource = subInteraction,
                            containerColor = if (item.enabled) (item.containerColor ?: MaterialTheme.colorScheme.surfaceContainerLowest) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (item.enabled) (item.contentColor ?: MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            shape = CircleShape,
                            modifier = Modifier.pressScale(subInteraction, pressedScale = if (item.enabled) 0.88f else 1f)
                        ) {
                            Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Main FAB
        val mainInteraction = remember { MutableInteractionSource() }
        FloatingActionButton(
            onClick = { onExpandedChange(!isExpanded) },
            interactionSource = mainInteraction,
            shape = CircleShape,
            containerColor = if (isExpanded) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary,
            contentColor = if (isExpanded) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .pressScale(mainInteraction, pressedScale = 0.90f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) stringResource(R.string.fab_close) else stringResource(R.string.fab_menu),
                modifier = Modifier
                    .size(26.dp)
                    .rotate(rotation)
            )
        }
    }
}
