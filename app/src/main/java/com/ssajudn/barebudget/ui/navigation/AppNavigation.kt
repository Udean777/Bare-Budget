package com.ssajudn.barebudget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssajudn.barebudget.ui.bills.DueBillsScreen
import com.ssajudn.barebudget.ui.dashboard.DashboardScreen
import com.ssajudn.barebudget.ui.transaction.AddTransactionScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
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
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
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
