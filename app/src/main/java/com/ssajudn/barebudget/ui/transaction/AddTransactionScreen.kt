package com.ssajudn.barebudget.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.data.model.TransactionType
import com.ssajudn.barebudget.data.model.Wallet

import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.ui.components.getCategoryIcon
import com.ssajudn.barebudget.ui.theme.categoryColors
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation
import com.ssajudn.barebudget.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSplitBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.saveTransaction() },
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0,
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
                            text = "Save Expense",
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
            // 0. TRANSACTION TYPE (Income/Expense)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.transactionType == TransactionType.EXPENSE,
                    onClick = { viewModel.onTransactionTypeChange(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Pengeluaran")
                }
                SegmentedButton(
                    selected = uiState.transactionType == TransactionType.INCOME,
                    onClick = { viewModel.onTransactionTypeChange(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Pemasukan")
                }
            }

            // 0.5. WALLET SELECTION
            var walletDropdownExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = walletDropdownExpanded,
                onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedWalletName = uiState.wallets.find { it.id == uiState.selectedWalletId }?.name ?: "Pilih Dompet"
                OutlinedTextField(
                    value = selectedWalletName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dompet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = walletDropdownExpanded,
                    onDismissRequest = { walletDropdownExpanded = false }
                ) {
                    uiState.wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet.name) },
                            onClick = {
                                viewModel.onWalletChange(wallet.id!!)
                                walletDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 1. AMOUNT INPUT (Prominent M3 Display Card with Quick Presets)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.transactionType == TransactionType.INCOME) "Nominal Pemasukan" else "Nominal Pengeluaran",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.rawAmount,
                        onValueChange = { viewModel.onAmountChange(it) },
                        placeholder = {
                            Text(
                                "Rp 0",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        },
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        singleLine = true,
                        visualTransformation = CurrencyVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Split Bill Trigger Button
                    if (uiState.parsedAmount > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        FilledTonalButton(
                            onClick = { showSplitBottomSheet = true },
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Smart Split Bill (Patungan Cerdas)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // 2. CATEGORY SELECTOR (M3 Filter Chips with Animated Indicators)
            Column {
                Text(
                    text = "Kategori",
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
                    val incomeCats = listOf(TransactionCategory.SALARY, TransactionCategory.BONUS, TransactionCategory.INVESTMENT)
                    val filteredCats = TransactionCategory.entries.filter { if (uiState.transactionType == TransactionType.INCOME) it in incomeCats else it !in incomeCats }
                    items(filteredCats) { category ->
                        val isSelected = category == uiState.selectedCategory
                        val catColors = categoryColors

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategoryChange(category) },
                            label = {
                                Text(
                                    text = category.displayName,
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
                            shape = MaterialTheme.shapes.small,
                            // Per-category colour, so the selected chip reinforces
                            // the same hue used in the list and the analytics
                            // breakdown. Previously every category selected to the
                            // same primaryContainer, which threw the colour coding
                            // away at the one moment the user is choosing a category.
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColors.container(category),
                                selectedLabelColor = catColors.onContainer(category),
                                selectedLeadingIconColor = catColors.onContainer(category)
                            )
                        )
                    }
                }
            }

            // 3. MERCHANT / STORE NAME
            Column {
                Text(
                    text = "Merchant / Store (Optional)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = { viewModel.onMerchantChange(it) },
                    placeholder = { Text("e.g. Starbucks, Indomaret, Grab") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
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
                    text = "Tanggal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(uiState.date),
                    onValueChange = { },
                    readOnly = true,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
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
                    text = "Notes (Optional)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    placeholder = { Text("Add detail...") },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
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
                    viewModel.onNotesChange("Split bill (Porsi saya dari total ${CurrencyFormatter.formatRupiah(uiState.parsedAmount)})")
                }
                showSplitBottomSheet = false
            }
        )
    }
}
