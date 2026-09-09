package com.ssajudn.bareuang.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.ui.tour.tourAnchor
 
data class NavigationBarItemData(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val tourAnchorKey: String? = null,
)

/**
 * Bareuang Floating Pill Bottom Navigation — Modern Bubbly Minimalism.
 * - Lebih gemuk: height 72dp, icon 24dp, pill lebih lebar
 * - Tombol Transfer menonjol ke atas (raised) dengan style Primary Action per DESIGN.MD:
 *   Honey Yellow (#F4A216) + 3D bottom border + Bear Brown text
 */
@Composable
fun AppNavigationBar(
    items: List<NavigationBarItemData>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val transferItem = items.find { it.route == "transfer" }
    val otherItems = items.filter { it.route != "transfer" }
    // Split others to left/right of center for balanced layout
    val leftItems = otherItems.take(2)
    val rightItems = otherItems.drop(2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 10.dp,
                    shape = AppShapes.Pill,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                .crispBorder(
                    shape = AppShapes.Pill,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
            shape = AppShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.97f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side
                leftItems.forEach { item ->
                    val selected = currentRoute == item.route
                    val navInteractionSource = remember { MutableInteractionSource() }
                    NavPill(item, selected, navInteractionSource, onNavigate)
                }
                // Spacer for raised center button — keeps bar gemuk & balanced
                Box(modifier = Modifier.weight(1f))
                // Right side
                rightItems.forEach { item ->
                    val selected = currentRoute == item.route
                    val navInteractionSource = remember { MutableInteractionSource() }
                    NavPill(item, selected, navInteractionSource, onNavigate)
                }
            }
        }
        // Raised Transfer — overlayed above bar so it is not clipped by Pill shape
        if (transferItem != null) {
            val selected = currentRoute == transferItem.route
            val navInteractionSource = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-14).dp)
                    .pressScale(navInteractionSource, pressedScale = 0.90f)
                    .clickable(
                        interactionSource = navInteractionSource,
                        indication = null
                    ) { onNavigate(transferItem.route) }
                    .then(
                        if (transferItem.tourAnchorKey != null) Modifier.tourAnchor(transferItem.tourAnchorKey)
                        else Modifier
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = if (selected) 10.dp else 8.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                        )
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(
                            width = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transferItem.icon,
                        contentDescription = transferItem.label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = transferItem.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavPill(
    item: NavigationBarItemData,
    selected: Boolean,
    navInteractionSource: MutableInteractionSource,
    onNavigate: (String) -> Unit,
) {
                    val animatedContainerColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "navItemBg"
                    )
                     val animatedContentColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                        label = "navItemContent"
                    )
                    val popScale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "navPop"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (item.tourAnchorKey != null) Modifier.tourAnchor(item.tourAnchorKey)
                                else Modifier
                            )
                            .pressScale(navInteractionSource, pressedScale = 0.88f)
                            .clip(AppShapes.Pill)
                            .clickable(
                                interactionSource = navInteractionSource,
                                indication = null
                            ) { onNavigate(item.route) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { scaleX = popScale; scaleY = popScale }
                                    .clip(AppShapes.Pill)
                                    .background(animatedContainerColor)
                                    .then(
                                        if (selected) Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            AppShapes.Pill
                                        ) else Modifier
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon ?: item.icon else item.icon,
                                    contentDescription = null,
                                    tint = animatedContentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
