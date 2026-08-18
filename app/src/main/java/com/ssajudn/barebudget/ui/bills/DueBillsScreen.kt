package com.ssajudn.barebudget.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.data.model.DueBill
import com.ssajudn.barebudget.data.model.DueBillStatus
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

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
                                    tint = PastelMintLight,
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
                            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
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
            onConfirm = { provider, amount, dueDate, isRecurring, interval, notes ->
                viewModel.addDueBill(provider, amount, dueDate, isRecurring, interval, notes)
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
    val isPaid = bill.status == DueBillStatus.PAID
    val daysLeft = DateUtils.getDaysUntilDue(bill.dueDate)
    val isOverdue = !isPaid && daysLeft < 0

    val badgeColor = when {
        isPaid -> PastelMintLight
        isOverdue -> PastelCoralLight
        daysLeft <= 3 -> PastelYellowLight
        else -> PastelBlueLight
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox indicator to toggle Paid/Unpaid
            FilledIconToggleButton(
                checked = isPaid,
                onCheckedChange = { onToggleStatus() },
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    checkedContainerColor = PastelMintLight,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                if (isPaid) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Paid",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
        amount: Long,
        dueDate: String,
        isRecurring: Boolean,
        recurringInterval: com.ssajudn.barebudget.data.model.RecurringInterval,
        notes: String
    ) -> Unit
) {
    var provider by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(DateUtils.getCurrentDateISO()) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf(com.ssajudn.barebudget.data.model.RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Due Bill",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Provider / Bill Name") },
                    placeholder = { Text("e.g. Shopee PayLater, Netflix, WiFi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { input ->
                        rawAmount = input.filter { it.isDigit() }.take(12)
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("Rp 0") },
                    visualTransformation = com.ssajudn.barebudget.utils.CurrencyVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Recurring Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isRecurring = !isRecurring }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Recurring Subscription",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Auto renew to next period when paid",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            com.ssajudn.barebudget.data.model.RecurringInterval.WEEKLY,
                            com.ssajudn.barebudget.data.model.RecurringInterval.MONTHLY,
                            com.ssajudn.barebudget.data.model.RecurringInterval.YEARLY
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = rawAmount.toLongOrNull() ?: 0L
                    if (provider.isNotBlank() && amount > 0) {
                        onConfirm(
                            provider,
                            amount,
                            dueDate,
                            isRecurring,
                            if (isRecurring) recurringInterval else com.ssajudn.barebudget.data.model.RecurringInterval.NONE,
                            notes
                        )
                    }
                }
            ) {
                Text("Add Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
