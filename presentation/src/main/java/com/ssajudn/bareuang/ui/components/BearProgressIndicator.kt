package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes

/**
 * Bareuang's signature Bear Progress Indicator (DESIGN.MD §7 Progress Bars & Budget Limits).
 *
 * Features:
 * - Thick, rounded progress track (10dp default).
 * - Animated sliding cute Bear mascot indicator at the tip of progress.
 * - Gentle spring physics animation.
 */
@Composable
fun BearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    trackHeight: Dp = 10.dp,
    bearSize: Dp = 22.dp,
    showBearMascot: Boolean = true,
    indicatorRes: Int = R.drawable.ic_bear_head,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bearProgressAnim"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(bearSize)
    ) {
        val totalWidth = maxWidth

        // 1. Thick rounded track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .clip(AppShapes.Pill)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(trackHeight)
                    .clip(AppShapes.Pill)
                    .background(color)
            )
        }

        // 2. Sliding Bear Mascot Head — wiggle ketika >90%
        if (showBearMascot) {
            val maxOffset = (totalWidth - bearSize).coerceAtLeast(0.dp)
            val bearOffset = maxOffset * animatedProgress
            val shouldWiggle = clampedProgress > 0.9f
            val transition = rememberInfiniteTransition(label = "bearWiggle")
            val wiggle by transition.animateFloat(
                initialValue = -7f, targetValue = 7f,
                animationSpec = infiniteRepeatable(animation = tween(durationMillis = 380, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
                label = "wiggle"
            )

            Image(
                painter = painterResource(id = indicatorRes),
                contentDescription = null,
                modifier = Modifier
                    .size(bearSize)
                    .offset(x = bearOffset)
                    .align(Alignment.CenterStart)
                    .graphicsLayer { rotationZ = if (shouldWiggle) wiggle else 0f }
            )
        }
    }
}
