package com.ssajudn.barebudget.ui.bills

import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.domain.model.DueBill
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.model.Wallet
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import coil.compose.AsyncImage
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.domain.model.RecurringInterval
import androidx.activity.result.contract.ActivityResultContracts
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueBillsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: DueBillsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val operation by viewModel.operation.collectAsState()
    val isOperationLoading = operation is OperationState.Loading
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> {}
            }
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Auto-refresh data dompet & tagihan setiap kali pengguna kembali ke layar ini
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadDueBills()
                viewModel.loadWallets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val selectedStatusFilter by viewModel.selectedStatus.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<DueBill?>(null) }
    var actionSheetBill by remember { mutableStateOf<DueBill?>(null) }

    var payingBill by remember { mutableStateOf<DueBill?>(null) }
    var unpayingBillConfirm by remember { mutableStateOf<DueBill?>(null) }
    var deletingBillConfirm by remember { mutableStateOf<DueBill?>(null) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    IconButton(onClick = {
                        editingBill = null
                        showFormDialog = true
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. FILTER TABS (Semua / Belum Lunas / Lunas)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                val filters = listOf(
                    Triple(null, "Semua", 0),
                    Triple(DueBillStatus.UNPAID, "Belum Lunas", 1),
                    Triple(DueBillStatus.PAID, "Lunas", 2)
                )
                filters.forEach { (status, label, index) ->
                    SegmentedButton(
                        selected = selectedStatusFilter == status,
                        enabled = !isOperationLoading, onClick = { viewModel.setFilterStatus(status) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label)
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
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gagal memuat daftar tagihan",
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
                                Text("Coba Lagi")
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
                                        text = "Tidak Ada Tagihan",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Semua kewajiban dan tagihan Anda sudah terkelola dengan baik.",
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
                                        onClick = { actionSheetBill = bill },
                                        onToggleStatus = {
                                            if (bill.status == DueBillStatus.UNPAID) {
                                                payingBill = bill
                                            } else {
                                                unpayingBillConfirm = bill
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
                                    imageVector = Icons.Default.ReceiptLong,
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
                                text = "Jatuh Tempo: ${DateUtils.formatDisplayDate(targetBill.dueDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Tindakan Cepat",
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
                                    Text("Bayar Tagihan Sekarang", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Pilih dompet sumber pembayaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
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
                                    Text("Batalkan Pembayaran (Refund)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text("Kembalikan status & saldo ke dompet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
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
                            Text("Edit Rincian Tagihan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
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
                            Text("Hapus Tagihan Ini", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
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
                    Text("Tutup")
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
        com.ssajudn.barebudget.ui.components.AppConfirmDialog(
            title = "Batal Pembayaran?",
            message = "Tagihan '${billToUnpay.providerName}' akan dikembalikan ke status Belum Lunas. Saldo dompet yang sebelumnya digunakan akan otomatis dikembalikan (refund).",
            confirmButtonText = "Batal Bayar",
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
        com.ssajudn.barebudget.ui.components.AppConfirmDialog(
            title = "Hapus Tagihan?",
            message = "Apakah Anda yakin ingin menghapus tagihan '${billToDelete.providerName}' sebesar ${CurrencyFormatter.formatRupiah(billToDelete.totalAmount)}?",
            confirmButtonText = "Hapus",
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

@Composable
fun DueBillItem(
    bill: DueBill,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isPaid = bill.status == DueBillStatus.PAID
    val daysLeft = DateUtils.getDaysUntilDue(bill.dueDate)
    val isOverdue = !isPaid && daysLeft < 0

    val (badgeBgColor, badgeTextColor, statusLabelText) = when {
        isPaid -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Lunas"
        )
        isOverdue -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Terlambat ${-daysLeft} Hari"
        )
        daysLeft == 0L -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Jatuh Tempo Hari Ini"
        )
        else -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Sisa $daysLeft Hari"
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    contentDescription = if (isPaid) "Sudah dibayar" else "Tandai sudah dibayar",
                    modifier = Modifier.size(20.dp),
                    tint = if (isPaid) LocalContentColor.current else Color.Transparent
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val resolvedResId: Int? = if (bill.providerIconUrl != null && bill.providerIconUrl!!.startsWith("res://")) {
                        bill.providerIconUrl!!.removePrefix("res://").toIntOrNull()
                    } else when {
                        bill.providerName.contains("Shopee", ignoreCase = true) -> R.drawable.logo_shopee
                        bill.providerName.contains("Kredivo", ignoreCase = true) -> R.drawable.logo_kredivo
                        bill.providerName.contains("GoPay", ignoreCase = true) -> R.drawable.logo_gopay
                        else -> null
                    }

                    if (resolvedResId != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = resolvedResId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else if (bill.providerIconUrl != null && !bill.providerIconUrl!!.startsWith("res://")) {
                        val iconModel: Any? = when {
                            bill.providerIconUrl!!.startsWith("/") -> java.io.File(bill.providerIconUrl!!)
                            bill.providerIconUrl!!.startsWith("file://") -> java.io.File(bill.providerIconUrl!!.removePrefix("file://"))
                            else -> bill.providerIconUrl!!
                        }
                        AsyncImage(
                            model = iconModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = bill.providerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Visual Status Badge
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = badgeBgColor
                    ) {
                        Text(
                            text = statusLabelText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = badgeTextColor
                        )
                    }

                    // Recurring Interval Badge (clean pill style)
                    if (bill.isRecurring) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = bill.recurringInterval.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Jatuh tempo: ${DateUtils.formatDisplayDate(bill.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
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
fun DueBillFormDialog(
    initialBill: DueBill? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        provider: String,
        providerIconUrl: String?,
        amount: Long,
        dueDate: String,
        isRecurring: Boolean,
        interval: RecurringInterval,
        notes: String
    ) -> Unit
) {
    data class BillProvider(val name: String, val iconRes: Int? = null, val isCustom: Boolean = false)
    val builtinProviders = listOf(
        BillProvider("Shopee PayLater", R.drawable.logo_shopee),
        BillProvider("Kredivo", R.drawable.logo_kredivo),
        BillProvider("GoPay Later", R.drawable.logo_gopay),
        BillProvider("Lainnya (Custom)", null, isCustom = true)
    )

    val existingProvider = builtinProviders.find { it.name == initialBill?.providerName }
    val initialSelectedProvider = existingProvider ?: if (initialBill != null) builtinProviders.last() else builtinProviders[0]

    var selectedProvider by remember { mutableStateOf(initialSelectedProvider) }
    var customProviderName by remember { mutableStateOf(if (existingProvider == null && initialBill != null) initialBill.providerName else "") }
    var customProviderIconUrl by remember { mutableStateOf<String?>(initialBill?.providerIconUrl) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            // ponytail: copy to internal storage so icon survives restart (content:// permission is transient)
            customProviderIconUrl = persistPickedImage(context, uri) ?: uri.toString()
        }
    }

    var rawAmount by remember { mutableStateOf(initialBill?.totalAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialBill?.totalAmount ?: 0L) }
    var dueDateIso by remember { mutableStateOf(initialBill?.dueDate ?: DateUtils.getCurrentDateISO()) }
    var isRecurring by remember { mutableStateOf(initialBill?.isRecurring ?: false) }
    var recurringInterval by remember { mutableStateOf(initialBill?.recurringInterval ?: RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf(initialBill?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = DateUtils.parseIsoToMillis(dueDateIso),
            onDateSelected = { millis ->
                dueDateIso = DateUtils.formatMillisToIso(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    val finalProviderName = if (selectedProvider.isCustom) customProviderName.trim() else selectedProvider.name
    val finalIconUrl = if (selectedProvider.isCustom) customProviderIconUrl else selectedProvider.iconRes?.let { "res://$it" }

    val isFormValid = finalProviderName.isNotBlank() && parsedAmount > 0

    AppFormDialog(
        title = if (initialBill != null) "Edit Tagihan" else "Tambah Tagihan Baru",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = if (initialBill != null) "Simpan Perubahan" else "Tambah Tagihan",
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            onConfirm(
                finalProviderName,
                finalIconUrl,
                parsedAmount,
                dueDateIso,
                isRecurring,
                if (isRecurring) recurringInterval else RecurringInterval.NONE,
                notes
            )
        }
    ) {
        // Provider Selection Dropdown (Combobox)
        var providerDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = providerDropdownExpanded,
            onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedProvider.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Penyedia / Kategori Tagihan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                leadingIcon = {
                    if (selectedProvider.iconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = selectedProvider.iconRes!!),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                    } else if (selectedProvider.isCustom && customProviderIconUrl != null) {
                        val previewModel: Any = when {
                            customProviderIconUrl!!.startsWith("/") -> java.io.File(customProviderIconUrl!!)
                            customProviderIconUrl!!.startsWith("file://") -> java.io.File(customProviderIconUrl!!.removePrefix("file://"))
                            else -> customProviderIconUrl!!
                        }
                        AsyncImage(
                            model = previewModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = providerDropdownExpanded,
                onDismissRequest = { providerDropdownExpanded = false }
            ) {
                builtinProviders.forEach { provider ->
                    DropdownMenuItem(
                        leadingIcon = {
                            if (provider.iconRes != null) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = provider.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        text = {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedProvider = provider
                            providerDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Custom Provider Input
        if (selectedProvider.isCustom) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customProviderName,
                onValueChange = { customProviderName = it },
                label = { Text("Nama Tagihan / Provider") },
                placeholder = { Text("e.g. PLN Electricity, Netflix, Wifi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (customProviderIconUrl != null) "Ganti Logo Icon" else "Upload Logo Icon (Opsional)")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount Input
        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }.take(12)
                rawAmount = digitsOnly
                parsedAmount = digitsOnly.toLongOrNull() ?: 0L
            },
            label = { Text("Jumlah Tagihan") },
            placeholder = { Text("Rp 0") },
            singleLine = true,
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Due Date Picker Field
        OutlinedTextField(
            value = DateUtils.formatDisplayDate(dueDateIso),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Jatuh Tempo") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notes Input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan / No. Pelanggan (Opsional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recurring Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tagihan Berulang",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Otomatis diperbarui saat dibayar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it }
            )
        }

        if (isRecurring) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(RecurringInterval.WEEKLY, RecurringInterval.MONTHLY, RecurringInterval.YEARLY).forEach { interval ->
                    FilterChip(
                        selected = recurringInterval == interval,
                        onClick = { recurringInterval = interval },
                        label = { Text(interval.displayName, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDueBillDialog(
    bill: DueBill,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String) -> Unit
) {
    var selectedWallet by remember(wallets) { mutableStateOf(wallets.firstOrNull()) }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    AppFormDialog(
        title = "Bayar Tagihan",
        icon = Icons.Default.AccountBalanceWallet,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = "Bayar Sekarang",
        isConfirmEnabled = selectedWallet?.id != null,
        onDismissRequest = onDismiss,
        onConfirm = {
            selectedWallet?.id?.let { onConfirm(it) }
        }
    ) {
        // Bill Info Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bill.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(bill.totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Jatuh tempo: ${DateUtils.formatDisplayDate(bill.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Pilih Sumber Dompet",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        ExposedDropdownMenuBox(
            expanded = walletDropdownExpanded,
            onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedWalletText = selectedWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: "Pilih Dompet"
            OutlinedTextField(
                value = selectedWalletText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Dompet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = walletDropdownExpanded,
                onDismissRequest = { walletDropdownExpanded = false }
            ) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Saldo: ${CurrencyFormatter.formatRupiah(wallet.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            selectedWallet = wallet
                            walletDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Saldo dompet akan otomatis terpotong dan dicatat sebagai pengeluaran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ponytail: content:// uri permission hilang setelah restart, copy ke filesDir
private fun persistPickedImage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val dir = java.io.File(context.filesDir, "duebill_icons").apply { mkdirs() }
        val mime = context.contentResolver.getType(uri)
        val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
        val file = java.io.File(dir, "duebill_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.$ext")
        file.outputStream().use { input.copyTo(it) }
        input.close()
        file.absolutePath
    } catch (_: Exception) { null }
}
