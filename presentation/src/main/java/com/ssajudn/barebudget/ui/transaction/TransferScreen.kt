package com.ssajudn.barebudget.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation
import com.ssajudn.barebudget.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onNavigateBack: (() -> Unit)? = null,
    onTransferSuccess: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Set transaction type to TRANSFER on open
    LaunchedEffect(Unit) {
        viewModel.onTransactionTypeChange(TransactionType.TRANSFER)
    }

    // Refresh wallet balance on resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onTransactionTypeChange(TransactionType.TRANSFER)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }
    var targetDropdownExpanded by remember { mutableStateOf(false) }

    val sourceWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
    val targetWallet = uiState.wallets.find { it.id == uiState.selectedToWalletId }

    val isBalanceInsufficient = sourceWallet != null && uiState.parsedAmount > sourceWallet.balance

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onTransferSuccess?.invoke()
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = DateUtils.parseIsoToMillis(uiState.date),
            onDateSelected = { millis ->
                viewModel.onDateChange(DateUtils.formatMillisToIso(millis))
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transfer Antar Dompet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
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
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0 && !isBalanceInsufficient && uiState.selectedWalletId != null && uiState.selectedToWalletId != null,
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
                            text = "Kirim Transfer Sekarang",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // DUAL WALLET SELECTOR CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source Wallet Dropdown
                    Text(
                        text = "Dompet Asal (Pengirim)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = sourceDropdownExpanded,
                        onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedText = sourceWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: "Pilih Dompet Asal"
                        OutlinedTextField(
                            value = selectedText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dari Dompet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false }
                        ) {
                            uiState.wallets.forEach { wallet ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("Saldo: ${CurrencyFormatter.formatRupiah(wallet.balance)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onWalletChange(wallet.id ?: "")
                                        sourceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Transfer Icon Arrow Down
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Target Wallet Dropdown
                    Text(
                        text = "Dompet Tujuan (Penerima)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = targetDropdownExpanded,
                        onExpandedChange = { targetDropdownExpanded = !targetDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedText = targetWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: "Pilih Dompet Tujuan"
                        OutlinedTextField(
                            value = selectedText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ke Dompet") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = targetDropdownExpanded,
                            onDismissRequest = { targetDropdownExpanded = false }
                        ) {
                            uiState.wallets.filter { it.id != uiState.selectedWalletId }.forEach { wallet ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("Saldo: ${CurrencyFormatter.formatRupiah(wallet.balance)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onToWalletChange(wallet.id ?: "")
                                        targetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // AMOUNT INPUT
            Text(
                text = "Nominal Transfer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = uiState.rawAmount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Jumlah Saldo (Rp)") },
                placeholder = { Text("Rp 0") },
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isBalanceInsufficient,
                modifier = Modifier.fillMaxWidth()
            )

            if (isBalanceInsufficient) {
                Text(
                    text = "Nominal melebihi saldo dompet asal (${CurrencyFormatter.formatRupiah(sourceWallet?.balance ?: 0L)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // DATE & NOTES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(uiState.date),
                    onValueChange = {},
                    label = { Text("Tanggal Transfer") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }
                )
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text("Catatan / Keterangan (Opsional)") },
                placeholder = { Text("e.g. Top up saldo GoPay, Tarik tunai BCA") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
