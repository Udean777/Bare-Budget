package com.ssajudn.bareuang.ui.dashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.model.RunwayStatus
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.FinancialRunwayCard
import com.ssajudn.bareuang.ui.components.RollingNumber
import com.ssajudn.bareuang.ui.components.TransactionItem
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.components.pressScale
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.PriceDisplayStyle
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.utils.CurrencyFormatter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.tour.tourAnchor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddManual: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToDueBills: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    // Auto-refresh dashboard data whenever returning back to this screen
    LifecycleResumeEffect(Unit) {
        viewModel.loadDashboardData()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val squishX by animateFloatAsState(if (isRefreshing) 1.12f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "squishX")
                        val squishY by animateFloatAsState(if (isRefreshing) 0.88f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "squishY")
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(38.dp).clip(MaterialTheme.shapes.small).graphicsLayer { scaleX = squishX; scaleY = squishY }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bareuang",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = stringResource(R.string.dashboard_subtitle),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    var showThemeDialog by remember { mutableStateOf(false) }
                    val currentDarkMode by viewModel.darkMode.collectAsStateWithLifecycle()

                    val themeIcon = when (currentDarkMode) {
                        AppThemeDarkMode.Dark -> Icons.Default.DarkMode
                        AppThemeDarkMode.Light -> Icons.Default.LightMode
                        AppThemeDarkMode.FollowSystem -> Icons.Default.BrightnessAuto
                    }

                    AppIconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = stringResource(R.string.dashboard_theme_desc)
                        )
                    }

                    AppIconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }

                    if (showThemeDialog) {
                        AlertDialog(
                            onDismissRequest = { showThemeDialog = false },
                            title = {
                                Text(
                                    text = stringResource(R.string.dashboard_choose_theme),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val options = listOf(
                                        Triple(AppThemeDarkMode.FollowSystem, stringResource(R.string.dashboard_follow_system), Icons.Default.BrightnessAuto),
                                        Triple(AppThemeDarkMode.Light, stringResource(R.string.dashboard_light), Icons.Default.LightMode),
                                        Triple(AppThemeDarkMode.Dark, stringResource(R.string.dashboard_dark), Icons.Default.DarkMode)
                                    )

                                    options.forEach { (mode, label, icon) ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setDarkMode(mode)
                                                    showThemeDialog = false
                                                },
                                            color = if (currentDarkMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (currentDarkMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (currentDarkMode == mode) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (currentDarkMode == mode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (currentDarkMode == mode) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                AppTextButton(onClick = { showThemeDialog = false }) {
                                    Text(stringResource(R.string.common_close))
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadDashboardData(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DashboardUiState.Error -> {
                    ErrorState(
                        title = stringResource(R.string.dashboard_load_error),
                        message = state.message.asString(),
                        retryLabel = stringResource(R.string.common_retry),
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.loadDashboardData() }
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        summary = state.summary,
                        onSetBudgetClick = onNavigateToBudget,
                        onAddManualClick = onNavigateToAddManual,
                        onWalletsClick = onNavigateToWallets,
                        onDueBillsClick = onNavigateToDueBills,
                        onGoalsClick = onNavigateToGoals,
                        onTransactionClick = onNavigateToTransactionDetail,
                        onSeeAllTransactionsClick = onNavigateToAllTransactions,
                        onAnalyticsClick = onNavigateToAnalytics
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
    onAddManualClick: () -> Unit,
    onWalletsClick: () -> Unit,
    onDueBillsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = Spacing.Small, bottom = Spacing.FabClearance)
    ) {
        // 1. FINANCIAL RUNWAY CARD (Core Feature)
        item {
            FinancialRunwayCard(
                modifier = Modifier.tourAnchor("dashboard_runway"),
                remainingBudget = summary.remainingBudget,
                netWorth = summary.netWorth,
                totalBudget = summary.monthlyBudget,
                estimatedDeathDay = summary.estimatedDeathDay,
                daysInMonth = summary.daysInMonth,
                message = summary.runwayStatus.toUiMessage(),
                onSetBudgetClick = onSetBudgetClick,
            )
        }

        item {
            if (summary.monthlyBudget > 0) {
                com.ssajudn.bareuang.ui.components.DailyPacingCard(
                    dailyAllowance = summary.dailyAllowance,
                    todaySpent = summary.todaySpent,
                    dailyProgress = summary.dailyProgress,
                    remainingToday = summary.remainingToday,
                    remainingDays = summary.remainingDays,
                )
            }
        }

        // 2. QUICK ACTION TILES (M3 Surface Containers)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tourAnchor("dashboard_quick_actions"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_wallet),
                    subtitle = stringResource(R.string.dashboard_quick_wallet_desc),
                    icon = Icons.Default.AccountBalanceWallet,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onWalletsClick
                )

                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_bills),
                    subtitle = if (summary.unpaidDueBillsSum > 0) {
                        CurrencyFormatter.formatCompact(summary.unpaidDueBillsSum)
                    } else stringResource(R.string.bills_badge_paid),
                    icon = Icons.Default.ReceiptLong,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onDueBillsClick
                )

                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_goals),
                    subtitle = stringResource(R.string.dashboard_quick_goals_desc),
                    icon = Icons.Default.Payments,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onGoalsClick
                )
            }
        }

        // 3. MONTHLY SPENDING METRICS
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // rounded-md = 24dp
                color = MaterialTheme.colorScheme.surfaceContainerLowest, // white on cream canvas
                shadowElevation = 2.dp,
                border = BorderStroke(
                    0.8.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_total_spent),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RollingNumber(
                            value = summary.totalSpent,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.dashboard_daily_avg),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RollingNumber(
                            value = summary.averageDailySpend,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }

        // 4. RECURRING SCHEDULES (Hanya jika ada template recurring aktif)
        if (summary.recurringTransactions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_recurring_schedule),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            summary.recurringTransactions.forEachIndexed { idx, tx ->
                item {
                    com.ssajudn.bareuang.ui.components.StaggeredFadeIn(idx) {
                        com.ssajudn.bareuang.ui.components.RecurringTransactionItem(
                            transaction = tx,
                            onClick = { tx.id?.let(onTransactionClick) }
                        )
                    }
                }
            }
        }

        // 5. RECENT TRANSACTIONS HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.dashboard_see_all),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onSeeAllTransactionsClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        val recent = summary.recentTransactions
        if (recent.isNullOrEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_no_tx),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            recent.forEachIndexed { idx, tx ->
                item {
                    com.ssajudn.bareuang.ui.components.StaggeredFadeIn(idx) {
                        TransactionItem(
                            transaction = tx,
                            onClick = { tx.id?.let(onTransactionClick) }
                        )
                    }
                }
            }
        }
    }
}
