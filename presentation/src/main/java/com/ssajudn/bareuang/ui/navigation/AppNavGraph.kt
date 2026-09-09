package com.ssajudn.bareuang.ui.navigation
import androidx.compose.material.icons.filled.Settings

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ssajudn.bareuang.ui.analytics.AnalyticsScreen
import com.ssajudn.bareuang.ui.bills.DueBillsScreen
import com.ssajudn.bareuang.ui.budget.BudgetScreen
import com.ssajudn.bareuang.ui.dashboard.DashboardScreen
import com.ssajudn.bareuang.ui.goals.GoalsScreen
import com.ssajudn.bareuang.ui.imports.ImportMutasiScreen
import com.ssajudn.bareuang.ui.ocr.OcrScanScreen
import com.ssajudn.bareuang.ui.onboarding.OnboardingScreen
import com.ssajudn.bareuang.ui.settings.SettingsScreen
import com.ssajudn.bareuang.ui.splash.SplashScreen
import com.ssajudn.bareuang.ui.transaction.AddTransactionScreen
import com.ssajudn.bareuang.ui.transaction.AllTransactionsScreen
import com.ssajudn.bareuang.ui.transaction.TransactionDetailScreen
import com.ssajudn.bareuang.ui.transaction.TransferScreen
import com.ssajudn.bareuang.ui.wallets.WalletsScreen

@Composable
fun AppNavGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(tween(350)) },
        exitTransition = { fadeOut(tween(350)) },
        popEnterTransition = { fadeIn(tween(350)) },
        popExitTransition = { fadeOut(tween(350)) },
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding),
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashFinished = { destination ->
                navController.navigate(
                    destination
                ) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinishOnboarding = {
                navController.navigate(
                    Screen.Dashboard.route
                ) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddManual = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToWallets = {
                    navController.navigate(Screen.Wallets.route) {
                        popUpTo(
                            Screen.Dashboard.route
                        ) { saveState = true }; launchSingleTop = true; restoreState = true
                    }
                },
                onNavigateToDueBills = {
                    navController.navigate(Screen.DueBills.route) {
                        popUpTo(
                            Screen.Dashboard.route
                        ) { saveState = true }; launchSingleTop = true; restoreState = true
                    }
                },
                onNavigateToGoals = {
                    navController.navigate(Screen.Goals.route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }; launchSingleTop = true; restoreState = true
                    }
                },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                onNavigateToTransactionDetail = {
                    navController.navigate(
                        Screen.TransactionDetail.createRoute(
                            it
                        )
                    )
                },
                onNavigateToAllTransactions = { navController.navigate(Screen.AllTransactions.route) },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route) {
                        popUpTo(
                            Screen.Dashboard.route
                        ) { saveState = true }; launchSingleTop = true; restoreState = true
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onReplayTour = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToImport = { navController.navigate(Screen.ImportMutasi.route) },
                onNavigateToOcr = { navController.navigate(Screen.OcrScan.route) },
                onLocalDataReset = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(Screen.Analytics.route) { AnalyticsScreen() }
        composable(Screen.AllTransactions.route) {
            AllTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransactionDetail = {
                    navController.navigate(
                        Screen.TransactionDetail.createRoute(it)
                    )
                })
        }
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) })
        }
        composable(Screen.DueBills.route) { DueBillsScreen(onNavigateBack = null) }
        composable(Screen.Transfer.route) {
            TransferScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = false
                        }; launchSingleTop = true
                    }
                },
                onTransferSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(
                            Screen.Dashboard.route
                        ) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Wallets.route) { WalletsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ImportMutasi.route) { ImportMutasiScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.OcrScan.route) { OcrScanScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Goals.route) { GoalsScreen() }
        composable(Screen.Budget.route) { BudgetScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            Screen.TransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { entry ->
            TransactionDetailScreen(
                transactionId = entry.arguments?.getString("transactionId").orEmpty(),
                onNavigateBack = { navController.popBackStack() })
        }
    }
}
