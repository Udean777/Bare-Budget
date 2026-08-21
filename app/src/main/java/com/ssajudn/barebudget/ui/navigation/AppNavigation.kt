package com.ssajudn.barebudget.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssajudn.barebudget.ui.analytics.AnalyticsScreen
import com.ssajudn.barebudget.ui.bills.DueBillsScreen
import com.ssajudn.barebudget.ui.budget.BudgetScreen
import com.ssajudn.barebudget.ui.components.AppNavigationBar
import com.ssajudn.barebudget.ui.components.NavigationBarItemData
import com.ssajudn.barebudget.ui.dashboard.DashboardScreen
import com.ssajudn.barebudget.ui.goals.GoalsScreen
import com.ssajudn.barebudget.ui.onboarding.AuthScreen
import com.ssajudn.barebudget.ui.onboarding.OnboardingScreen
import com.ssajudn.barebudget.ui.settings.SettingsScreen
import com.ssajudn.barebudget.ui.splash.SplashScreen
import com.ssajudn.barebudget.ui.transaction.AddTransactionScreen
import com.ssajudn.barebudget.ui.transaction.AllTransactionsScreen
import com.ssajudn.barebudget.ui.transaction.TransactionDetailScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object AllTransactions : Screen("all_transactions")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object DueBills : Screen("due_bills")
    object Transfer : Screen("transfer")
    object Goals : Screen("goals")
    object Budget : Screen("budget")
    object Wallets : Screen("wallets")

    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
}

/**
 * The four destinations reachable from the bottom navigation bar.
 *
 * Declared once at file scope rather than rebuilt inside the Scaffold on every
 * recomposition. Each pairs an outlined icon for the unselected state with a
 * filled one for selected, per M3 — the previous code had a `selectedIcon` field
 * that was never populated, so the distinction was silently dead.
 */
private val TopLevelDestinations = listOf(
    NavigationBarItemData(
        route = Screen.Dashboard.route,
        label = "Beranda",
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Filled.Dashboard,
    ),
    NavigationBarItemData(
        route = Screen.DueBills.route,
        label = "Tagihan",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
    ),
    NavigationBarItemData(
        route = Screen.Transfer.route,
        label = "Transfer",
        icon = Icons.Default.SwapHoriz,
        selectedIcon = Icons.Default.SwapHoriz,
    ),
    NavigationBarItemData(
        route = Screen.Goals.route,
        label = "Target",
        icon = Icons.Outlined.Payments,
        selectedIcon = Icons.Filled.Payments,
    ),
    NavigationBarItemData(
        route = Screen.Analytics.route,
        label = "Analitik",
        icon = Icons.AutoMirrored.Outlined.TrendingUp,
        selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
    ),
)

private val TopLevelRoutes = TopLevelDestinations.map { it.route }.toSet()

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showNavigationBar = currentRoute in TopLevelRoutes

    Scaffold(
        bottomBar = {
            // AnimatedVisibility rather than `if`: the bar slides out instead of
            // disappearing between frames, and keeping it in the composition means
            // the Scaffold's bottom padding animates with it rather than jumping.
            AnimatedVisibility(
                visible = showNavigationBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                AppNavigationBar(
                    items = TopLevelDestinations,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showNavigationBar,
                enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)),
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddTransaction.route) },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat pengeluaran")
                }
            }
        },
    ) { innerPadding ->
        // padding() applies the inset; consumeWindowInsets() then marks it as
        // handled.
        //
        // Both are needed because every screen has its own Scaffold. Without the
        // consume, those inner Scaffolds would add the status- and navigation-bar
        // insets a second time on top of the space already reserved here, pushing
        // each top bar down by the height of the status bar.
        //
        // Previously the bottom inset was discarded whenever the nav bar was
        // visible, so each top-level screen hardcoded a bottom contentPadding to
        // clear it — 88dp on the dashboard, 100dp on three others, for the same
        // bar — and the top inset was never applied on any route.
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(350)) },
            exitTransition = { fadeOut(animationSpec = tween(350)) },
            popEnterTransition = { fadeIn(animationSpec = tween(350)) },
            popExitTransition = { fadeOut(animationSpec = tween(350)) },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

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
                    onNavigateToWallets = {
                        navController.navigate(Screen.Wallets.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },

                    onNavigateToDueBills = {
                        navController.navigate(Screen.DueBills.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGoals = {
                        navController.navigate(Screen.Goals.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                        navController.navigate(Screen.Analytics.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
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
                AnalyticsScreen()
            }

            composable(Screen.AllTransactions.route) {
                AllTransactionsScreen(
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
                DueBillsScreen()
            }

            composable(Screen.Transfer.route) {
                com.ssajudn.barebudget.ui.transaction.TransferScreen(
                    onTransferSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Wallets.route) {
                com.ssajudn.barebudget.ui.wallets.WalletsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            composable(Screen.Goals.route) {
                GoalsScreen()
            }

            composable(Screen.Budget.route) {
                BudgetScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                TransactionDetailScreen(
                    transactionId = transactionId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
