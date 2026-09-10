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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar
import com.ssajudn.bareuang.domain.model.RecurringInterval
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.utils.CurrencyVisualTransformation
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.ui.components.AmountTextField

@Composable
internal fun DueBillsContent(
    paddingValues: PaddingValues,
    uiState: DueBillsUiState,
    searchQuery: String,
    selectedStatusFilter: DueBillStatus,
    isRefreshing: Boolean,
    isOperationLoading: Boolean,
    viewModel: DueBillsViewModel,
    onActionSheetBill: (DueBill) -> Unit,
    onPayingBill: (DueBill) -> Unit
) {
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
                                    onClick = { onActionSheetBill(bill) },
                                    onToggleStatus = {
                                        if (bill.status == DueBillStatus.UNPAID) {
                                            onPayingBill(bill)
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
