package com.ssajudn.bareuang.ui.transaction
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.ui.components.WalletDropdown
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.getCategoryIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter

import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.components.bareuangOutlinedTextFieldColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(
                    effect.uiText.asString(
                        context
                    )
                )

                is UiEffect.Navigate -> onNavigateToBudget()
                is UiEffect.PopBackStack -> {}
            }
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSplitBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tx_add_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                AppButton(
                    onClick = { viewModel.saveTransaction() },
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0 && !isOperationLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.common_save),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 0. BUDGET GATE BANNER
            if (uiState.isBudgetMissing && uiState.transactionType != TransactionType.TRANSFER) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tx_budget_not_set),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        AppTextButton(onClick = onNavigateToBudget) {
                            Text(
                                stringResource(R.string.tx_budget_set_action),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 0. TRANSACTION TYPE (Expense / Income / Transfer)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val types = listOf(
                    Triple(TransactionType.EXPENSE, stringResource(R.string.tx_expense), 0),
                    Triple(TransactionType.INCOME, stringResource(R.string.tx_income), 1),
                    Triple(TransactionType.TRANSFER, stringResource(R.string.tx_transfer), 2)
                )
                types.forEach { (type, label, index) ->
                    SegmentedButton(
                        selected = uiState.transactionType == type,
                        onClick = { viewModel.onTransactionTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label)
                    }
                }
            }

            // 0.5. WALLET SELECTION (Single for Income/Expense, Dual for Transfer)
            if (uiState.transactionType == TransactionType.TRANSFER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Source Wallet (Dari)
                    WalletDropdown(
                        wallets = uiState.wallets,
                        selectedWalletId = uiState.selectedWalletId,
                        label = stringResource(R.string.tx_from_wallet),
                        emptyText = stringResource(R.string.common_add),
                        modifier = Modifier.weight(1f),
                        onSelected = { viewModel.onWalletChange(it.id!!) }
                    )

                    // Destination Wallet (Ke)
                    WalletDropdown(
                        wallets = uiState.wallets,
                        selectedWalletId = uiState.selectedToWalletId,
                        label = stringResource(R.string.tx_to_wallet),
                        emptyText = stringResource(R.string.common_add),
                        modifier = Modifier.weight(1f),
                        onSelected = { viewModel.onToWalletChange(it.id!!) }
                    )
                }
            } else {
                WalletDropdown(
                    wallets = uiState.wallets,
                    selectedWalletId = uiState.selectedWalletId,
                    label = stringResource(R.string.tx_wallet_label),
                    onSelected = { viewModel.onWalletChange(it.id!!) }
                )
            }

            // 1. AMOUNT INPUT (Prominent M3 Display Card with Quick Presets)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .crispBorder(
                        shape = AppShapes.Squircle,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                shape = AppShapes.Squircle,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val amountLabel = when (uiState.transactionType) {
                        TransactionType.INCOME -> stringResource(R.string.tx_amount_income)
                        TransactionType.TRANSFER -> stringResource(R.string.tx_amount_transfer)
                        TransactionType.EXPENSE -> stringResource(R.string.tx_amount_expense)
                    }
                    Text(
                        text = amountLabel,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AmountTextField(
                        value = uiState.rawAmount,
                        onValueChange = { viewModel.onAmountChange(it) },
                        placeholder = {
                            Text(
                                stringResource(R.string.common_rp_zero),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        },
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Nominal Presets (+10k, +20k, +50k, +100k)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10_000L, 20_000L, 50_000L, 100_000L).forEach { addNominal ->
                            SuggestionChip(
                                onClick = {
                                    val current = uiState.parsedAmount
                                    viewModel.onAmountChange((current + addNominal).toString())
                                },
                                label = {
                                    Text(
                                        "+${CurrencyFormatter.formatCompact(addNominal)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                shape = AppShapes.Pill,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Split Bill Trigger Button (Hanya jika pengeluaran)
                    if (uiState.transactionType == TransactionType.EXPENSE && uiState.parsedAmount > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        FilledTonalButton(
                            onClick = { showSplitBottomSheet = true },
                            shape = AppShapes.Pill,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.split_title),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // 2. CATEGORY SELECTOR (Hanya jika bukan Transfer)
            if (uiState.transactionType != TransactionType.TRANSFER) {
                Column {
                    Text(
                        text = stringResource(R.string.tx_category),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        val incomeCats = listOf(
                            TransactionCategory.SALARY,
                            TransactionCategory.BONUS,
                            TransactionCategory.INVESTMENT
                        )
                        val filteredCats = TransactionCategory.entries.filter {
                            it != TransactionCategory.TRANSFER && (if (uiState.transactionType == TransactionType.INCOME) it in incomeCats else it !in incomeCats)
                        }
                        items(filteredCats) { category ->
                            val isSelected = category == uiState.selectedCategory
                            val catColors = categoryColors
                            val catBudget = uiState.categoryBudgets.find { it.category == category }

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategoryChange(category) },
                                label = {
                                    val labelText =
                                        if (catBudget != null && catBudget.limitAmount > 0) {
                                            "${stringResource(category.labelRes())} (${
                                                CurrencyFormatter.formatCompact(
                                                    catBudget.remainingAmount
                                                )
                                            })"
                                        } else {
                                            stringResource(category.labelRes())
                                        }
                                    Text(
                                        text = labelText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = AppShapes.Pill,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = catColors.container(category),
                                    selectedLabelColor = catColors.onContainer(category),
                                    selectedLeadingIconColor = catColors.onContainer(category)
                                )
                            )
                        }
                    }
                }
            }

            // SOFT-WARN: category budget overspent hint (informational, never blocks)
            val selectedCatBudget =
                uiState.categoryBudgets.find { it.category == uiState.selectedCategory }
            if (uiState.transactionType == TransactionType.EXPENSE &&
                selectedCatBudget != null && selectedCatBudget.limitAmount > 0 &&
                selectedCatBudget.spentAmount + uiState.parsedAmount > selectedCatBudget.limitAmount
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(
                                R.string.tx_category_overspent_warning,
                                CurrencyFormatter.formatCompact(
                                    (selectedCatBudget.limitAmount - selectedCatBudget.spentAmount).coerceAtLeast(
                                        0L
                                    )
                                ),
                                stringResource(selectedCatBudget.category.labelRes())
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 3. MERCHANT / STORE NAME
            Column {
                Text(
                    text = stringResource(R.string.tx_merchant_label),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = { viewModel.onMerchantChange(it) },
                    placeholder = { Text(stringResource(R.string.tx_merchant_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = bareuangOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3.5. DATE
            var showDatePicker by remember { mutableStateOf(false) }
            if (showDatePicker) {
                AppDatePickerDialog(
                    initialDateMillis = DateUtils.parseIsoToMillis(uiState.date),
                    onDateSelected = { millis ->
                        viewModel.onDateChange(DateUtils.formatMillisToIso(millis))
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.tx_date),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = DateFormatter.formatDisplayDate(uiState.date),
                    onValueChange = { },
                    readOnly = true,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // 4. NOTES
            Column {
                Text(
                    text = stringResource(R.string.tx_notes_label),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    placeholder = { Text(stringResource(R.string.tx_notes_hint)) },
                    shape = MaterialTheme.shapes.medium,
                    colors = bareuangOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4.5. RECURRING TRANSACTION SECTION (Hanya untuk Income & Expense)
            if (uiState.transactionType != TransactionType.TRANSFER) {
                Surface(
                    shape = AppShapes.Squircle,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .crispBorder(
                            shape = AppShapes.Squircle,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.tx_recurring_label),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.tx_recurring_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.isRecurring,
                                onCheckedChange = { viewModel.onRecurringChange(it) }
                            )
                        }

                        if (uiState.isRecurring) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.tx_recurring_interval),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val intervals = listOf(
                                    com.ssajudn.bareuang.domain.model.RecurringInterval.WEEKLY to stringResource(
                                        R.string.tx_recurring_weekly
                                    ),
                                    com.ssajudn.bareuang.domain.model.RecurringInterval.MONTHLY to stringResource(
                                        R.string.tx_recurring_monthly
                                    ),
                                    com.ssajudn.bareuang.domain.model.RecurringInterval.YEARLY to stringResource(
                                        R.string.tx_recurring_yearly
                                    )
                                )
                                intervals.forEach { (interval, label) ->
                                    val isSelected = uiState.recurringInterval == interval
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onRecurringIntervalChange(interval) },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        },
                                        shape = AppShapes.Pill
                                    )
                                }
                            }

                            // Day of week selector when WEEKLY is selected
                            if (uiState.recurringInterval == com.ssajudn.bareuang.domain.model.RecurringInterval.WEEKLY) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.tx_recurring_on_day),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val days = listOf(
                                    1 to stringResource(R.string.day_mon),
                                    2 to stringResource(R.string.day_tue),
                                    3 to stringResource(R.string.day_wed),
                                    4 to stringResource(R.string.day_thu),
                                    5 to stringResource(R.string.day_fri),
                                    6 to stringResource(R.string.day_sat),
                                    7 to stringResource(R.string.day_sun)
                                )
                                val currentDayOfWeek = DateUtils.getDayOfWeek(uiState.date)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    days.forEach { (dayIso, dayName) ->
                                        val isCurrentDay = currentDayOfWeek == dayIso
                                        Surface(
                                            shape = AppShapes.Pill,
                                            color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val nextDate = DateUtils.calculateNextWeeklyDay(
                                                        uiState.date,
                                                        dayIso
                                                    )
                                                    viewModel.onDateChange(nextDate)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isCurrentDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (uiState.recurringInterval == com.ssajudn.bareuang.domain.model.RecurringInterval.MONTHLY) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val dayOfMonth = DateUtils.getDayOfMonth(uiState.date)
                                Text(
                                    text = stringResource(
                                        R.string.tx_recurring_on_date,
                                        dayOfMonth
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            val errorText = uiState.validationError?.let {
                when (it) {
                    AddTransactionError.INSUFFICIENT_BALANCE -> stringResource(
                        it.resId,
                        CurrencyFormatter.formatRupiah(
                            uiState.wallets.find { w -> w.id == uiState.selectedWalletId }?.balance
                                ?: 0L
                        )
                    )

                    else -> stringResource(it.resId)
                }
            } ?: uiState.errorMessage
            if (errorText != null) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showSplitBottomSheet) {
        SplitBillBottomSheet(
            totalBillAmount = uiState.parsedAmount,
            merchantName = uiState.merchant,
            onDismiss = { showSplitBottomSheet = false },
            onApplyMyPortion = { myPortion ->
                viewModel.onAmountChange(myPortion.toString())
                if (uiState.notes.isBlank()) {
                    viewModel.onNotesChange("Split bill (${CurrencyFormatter.formatRupiah(uiState.parsedAmount)})")
                }
                showSplitBottomSheet = false
            }
        )
    }

    // Soft daily-budget nudge: confirm before saving over today's allowance.
    if (uiState.pendingDailyOverride) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyOverride() },
            title = { Text(stringResource(R.string.tx_daily_override_title)) },
            text = {
                Text(
                    uiState.pendingDailyMessage ?: stringResource(R.string.tx_error_daily_exceeded)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDailyOverride() }) {
                    Text(stringResource(R.string.tx_daily_override_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDailyOverride() }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
