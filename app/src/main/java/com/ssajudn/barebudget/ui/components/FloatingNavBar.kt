package com.ssajudn.barebudget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.ui.navigation.Screen
import com.ssajudn.barebudget.ui.theme.PastelMintLight

enum class NavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Dashboard, Screen.Dashboard.route),
    DUE_BILLS("Bills", Icons.AutoMirrored.Filled.ReceiptLong, Screen.DueBills.route),
    GOALS("Goals", Icons.Default.Payments, Screen.Goals.route),
    ANALYTICS("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Screen.Analytics.route)
}

@Composable
fun FloatingBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 1: Home
                FloatingNavItemView(
                    item = NavItem.HOME,
                    isSelected = currentRoute == NavItem.HOME.route,
                    onClick = { onNavigate(NavItem.HOME.route) }
                )

                // Item 2: Due Bills
                FloatingNavItemView(
                    item = NavItem.DUE_BILLS,
                    isSelected = currentRoute == NavItem.DUE_BILLS.route,
                    onClick = { onNavigate(NavItem.DUE_BILLS.route) }
                )

                // Center Highlighted Add Button (Frictionless Quick Expense Log)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 26.dp)
                        ) { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Item 3: Goals
                FloatingNavItemView(
                    item = NavItem.GOALS,
                    isSelected = currentRoute == NavItem.GOALS.route,
                    onClick = { onNavigate(NavItem.GOALS.route) }
                )

                // Item 4: Analytics
                FloatingNavItemView(
                    item = NavItem.ANALYTICS,
                    isSelected = currentRoute == NavItem.ANALYTICS.route,
                    onClick = { onNavigate(NavItem.ANALYTICS.route) }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItemView(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navIconColor"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        label = "navBgColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = animatedIconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedIconColor
            )
        }
    }
}
