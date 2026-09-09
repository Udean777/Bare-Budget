package com.ssajudn.bareuang.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssajudn.bareuang.ui.components.AppNavigationBar

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showNavigationBar = currentRoute in TopLevelRoutes && currentRoute != Screen.Transfer.route
    val destinations = rememberTopLevelDestinations()

    Box(Modifier.fillMaxSize()) {
        TourHost(currentRoute, navController) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(showNavigationBar, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                    AppNavigationBar(
                        items = destinations,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (currentRoute != route) navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                AppFabHost(showNavigationBar && currentRoute != Screen.Transfer.route, currentRoute, navController)
            },
        ) { innerPadding -> AppNavGraph(navController, innerPadding) }
        }
    }
}
