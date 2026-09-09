package com.ssajudn.bareuang.ui.navigation
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.NavigationBarItemData

val TopLevelRoutes = setOf(
    Screen.Dashboard.route,
    Screen.DueBills.route,
    Screen.Transfer.route,
    Screen.Goals.route,
    Screen.Analytics.route,
)

@Composable
fun rememberTopLevelDestinations(): List<NavigationBarItemData> {
    val labels = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_bills),
        stringResource(R.string.nav_transfer),
        stringResource(R.string.nav_goals),
        stringResource(R.string.nav_analytics),
    )
    return remember(labels) {
        listOf(
            NavigationBarItemData(Screen.Dashboard.route, labels[0], Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
            NavigationBarItemData(Screen.DueBills.route, labels[1], Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong, "nav_bills"),
            NavigationBarItemData(Screen.Transfer.route, labels[2], Icons.Default.SwapHoriz, Icons.Default.SwapHoriz),
            NavigationBarItemData(Screen.Goals.route, labels[3], Icons.Outlined.Payments, Icons.Filled.Payments),
            NavigationBarItemData(Screen.Analytics.route, labels[4], Icons.AutoMirrored.Outlined.TrendingUp, Icons.AutoMirrored.Filled.TrendingUp, "nav_analytics"),
        )
    }
}
