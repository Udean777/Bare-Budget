package com.ssajudn.barebudget.ui.goals

import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.domain.model.Goal
import com.ssajudn.barebudget.ui.components.AppConfirmDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog
import com.ssajudn.barebudget.domain.model.Wallet

enum class GoalFilter(val label: String) {
    ALL("Semua"),
    ACTIVE("Aktif"),
    COMPLETED("Tercapai")
}

val presetGoalColors = listOf(
    "#4E73DF", "#2ECC71", "#E74C3C", "#F39C12", "#9B59B6", "#1ABC9C"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: GoalsViewModel = hiltViewModel()
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Auto-refresh data dompet & target tabungan setiap kali pengguna kembali ke layar ini
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadGoals()
                viewModel.loadWallets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedFilter by remember { mutableStateOf(GoalFilter.ALL) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var actionSheetGoal by remember { mutableStateOf<Goal?>(null) }

    var depositGoalTarget by remember { mutableStateOf<Goal?>(null) }
    var initialIsWithdraw by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Target Tabungan",
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
                actions = {
                    IconButton(onClick = {
                        editingGoal = null
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Target")
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
                            "Cari target tabungan...",
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
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus")
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

                // 2. FILTER TABS (Semua / Aktif / Tercapai)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GoalFilter.entries.forEachIndexed { index, filter ->
                        SegmentedButton(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalFilter.entries.size)
                        ) {
                            Text(filter.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium))
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadGoals(isPullToRefresh = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is GoalsUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is GoalsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gagal memuat target tabungan",
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
                            Button(enabled = !isOperationLoading, onClick = { viewModel.loadGoals() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }

                    is GoalsUiState.Success -> {
                        val filteredGoals = state.goals.filter { g ->
                            val isDone = g.currentAmount >= g.targetAmount
                            when (selectedFilter) {
                                GoalFilter.ALL -> true
                                GoalFilter.ACTIVE -> !isDone
                                GoalFilter.COMPLETED -> isDone
                            }
                        }

                        if (filteredGoals.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Tidak Ada Target Tabungan",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Mulai buat target dana darurat, liburan, atau impian gadget Anda!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        editingGoal = null
                                        showAddDialog = true
                                    }) {
                                        Text("Buat Target Pertama")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(top = Spacing.MediumSmall, bottom = Spacing.FabClearance)
                            ) {
                                items(filteredGoals) { goal ->
                                    GoalCard(
                                        goal = goal,
                                        onClick = { actionSheetGoal = goal }
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
    if (actionSheetGoal != null) {
        val targetGoal = actionSheetGoal!!
        ModalBottomSheet(
            onDismissRequest = { actionSheetGoal = null },
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
                        val accentColor = try {
                            Color(android.graphics.Color.parseColor(targetGoal.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = targetGoal.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatRupiah(targetGoal.currentAmount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Target: ${CurrencyFormatter.formatRupiah(targetGoal.targetAmount)}",
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
                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            initialIsWithdraw = false
                            depositGoalTarget = g
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
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Column {
                                Text("Setor Tabungan (Deposit)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Alokasikan uang dari dompet ke target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            initialIsWithdraw = true
                            depositGoalTarget = g
                        },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Column {
                                Text("Tarik Tabungan (Withdraw)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Kembalikan saldo target ke dompet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            editingGoal = g
                            showAddDialog = true
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
                            Text("Edit Target Tabungan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Surface(
                        onClick = {
                            val g = targetGoal
                            actionSheetGoal = null
                            goalToDelete = g
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
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Hapus Target Ini", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { actionSheetGoal = null },
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

    // 3. ADD / EDIT GOAL DIALOG
    if (showAddDialog) {
        GoalFormDialog(
            initialGoal = editingGoal,
            onDismiss = {
                showAddDialog = false
                editingGoal = null
            },
            onConfirm = { name, targetAmount, targetDate, colorHex, notes ->
                if (editingGoal != null && editingGoal!!.id != null) {
                    viewModel.updateGoal(editingGoal!!.id!!, name, targetAmount, targetDate, colorHex, notes)
                } else {
                    viewModel.addGoal(name, targetAmount, targetDate, colorHex, notes)
                }
                showAddDialog = false
                editingGoal = null
            }
        )
    }

    // 4. DEPOSIT / WITHDRAW DIALOG WITH WALLET SELECTOR
    if (depositGoalTarget != null) {
        val g = depositGoalTarget!!
        DepositGoalDialog(
            goal = g,
            wallets = wallets,
            initialWithdraw = initialIsWithdraw,
            onDismiss = { depositGoalTarget = null },
            onConfirm = { amount, walletId ->
                g.id?.let { goalId ->
                    viewModel.depositToGoal(goalId, amount, walletId)
                }
                depositGoalTarget = null
            }
        )
    }

    // 5. DELETE CONFIRMATION DIALOG
    if (goalToDelete != null) {
        val target = goalToDelete!!
        AppConfirmDialog(
            title = "Hapus Target Tabungan?",
            message = "Apakah Anda yakin ingin menghapus target '${target.name}'? Seluruh progres tabungan ini akan dihapus.",
            confirmButtonText = "Hapus Target",
            onDismissRequest = { goalToDelete = null },
            onConfirm = {
                target.id?.let { viewModel.deleteGoal(it) }
                goalToDelete = null
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit
) {
    val isCompleted = goal.currentAmount >= goal.targetAmount
    val progressPercentInt = (goal.progressPercentage * 100).toInt()

    val daysLeft = if (!goal.targetDate!!.isNullOrBlank()) DateUtils.getDaysUntilDue(goal.targetDate!!) else null
    val isNearDeadline = daysLeft != null && daysLeft in 0..30 && goal.progressPercentage < 0.8f

    val (badgeBgColor, badgeTextColor, badgeLabel) = when {
        isCompleted -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Tercapai 100%")
        isNearDeadline -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Mendekati Deadline")
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "On Track")
    }

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = goal.progressPercentage,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "goalProgress"
    )

    val cardAccentColor = try {
        Color(android.graphics.Color.parseColor(goal.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .crispBorder(
                shape = AppShapes.Squircle,
                color = cardAccentColor.copy(alpha = 0.35f)
            )
            .clickable(onClick = onClick),
        shape = AppShapes.Squircle,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isCompleted) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Accent Dot, Title, & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(cardAccentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = AppShapes.Pill,
                    color = badgeBgColor
                ) {
                    Text(
                        text = badgeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = badgeTextColor
                    )
                }
            }

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Terkumpul",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(goal.currentAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target: ${CurrencyFormatter.formatRupiah(goal.targetAmount)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!goal.targetDate!!.isNullOrBlank()) {
                        Text(
                            text = "s/d ${DateUtils.formatDisplayDate(goal.targetDate!!)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = cardAccentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted) {
                        Text(
                            text = "Target Tercapai!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Sisa ${CurrencyFormatter.formatRupiah(goal.remainingAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$progressPercentInt%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Smart Calculator Banner
            if (!isCompleted && daysLeft != null && daysLeft > 0) {
                val monthsLeft = kotlin.math.max(1L, daysLeft / 30L)
                val perMonth = goal.remainingAmount / monthsLeft
                val perDay = goal.remainingAmount / daysLeft

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rekomendasi: Tabung ${CurrencyFormatter.formatCompact(perMonth)}/bln (${CurrencyFormatter.formatCompact(perDay)}/hari)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalColorRow(
    selectedColorHex: String,
    onSelectColor: (String) -> Unit
) {
    Column {
        Text(
            text = "Warna Aksen",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetGoalColors.forEach { colorHex ->
                val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onSelectColor(colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormDialog(
    initialGoal: Goal? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long, targetDate: String, colorHex: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var rawAmount by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialGoal?.targetAmount ?: 0L) }
    var targetDateIso by remember { mutableStateOf(initialGoal?.targetDate ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialGoal?.colorHex ?: presetGoalColors[0]) }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && parsedAmount > 0

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = if (targetDateIso.isNotBlank()) DateUtils.parseIsoToMillis(targetDateIso) else null,
            onDateSelected = { millis ->
                targetDateIso = DateUtils.formatMillisToIso(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AppFormDialog(
        title = if (initialGoal != null) "Edit Target Tabungan" else "Target Tabungan Baru",
        icon = Icons.Default.Payments,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = if (initialGoal != null) "Simpan Perubahan" else "Buat Target",
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            onConfirm(name, parsedAmount, targetDateIso, selectedColorHex, notes)
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Target") },
            placeholder = { Text("e.g. Dana Darurat, Liburan Bali, iPhone 16") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }.take(12)
                rawAmount = digitsOnly
                parsedAmount = digitsOnly.toLongOrNull() ?: 0L
            },
            label = { Text("Target Nominal") },
            placeholder = { Text("Rp 0") },
            singleLine = true,
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if (targetDateIso.isNotBlank()) DateUtils.formatDisplayDate(targetDateIso) else "",
            onValueChange = {},
            label = { Text("Tanggal Target (Opsional)") },
            placeholder = { Text("Pilih tanggal target") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        GoalColorRow(
            selectedColorHex = selectedColorHex,
            onSelectColor = { selectedColorHex = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan Tambahan (Opsional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositGoalDialog(
    goal: Goal,
    wallets: List<Wallet>,
    initialWithdraw: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, walletId: String) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var parsedAmount by remember { mutableStateOf(0L) }
    var isWithdraw by remember { mutableStateOf(initialWithdraw) }
    var selectedWallet by remember(wallets) { mutableStateOf(wallets.firstOrNull()) }
    var walletDropdownExpanded by remember { mutableStateOf(false) }

    val walletBalance = selectedWallet?.balance ?: 0L
    val isAmountValid = parsedAmount > 0
    val isBalanceValid = if (isWithdraw) parsedAmount <= goal.currentAmount else parsedAmount <= walletBalance
    val isFormValid = isAmountValid && isBalanceValid && selectedWallet?.id != null

    AppFormDialog(
        title = if (isWithdraw) "Tarik Tabungan" else "Setor Tabungan",
        icon = if (isWithdraw) Icons.Default.Payments else Icons.Default.Savings,
        iconTint = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        confirmButtonText = if (isWithdraw) "Tarik Dana" else "Setor Sekarang",
        confirmButtonContainerColor = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        isConfirmEnabled = isFormValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            selectedWallet?.id?.let { wId ->
                val finalAmount = if (isWithdraw) -parsedAmount else parsedAmount
                onConfirm(finalAmount, wId)
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isWithdraw,
                onClick = { isWithdraw = false },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("Setor (Deposit)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isWithdraw,
                onClick = { isWithdraw = true },
                leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("Tarik (Withdraw)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Wallet Selector Dropdown
        Text(
            text = if (isWithdraw) "Tujuan Dompet Penarikan" else "Sumber Dompet Setoran",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        ExposedDropdownMenuBox(
            expanded = walletDropdownExpanded,
            onExpandedChange = { walletDropdownExpanded = !walletDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedText = selectedWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: "Pilih Dompet"
            OutlinedTextField(
                value = selectedText,
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
                                Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Saldo: ${CurrencyFormatter.formatRupiah(wallet.balance)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }.take(12)
                rawAmount = digitsOnly
                parsedAmount = digitsOnly.toLongOrNull() ?: 0L
            },
            label = { Text("Nominal Rp") },
            placeholder = { Text("Rp 0") },
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            singleLine = true,
            isError = parsedAmount > 0 && !isBalanceValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (parsedAmount > 0 && !isBalanceValid) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isWithdraw) "Nominal melebihi saldo terkumpul target (${CurrencyFormatter.formatRupiah(goal.currentAmount)})"
                       else "Nominal melebihi saldo dompet terpilih (${CurrencyFormatter.formatRupiah(walletBalance)})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
