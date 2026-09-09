package com.ssajudn.bareuang.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.ssajudn.bareuang.ui.tour.LocalTourRegistry
import com.ssajudn.bareuang.ui.tour.TourOverlay
import com.ssajudn.bareuang.ui.tour.TourRegistry
import com.ssajudn.bareuang.ui.tour.TourScript
import com.ssajudn.bareuang.ui.tour.TourViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.delay

@Composable
fun TourHost(
    currentRoute: String?,
    navController: NavHostController,
    content: @Composable () -> Unit = {},
) {
    val viewModel: TourViewModel = hiltViewModel()
    val registry = remember { TourRegistry() }
    var index by rememberSaveable { mutableIntStateOf(-1) }

    LaunchedEffect(currentRoute) {
        if (index == -1 && currentRoute == Screen.Dashboard.route && !viewModel.isTourCompleted) index = 0
    }
    fun navigateToStep(route: String) {
        if (navController.currentDestination?.route != route) navController.navigate(route) { launchSingleTop = true }
    }
    fun goToStep(nextIndex: Int) {
        index = nextIndex
        TourScript.steps.getOrNull(nextIndex)?.let { step -> if (currentRoute != step.route) navigateToStep(step.route) }
    }
    fun endTour() {
        viewModel.markTourCompleted()
        index = -1
        navController.navigate(Screen.Dashboard.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
    }
    LaunchedEffect(index) {
        TourScript.steps.getOrNull(index)?.let { step -> delay(800); navigateToStep(step.route) }
    }

    val step = TourScript.steps.getOrNull(index)
    val anchor = if (step != null && currentRoute == step.route) registry.anchors[step.anchorKey] else null
    CompositionLocalProvider(LocalTourRegistry provides registry) {
        content()
        TourOverlay(
            step = step,
            anchorRect = anchor,
            stepIndex = index,
            totalSteps = TourScript.steps.size,
            onNext = { if (index >= TourScript.steps.lastIndex) endTour() else goToStep(index + 1) },
            onSkip = ::endTour,
        )
    }
}
