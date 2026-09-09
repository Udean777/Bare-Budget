package com.ssajudn.bareuang.ui.navigation
import androidx.compose.material.icons.filled.Settings

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object AddTransaction : Screen("add_transaction")
    data object AllTransactions : Screen("all_transactions")
    data object Analytics : Screen("analytics")
    data object Settings : Screen("settings")
    data object DueBills : Screen("due_bills")
    data object Transfer : Screen("transfer")
    data object Goals : Screen("goals")
    data object Budget : Screen("budget")
    data object Wallets : Screen("wallets")
    data object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
    data object ImportMutasi : Screen("import_mutasi")
    data object OcrScan : Screen("ocr_scan")
}
