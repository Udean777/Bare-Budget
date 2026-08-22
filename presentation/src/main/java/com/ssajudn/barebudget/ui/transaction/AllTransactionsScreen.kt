package com.ssajudn.barebudget.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.domain.model.TransactionCategory
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.ui.components.TransactionItem
import com.ssajudn.barebudget.ui.components.getCategoryIcon
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.ui.theme.categoryColors
import com.ssajudn.barebudget.ui.theme.crispBorder
import com.ssajudn.barebudget.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    viewModel: AllTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showFilterBottomSheet by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadTransactions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Semua Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
            onRefresh = { viewModel.loadTransactions(isPullToRefresh = true) },
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
                                            "Cari transaksi...",
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        if (state.searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                                Icon(Icons.Default.Close, contentDescription = "Hapus")
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
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
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
                                            color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Filter Transaksi",
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
                                            text = "TOTAL DITEMUKAN",
                                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp, fontSize = 9.5.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${state.transactions.size} Transaksi",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (state.filteredExpenseTotal > 0) {
                                            Text(
                                                text = "-${CurrencyFormatter.formatRupiah(state.filteredExpenseTotal)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (state.filteredIncomeTotal > 0) {
                                            Text(
                                                text = "+${CurrencyFormatter.formatRupiah(state.filteredIncomeTotal)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF2ECC71)
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
                                    val isAnyDraftActive = draftType != null || draftCategory != null || draftWalletId != null

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Filter Transaksi",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (isAnyDraftActive) {
                                            TextButton(onClick = {
                                                draftType = null
                                                draftCategory = null
                                                draftWalletId = null
                                            }) {
                                                Text("Reset Filter")
                                            }
                                        }
                                    }

                                    // 1. Tipe Transaksi
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Tipe Transaksi",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            item {
                                                val isAll = draftType == null
                                                FilterChip(
                                                    selected = isAll,
                                                    onClick = { draftType = null },
                                                    label = { Text("Semua Tipe") },
                                                    shape = AppShapes.Pill,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                            val types = listOf(
                                                TransactionType.EXPENSE to "Pengeluaran 🔴",
                                                TransactionType.INCOME to "Pemasukan 🟢",
                                                TransactionType.TRANSFER to "Transfer ⇄"
                                            )
                                            items(types) { (type, label) ->
                                                val isSelected = draftType == type
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { draftType = if (isSelected) null else type },
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
                                            text = "Kategori",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(TransactionCategory.entries) { category ->
                                                val isSelected = category == draftCategory
                                                val catColors = categoryColors

                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { draftCategory = if (isSelected) null else category },
                                                    label = { Text(category.displayName) },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = getCategoryIcon(category),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
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

                                    // 3. Dompet
                                    if (state.wallets.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Dompet Akun",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(state.wallets) { wallet ->
                                                    val isSelected = wallet.id == draftWalletId
                                                    val parsedColor = try {
                                                        Color(android.graphics.Color.parseColor(wallet.colorHex))
                                                    } catch (e: Exception) {
                                                        MaterialTheme.colorScheme.primary
                                                    }

                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { draftWalletId = if (isSelected) null else wallet.id },
                                                        label = { Text(wallet.name) },
                                                        leadingIcon = {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                                    .background(parsedColor)
                                                            )
                                                        },
                                                        shape = AppShapes.Pill,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = parsedColor.copy(alpha = 0.25f),
                                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Button(
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
                                        Text("Terapkan Filter", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    else -> { /* Loading state header */ }
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
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadTransactions() }) {
                                    Text("Retry")
                                }
                            }
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
                                            "No transactions match your search or filter."
                                        } else {
                                            "No transactions recorded yet."
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
