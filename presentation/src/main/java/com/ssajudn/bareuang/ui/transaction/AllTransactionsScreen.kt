package com.ssajudn.bareuang.ui.transaction
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.TransactionItem
import com.ssajudn.bareuang.ui.components.getCategoryIcon
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    viewModel: AllTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.loadTransactions()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tx_all_title),
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadTransactions() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar & Filter Action Row
                when (val state = uiState) {
                    is AllTransactionsUiState.Success -> {
                        val activeFilterCount = (if (state.selectedType != null) 1 else 0) +
                                (if (state.selectedCategory != null) 1 else 0) +
                                (if (state.selectedWalletId != null) 1 else 0)

                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row Search + Filter Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = state.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    placeholder = {
                                        Text(
                                            stringResource(R.string.tx_search_hint),
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
                                        if (state.searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = stringResource(R.string.common_close)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    maxLines = 1,
                                    shape = AppShapes.Squircle,
                                    modifier = Modifier
                                        .weight(1f)
                                        .crispBorder(
                                            shape = AppShapes.Squircle,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.35f
                                            )
                                        ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                // Trigger Filter Button with Badge
                                Surface(
                                    onClick = { showFilterBottomSheet = true },
                                    shape = AppShapes.Squircle,
                                    color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .crispBorder(
                                            shape = AppShapes.Squircle,
                                            color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.5f
                                            ) else MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.35f
                                            )
                                        )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = stringResource(R.string.tx_filter),
                                            tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (activeFilterCount > 0) {
                                            Badge(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp),
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) {
                                                Text("$activeFilterCount")
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Filter Summary Metric Card
                            Surface(
                                shape = AppShapes.Squircle,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .crispBorder(
                                        shape = AppShapes.Squircle,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.tx_total_found),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 0.6.sp,
                                                fontSize = 9.5.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${state.transactions.size} " + stringResource(R.string.tx_all_title),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (state.filteredExpenseTotal > 0) {
                                            Text(
                                                text = "-${CurrencyFormatter.formatRupiah(state.filteredExpenseTotal)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Black
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (state.filteredIncomeTotal > 0) {
                                            Text(
                                                text = "+${CurrencyFormatter.formatRupiah(state.filteredIncomeTotal)}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = IncomeAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Filter Bottom Sheet
                        if (showFilterBottomSheet) {
                            var draftType by remember { mutableStateOf(state.selectedType) }
                            var draftCategory by remember { mutableStateOf(state.selectedCategory) }
                            var draftWalletId by remember { mutableStateOf(state.selectedWalletId) }

                            ModalBottomSheet(
                                onDismissRequest = { showFilterBottomSheet = false },
                                shape = MaterialTheme.shapes.extraLarge,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 36.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    val isAnyDraftActive =
                                        draftType != null || draftCategory != null || draftWalletId != null

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tx_filter),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (isAnyDraftActive) {
                                            TextButton(onClick = {
                                                draftType = null
                                                draftCategory = null
                                                draftWalletId = null
                                            }) {
                                                Text(stringResource(R.string.tx_reset_filter))
                                            }
                                        }
                                    }

                                    // 1. Tipe Transaksi
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = stringResource(R.string.tx_type),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            item {
                                                val isAll = draftType == null
                                                FilterChip(
                                                    selected = isAll,
                                                    onClick = { draftType = null },
                                                    label = { Text(stringResource(R.string.tx_type_all)) },
                                                    shape = AppShapes.Pill,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                            val types = listOf(
                                                TransactionType.EXPENSE,
                                                TransactionType.INCOME,
                                                TransactionType.TRANSFER
                                            )
                                            items(types) { type ->
                                                val label = when (type) {
                                                    TransactionType.EXPENSE -> stringResource(R.string.tx_expense)
                                                    TransactionType.INCOME -> stringResource(R.string.tx_income)
                                                    else -> stringResource(R.string.tx_transfer)
                                                }
                                                val isSelected = draftType == type
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        draftType = if (isSelected) null else type
                                                    },
                                                    label = { Text(label) },
                                                    shape = AppShapes.Pill,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 2. Kategori
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = stringResource(R.string.tx_category),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(TransactionCategory.entries) { category ->
                                                val isSelected = category == draftCategory
                                                val catColors = categoryColors

                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        draftCategory =
                                                            if (isSelected) null else category
                                                    },
                                                    label = { Text(stringResource(category.labelRes())) },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = getCategoryIcon(category),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    },
                                                    shape = AppShapes.Pill,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = catColors.container(
                                                            category
                                                        ),
                                                        selectedLabelColor = catColors.onContainer(
                                                            category
                                                        ),
                                                        selectedLeadingIconColor = catColors.onContainer(
                                                            category
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 3. Dompet
                                    if (state.wallets.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = stringResource(R.string.tx_wallet_account),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(state.wallets) { wallet ->
                                                    val isSelected = wallet.id == draftWalletId
                                                    val parsedColor = try {
                                                        Color(
                                                            android.graphics.Color.parseColor(
                                                                wallet.colorHex
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        MaterialTheme.colorScheme.primary
                                                    }

                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            draftWalletId =
                                                                if (isSelected) null else wallet.id
                                                        },
                                                        label = { Text(wallet.name) },
                                                        leadingIcon = {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(parsedColor)
                                                            )
                                                        },
                                                        shape = AppShapes.Pill,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = parsedColor.copy(
                                                                alpha = 0.25f
                                                            ),
                                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    AppButton(
                                        onClick = {
                                            viewModel.applyFilters(
                                                category = draftCategory,
                                                type = draftType,
                                                walletId = draftWalletId
                                            )
                                            showFilterBottomSheet = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        shape = AppShapes.Pill
                                    ) {
                                        Text(
                                            stringResource(R.string.tx_apply_filter),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> { /* Loading state header */
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content List
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    when (val state = uiState) {
                        is AllTransactionsUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is AllTransactionsUiState.Error -> {
                            ErrorState(
                                title = stringResource(R.string.tx_load_error),
                                message = state.message,
                                retryLabel = stringResource(R.string.common_retry),
                                modifier = Modifier.align(Alignment.Center),
                                onRetry = { viewModel.loadTransactions() }
                            )
                        }

                        is AllTransactionsUiState.Success -> {
                            if (state.transactions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.searchQuery.isNotBlank() || state.selectedCategory != null) {
                                            stringResource(R.string.tx_no_match)
                                        } else {
                                            stringResource(R.string.tx_no_data)
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                                ) {
                                    items(state.transactions) { tx ->
                                        TransactionItem(
                                            transaction = tx,
                                            onClick = { tx.id?.let(onNavigateToTransactionDetail) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
