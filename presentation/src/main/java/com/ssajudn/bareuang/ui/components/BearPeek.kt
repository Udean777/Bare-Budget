package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.presentation.R
import androidx.compose.material3.MaterialTheme

@Composable
fun BearPeek(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    wiggle: Boolean = true
) {
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bearPeekOffset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(250),
        label = "bearPeekAlpha"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "bearWiggle")
    val infiniteWiggle by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wiggle"
    )

    Box(modifier = modifier.graphicsLayer { this.alpha = alpha }) {
        Image(
            painter = painterResource(id = R.drawable.ic_bear_head),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .offset(y = offsetY)
                .clip(MaterialTheme.shapes.medium)
                .graphicsLayer { rotationZ = if (visible && wiggle) infiniteWiggle else 0f }
        )
    }
}
