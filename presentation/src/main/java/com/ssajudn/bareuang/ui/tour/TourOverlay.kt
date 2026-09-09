package com.ssajudn.bareuang.ui.tour

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.presentation.R
import androidx.compose.runtime.remember

/** Bounds (root coordinates) of all registered tour anchors on the current screen. */
class TourRegistry {
    val anchors = mutableStateMapOf<String, Rect>()
    fun clear() = anchors.clear()
}

val LocalTourRegistry = compositionLocalOf<TourRegistry?> { null }

/** Registers this node's bounds as a tour spotlight target. No-op when no tour is wired. */
fun Modifier.tourAnchor(key: String): Modifier = composed {
    val registry = LocalTourRegistry.current
    if (registry == null) this
    else this.onGloballyPositioned { coords ->
        registry.anchors[key] = coords.boundsInRoot()
    }
}

/**
 * Full-screen scrim with a rounded cutout over the current step's anchor,
 * plus a tooltip card. Renders nothing while waiting for the anchor to
 * appear (e.g. right after navigating to the step's screen).
 */
@Composable
fun TourOverlay(
    step: TourStep?,
    anchorRect: Rect?,
    stepIndex: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Renders nothing while waiting for the anchor to register (e.g. right after navigating).
    if (step == null || anchorRect == null || anchorRect.width <= 0f) return

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val padPx = with(density) { 14.dp.toPx() }
        val cutout = Rect(
            anchorRect.left - padPx,
            anchorRect.top - padPx,
            anchorRect.right + padPx,
            anchorRect.bottom + padPx
        )

        val scrim = MaterialTheme.colorScheme.scrim
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .pointerInput(Unit) { detectTapGestures { } }
        ) {
            drawRect(scrim.copy(alpha = 0.72f))
            drawRoundRect(
                color = androidx.compose.ui.graphics.Color.Transparent,
                topLeft = cutout.topLeft,
                size = cutout.size,
                cornerRadius = CornerRadius(28f, 28f),
                blendMode = BlendMode.Clear
            )
        }

        val tooltipBelow = cutout.bottom + with(density) { 220.dp.toPx() } < constraints.maxHeight
        val isLast = stepIndex >= totalSteps - 1

        Card(
            modifier = Modifier
                .align(if (tooltipBelow) Alignment.TopStart else Alignment.BottomStart)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .then(
                    if (tooltipBelow) {
                        Modifier.padding(top = with(density) { cutout.bottom.toDp() } + 12.dp)
                    } else {
                        Modifier.padding(bottom = with(density) { (constraints.maxHeight - cutout.top).toDp() } + 12.dp)
                    }
                ),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "${stepIndex + 1}/$totalSteps · ${stringResource(step.titleRes)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(step.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(totalSteps) { i ->
                            val dotColor =
                                if (i == stepIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .background(dotColor, CircleShape)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    AppTextButton(onClick = onSkip) { Text(stringResource(R.string.tour_skip)) }
                    Spacer(Modifier.width(4.dp))
                    AppButton(onClick = onNext) {
                        Text(stringResource(if (isLast) R.string.tour_finish else R.string.tour_next))
                    }
                }
            }
        }
    }
}
