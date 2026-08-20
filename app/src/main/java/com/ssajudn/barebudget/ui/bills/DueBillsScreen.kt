package com.ssajudn.barebudget.ui.bills

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.data.model.DueBill
import com.ssajudn.barebudget.data.model.DueBillStatus
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import coil.compose.AsyncImage
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.data.model.RecurringInterval
import androidx.activity.result.contract.ActivityResultContracts
import com.ssajudn.barebudget.R
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueBillsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: DueBillsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Due Bills & Commitments",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Bill")
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
            onRefresh = { viewModel.loadDueBills(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DueBillsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DueBillsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading bills",
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
                        Button(onClick = { viewModel.loadDueBills() }) {
                            Text("Retry")
                        }
                    }
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
                                    text = "No Due Bills",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You have no upcoming liabilities or paylater commitments.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = Spacing.MediumSmall, bottom = Spacing.FabClearance)
                        ) {
                            items(state.bills) { bill ->
                                DueBillItem(
                                    bill = bill,
                                    onToggleStatus = { viewModel.toggleBillStatus(bill) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDueBillDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { provider, iconUrl, amount, dueDate, isRecurring, interval, notes ->
                viewModel.addDueBill(provider, iconUrl, amount, dueDate, isRecurring, interval, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DueBillItem(
    bill: DueBill,
    onToggleStatus: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isPaid = bill.status == DueBillStatus.PAID
    val daysLeft = DateUtils.getDaysUntilDue(bill.dueDate)
    val isOverdue = !isPaid && daysLeft < 0

    val badgeColor = when {
        isPaid -> MaterialTheme.colorScheme.primary
        isOverdue -> MaterialTheme.colorScheme.error
        daysLeft <= 3 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isPaid) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox indicator to toggle Paid/Unpaid with Haptics
            FilledIconToggleButton(
                checked = isPaid,
                onCheckedChange = {
                    haptic.performHapticFeedback(
                        if (!isPaid) androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                        else androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                    )
                    onToggleStatus()
                },
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    // Announce the state, not just the icon, and keep the icon
                    // present-but-transparent so the target never changes size.
                    contentDescription = if (isPaid) "Sudah dibayar" else "Tandai sudah dibayar",
                    modifier = Modifier.size(20.dp),
                    tint = if (isPaid) LocalContentColor.current else Color.Transparent
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bill.providerIconUrl != null) {
                        if (bill.providerIconUrl.startsWith("res://")) {
                            val resId = bill.providerIconUrl.removePrefix("res://").toIntOrNull()
                            if (resId != null) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = resId),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(MaterialTheme.shapes.small),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        } else {
                            AsyncImage(
                                model = bill.providerIconUrl,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).clip(MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Text(
                        text = bill.providerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (bill.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "🔁 ${bill.recurringInterval.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Due ${DateUtils.formatDisplayDate(bill.dueDate)} • ${DateUtils.getDueStatusMessage(bill.dueDate)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant else badgeColor,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }

            Text(
                text = CurrencyFormatter.formatRupiah(bill.totalAmount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDueBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        provider: String,
        providerIconUrl: String?,
        amount: Long,
        dueDate: String,
        isRecurring: Boolean,
        recurringInterval: RecurringInterval,
        notes: String
    ) -> Unit
) {
    data class BillProvider(val name: String, val iconRes: Int? = null, val isCustom: Boolean = false)
    val builtinProviders = listOf(
        BillProvider("Shopee PayLater", R.drawable.ic_provider_shopee),
        BillProvider("Kredivo", R.drawable.ic_provider_kredivo),
        BillProvider("GoPay Later", R.drawable.ic_provider_gopay),
        BillProvider("Lainnya (Custom)", null, isCustom = true)
    )

    var selectedProvider by remember { mutableStateOf(builtinProviders[0]) }
    var customProviderName by remember { mutableStateOf("") }
    var customProviderIconUrl by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        customProviderIconUrl = uri?.toString()
    }

    var rawAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(DateUtils.getCurrentDateISO()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf("") }

    val amount = rawAmount.toLongOrNull() ?: 0L
    val finalProviderName = if (selectedProvider.isCustom) customProviderName else selectedProvider.name
    val isValid = finalProviderName.isNotBlank() && amount > 0

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = DateUtils.parseIsoToMillis(dueDate),
            onDateSelected = { millis ->
                dueDate = DateUtils.formatMillisToIso(millis)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AppFormDialog(
        title = "Add Due Bill",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        iconTint = MaterialTheme.colorScheme.error,
        confirmButtonText = "Save Bill",
        isConfirmEnabled = isValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            if (isValid) {
                val iconUrl = if (selectedProvider.isCustom) customProviderIconUrl else "res://${selectedProvider.iconRes}"
                onConfirm(
                    finalProviderName,
                    iconUrl,
                    amount,
                    dueDate,
                    isRecurring,
                    if (isRecurring) recurringInterval else RecurringInterval.NONE,
                    notes
                )
            }
        }
    ) {
        Text("Pilih Provider", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(builtinProviders) { provider ->
                FilterChip(
                    selected = selectedProvider == provider,
                    onClick = { selectedProvider = provider },
                    label = { Text(provider.name) }
                )
            }
        }

        if (selectedProvider.isCustom) {
            OutlinedTextField(
                value = customProviderName,
                onValueChange = { customProviderName = it },
                label = { Text("Nama Provider Custom") },
                placeholder = { Text("Mis. PLN, PDAM, dll") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (customProviderIconUrl != null) {
                    Text("Ubah Logo Provider (Terpilih)")
                } else {
                    Text("Upload Logo Provider (Opsional)")
                }
            }
        }


        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input.filter { it.isDigit() }.take(12)
            },
            label = { Text("Amount") },
            placeholder = { Text("Rp 0") },
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(
            value = DateUtils.formatDisplayDate(dueDate),
            onValueChange = { },
            label = { Text("Due Date") },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Recurring Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { isRecurring = !isRecurring }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recurring Subscription",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Auto renew when paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it }
            )
        }

        // Recurring Interval Selection
        if (isRecurring) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    RecurringInterval.WEEKLY,
                    RecurringInterval.MONTHLY,
                    RecurringInterval.YEARLY
                ).forEach { interval ->
                    val isSelected = recurringInterval == interval
                    FilterChip(
                        selected = isSelected,
                        onClick = { recurringInterval = interval },
                        label = { Text(interval.displayName, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
