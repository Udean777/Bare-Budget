package com.ssajudn.bareuang.ui.goals
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.ui.components.AmountTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.PriceDisplayStyle
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.utils.CurrencyVisualTransformation
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.ui.components.pressScale
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar
import com.ssajudn.bareuang.ui.components.AppOutlinedButton
import com.ssajudn.bareuang.domain.model.Wallet

enum class GoalFilter(val labelRes: Int) {
    ALL(R.string.goals_filter_all),
    ACTIVE(R.string.goals_filter_active),
    COMPLETED(R.string.goals_filter_completed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: (() -> Unit)? = null,
    autoOpenAddGoal: Boolean = false,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> {}
            }
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()

    // Auto-refresh data dompet & target tabungan setiap kali pengguna kembali ke layar ini
    LifecycleResumeEffect(Unit) {
        viewModel.loadGoals()
        viewModel.loadWallets()
        onPauseOrDispose { }
    }

    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var actionSheetGoal by remember { mutableStateOf<Goal?>(null) }

    LaunchedEffect(autoOpenAddGoal) {
        if (autoOpenAddGoal) {
            editingGoal = null
            showAddDialog = true
        }
    }

    var depositGoalTarget by remember { mutableStateOf<Goal?>(null) }
    var initialIsWithdraw by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FeatureTopAppBar(
                titleRes = R.string.goals_title,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        GoalsContent(
            paddingValues = paddingValues,
            uiState = uiState,
            searchQuery = searchQuery,
            selectedFilter = selectedFilter,
            isRefreshing = isRefreshing,
            viewModel = viewModel,
            onActionSheetGoal = { actionSheetGoal = it },
            onAddGoal = {
                editingGoal = null
                showAddDialog = true
            }
        )
    }
    // 2. QUICK ACTION BOTTOM SHEET
    if (actionSheetGoal != null) {
        val targetGoal = actionSheetGoal!!
        ModalBottomSheet(
            onDismissRequest = { actionSheetGoal = null },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Info Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val accentColor = targetGoal.colorHex.toComposeColorOr(MaterialTheme.colorScheme.primary)

                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetGoal.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatRupiah(targetGoal.currentAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = stringResource(R.string.goals_target_prefix, CurrencyFormatter.formatRupiah(targetGoal.targetAmount)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.goals_quick_action),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Actions Group
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            initialIsWithdraw = false
                            depositGoalTarget = g
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Column {
                                Text(stringResource(R.string.goals_deposit_title), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(stringResource(R.string.goals_deposit_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            initialIsWithdraw = true
                            depositGoalTarget = g
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Column {
                                Text(stringResource(R.string.goals_withdraw_title), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(stringResource(R.string.goals_withdraw_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            editingGoal = g
                            showAddDialog = true
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Text(stringResource(R.string.goals_edit), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            goalToDelete = g
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(stringResource(R.string.goals_delete), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                AppOutlinedButton(
                    onClick = { actionSheetGoal = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.goals_close))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 3. ADD / EDIT GOAL DIALOG
    if (showAddDialog) {
        GoalFormDialog(
            initialGoal = editingGoal,
            onDismiss = {
                showAddDialog = false
                editingGoal = null
            },
            onConfirm = { name, targetAmount, targetDate, colorHex, notes ->
                if (editingGoal != null && editingGoal!!.id != null) {
                    viewModel.updateGoal(editingGoal!!.id!!, name, targetAmount, targetDate, colorHex, notes)
                } else {
                    viewModel.addGoal(name, targetAmount, targetDate, colorHex, notes)
                }
                showAddDialog = false
                editingGoal = null
            }
        )
    }

    // 4. DEPOSIT / WITHDRAW DIALOG WITH WALLET SELECTOR
    if (depositGoalTarget != null) {
        val g = depositGoalTarget!!
        DepositGoalDialog(
            goal = g,
            wallets = wallets,
            initialWithdraw = initialIsWithdraw,
            onDismiss = { depositGoalTarget = null },
            onConfirm = { amount, walletId ->
                g.id?.let { goalId ->
                    viewModel.depositToGoal(goalId, amount, walletId)
                }
                depositGoalTarget = null
            }
        )
    }

    // 5. DELETE CONFIRMATION DIALOG
    if (goalToDelete != null) {
        val target = goalToDelete!!
        AppConfirmDialog(
            title = stringResource(R.string.goals_delete_title),
            message = stringResource(R.string.goals_delete_message, target.name),
            confirmButtonText = stringResource(R.string.goals_delete_confirm),
            onDismissRequest = { goalToDelete = null },
            onConfirm = {
                target.id?.let { viewModel.deleteGoal(it) }
                goalToDelete = null
            }
        )
    }
}
