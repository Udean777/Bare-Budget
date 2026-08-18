package com.ssajudn.barebudget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssajudn.barebudget.ui.bills.DueBillsScreen
import com.ssajudn.barebudget.ui.dashboard.DashboardScreen
import com.ssajudn.barebudget.ui.transaction.AddTransactionScreen

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.ui.onboarding.AuthScreen
import com.ssajudn.barebudget.ui.onboarding.OnboardingScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object AllTransactions : Screen("all_transactions")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object DueBills : Screen("due_bills")
    object Budget : Screen("budget")
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val sessionManager = remember { 
        UserSessionManager(context).apply { initSession() } 
    }
    
    // Dynamic entry point:
    // 1. If never onboarded -> Onboarding
    // 2. If logged in (has userId) -> Dashboard
    // 3. If signed out but onboarded -> Auth Screen
    val startDestination = when {
        !sessionManager.isOnboardingCompleted -> Screen.Onboarding.route
        sessionManager.userId.isNotBlank() -> Screen.Dashboard.route
        else -> Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddManual = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToDueBills = {
                    navController.navigate(Screen.DueBills.route)
                },
                onNavigateToBudget = {
                    navController.navigate(Screen.Budget.route)
                },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                },
                onNavigateToAllTransactions = {
                    navController.navigate(Screen.AllTransactions.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            com.ssajudn.barebudget.ui.settings.SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOutSuccess = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Analytics.route) {
            com.ssajudn.barebudget.ui.analytics.AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AllTransactions.route) {
            com.ssajudn.barebudget.ui.transaction.AllTransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DueBills.route) {
            DueBillsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Budget.route) {
            com.ssajudn.barebudget.ui.budget.BudgetScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("transactionId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            com.ssajudn.barebudget.ui.transaction.TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
