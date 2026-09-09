package com.ssajudn.bareuang.ui.bills
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReceiptLong

import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.Wallet
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
import com.ssajudn.bareuang.ui.common.DateFormatter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.pressScale
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.domain.model.RecurringInterval
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.utils.CurrencyVisualTransformation
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.ui.components.AmountTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueBillsScreen(
    onNavigateBack: (() -> Unit)? = null,
    autoOpenAddBill: Boolean = false,
    viewModel: DueBillsViewModel = hiltViewModel()
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

    // Auto-refresh data dompet & tagihan setiap kali pengguna kembali ke layar ini
    LifecycleResumeEffect(Unit) {
        viewModel.loadDueBills()
        viewModel.loadWallets()
        onPauseOrDispose { }
    }
    val selectedStatusFilter by viewModel.selectedStatus.collectAsStateWithLifecycle()

    var showFormDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<DueBill?>(null) }
    var actionSheetBill by remember { mutableStateOf<DueBill?>(null) }

    LaunchedEffect(autoOpenAddBill) {
        if (autoOpenAddBill) {
            editingBill = null
            showFormDialog = true
        }
    }

    var payingBill by remember { mutableStateOf<DueBill?>(null) }
    var unpayingBillConfirm by remember { mutableStateOf<DueBill?>(null) }
    var deletingBillConfirm by remember { mutableStateOf<DueBill?>(null) }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.bills_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        AppIconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Single-Line Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.bills_search_hint),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.common_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close))
                            }
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    shape = AppShapes.Squircle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .crispBorder(
                            shape = AppShapes.Squircle,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // 2. FILTER TABS (Belum Lunas / Lunas) — bertindak sebagai halaman terpisah
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        Pair(DueBillStatus.UNPAID, stringResource(R.string.bills_filter_unpaid)),
                        Pair(DueBillStatus.PAID, stringResource(R.string.bills_filter_paid))
                    )
                    filters.forEachIndexed { index, (status, label) ->
                        SegmentedButton(
                            selected = selectedStatusFilter == status,
                            enabled = !isOperationLoading,
                            onClick = { viewModel.setFilterStatus(status) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedStatusFilter == status) FontWeight.Bold else FontWeight.Medium))
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadDueBills(isPullToRefresh = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is DueBillsUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is DueBillsUiState.Error -> {
                        ErrorState(
                            title = stringResource(R.string.bills_load_error),
                            message = state.message,
                            retryLabel = stringResource(R.string.common_retry),
                            modifier = Modifier.align(Alignment.Center),
                            onRetry = { viewModel.loadDueBills() }
                        )
                    }
                    is DueBillsUiState.Success -> {
                        if (state.bills.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.bills_empty_title),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.bills_empty_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = Spacing.ScreenHorizontal,
                                    end = Spacing.ScreenHorizontal,
                                    top = Spacing.Small,
                                    bottom = Spacing.FabClearance
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.bills, key = { it.id ?: it.hashCode() }) { bill ->
                                    DueBillItem(
                                        bill = bill,
                                        onClick = { actionSheetBill = bill },
                                        onToggleStatus = {
                                            if (bill.status == DueBillStatus.UNPAID) {
                                                payingBill = bill
                                            } else {
                                                viewModel.toggleBillStatus(bill)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. QUICK ACTION BOTTOM SHEET
    if (actionSheetBill != null) {
        val targetBill = actionSheetBill!!
        ModalBottomSheet(
            onDismissRequest = { actionSheetBill = null },
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
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetBill.providerName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatRupiah(targetBill.totalAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = stringResource(R.string.bills_due_prefix, DateFormatter.formatDisplayDate(targetBill.dueDate)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.bills_quick_action),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Actions Group
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (targetBill.status == DueBillStatus.UNPAID) {
                        Surface(
                            onClick = {
                                val b = targetBill
                                actionSheetBill = null
                                payingBill = b
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
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Column {
                                    Text(stringResource(R.string.bills_pay_now), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(stringResource(R.string.bills_pay_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                            }
                        }
                    } else {
                        Surface(
                            onClick = {
                                val b = targetBill
                                actionSheetBill = null
                                unpayingBillConfirm = b
                            },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Column {
                                    Text(stringResource(R.string.bills_refund), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text(stringResource(R.string.bills_refund_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val b = targetBill
                            actionSheetBill = null
                            editingBill = b
                            showFormDialog = true
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
                            Text(stringResource(R.string.bills_edit), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Surface(
                        onClick = {
                            val b = targetBill
                            actionSheetBill = null
                            deletingBillConfirm = b
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(stringResource(R.string.bills_delete), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { actionSheetBill = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.bills_close))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 3. ADD / EDIT FORM DIALOG REUSABLE
    if (showFormDialog) {
        DueBillFormDialog(
            initialBill = editingBill,
            onDismiss = {
                showFormDialog = false
                editingBill = null
            },
            onConfirm = { provider, iconUrl, amount, dueDate, isRecurring, interval, notes ->
                if (editingBill != null && editingBill!!.id != null) {
                    viewModel.updateDueBill(editingBill!!.id!!, provider, iconUrl, amount, dueDate, isRecurring, interval, notes)
                } else {
                    viewModel.addDueBill(provider, iconUrl, amount, dueDate, isRecurring, interval, notes)
                }
                showFormDialog = false
                editingBill = null
            }
        )
    }

    // 4. PAY BILL DIALOG
    if (payingBill != null) {
        val billToPay = payingBill!!
        PayDueBillDialog(
            bill = billToPay,
            wallets = wallets,
            onDismiss = { payingBill = null },
            onConfirm = { walletId ->
                viewModel.payBill(billToPay, walletId)
                payingBill = null
            }
        )
    }

    // 5. UNPAID REFUND CONFIRMATION DIALOG
    if (unpayingBillConfirm != null) {
        val billToUnpay = unpayingBillConfirm!!
        com.ssajudn.bareuang.ui.components.AppConfirmDialog(
            title = stringResource(R.string.bills_cancel_title),
            message = stringResource(R.string.bills_cancel_message, billToUnpay.providerName),
            confirmButtonText = stringResource(R.string.bills_cancel_confirm),
            onConfirm = {
                viewModel.markBillAsUnpaid(billToUnpay)
                unpayingBillConfirm = null
            },
            onDismissRequest = { unpayingBillConfirm = null }
        )
    }

    // 6. DELETE CONFIRMATION DIALOG
    if (deletingBillConfirm != null) {
        val billToDelete = deletingBillConfirm!!
        com.ssajudn.bareuang.ui.components.AppConfirmDialog(
            title = stringResource(R.string.bills_delete_title),
            message = stringResource(R.string.bills_delete_message, billToDelete.providerName, CurrencyFormatter.formatRupiah(billToDelete.totalAmount)),
            confirmButtonText = stringResource(com.ssajudn.bareuang.presentation.R.string.common_delete),
            onConfirm = {
                if (billToDelete.id != null) {
                    viewModel.deleteBill(billToDelete.id!!)
                }
                deletingBillConfirm = null
            },
            onDismissRequest = { deletingBillConfirm = null }
        )
    }
}
