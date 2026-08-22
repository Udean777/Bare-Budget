package com.ssajudn.barebudget.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object AppShapes {
    val LargeIncreased = RoundedCornerShape(24.dp)
    val ExtraLargeIncreased = RoundedCornerShape(32.dp)
    
    // Material 3 Expressive Asymmetric & Custom Shapes
    val AsymmetricHero = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 12.dp,
        bottomEnd = 28.dp,
        bottomStart = 12.dp
    )
    val AsymmetricHeroReversed = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 28.dp,
        bottomEnd = 12.dp,
        bottomStart = 28.dp
    )
    val Squircle = RoundedCornerShape(20.dp)
    val Pill = RoundedCornerShape(50)
    val CardTopRounded = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
}

/**
 * Modifier to apply subtle, crisp border around cards and surfaces in both Dark and Light mode.
 * Gives clean separation on OLED/AMOLED screens.
 */
@Composable
fun Modifier.crispBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.8.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
): Modifier = this.border(
    width = borderWidth,
    color = color,
    shape = shape
)
