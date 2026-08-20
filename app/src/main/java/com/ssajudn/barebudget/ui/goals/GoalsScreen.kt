package com.ssajudn.barebudget.ui.goals

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssajudn.barebudget.data.model.Goal
import com.ssajudn.barebudget.ui.components.AppConfirmDialog
import com.ssajudn.barebudget.ui.components.AppFormDialog
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.utils.CurrencyVisualTransformation
import com.ssajudn.barebudget.ui.components.AppDatePickerDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: GoalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<Goal?>(null) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Savings Goals & Pockets",
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
                        Icon(Icons.Default.Add, contentDescription = "Add Goal")
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
            onRefresh = { viewModel.loadGoals(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            text = "Error loading goals",
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
                        Button(onClick = { viewModel.loadGoals() }) {
                            Text("Retry")
                        }
                    }
                }

                is GoalsUiState.Success -> {
                    if (state.goals.isEmpty()) {
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
                                    text = "No Savings Goals Yet",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Set up your emergency fund, vacation target, or gadget pockets!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { showAddDialog = true }) {
                                    Text("Create First Goal")
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
                            items(state.goals) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onDepositClick = { selectedGoalForDeposit = goal },
                                    onDeleteClick = { goalToDelete = goal }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, targetAmount, targetDate, colorHex, notes ->
                viewModel.addGoal(name, targetAmount, targetDate, colorHex, notes)
                showAddDialog = false
            }
        )
    }

    if (selectedGoalForDeposit != null) {
        DepositGoalDialog(
            goal = selectedGoalForDeposit!!,
            onDismiss = { selectedGoalForDeposit = null },
            onConfirm = { amount ->
                selectedGoalForDeposit?.id?.let { goalId ->
                    viewModel.depositToGoal(goalId, amount)
                }
                selectedGoalForDeposit = null
            }
        )
    }

    if (goalToDelete != null) {
        AppConfirmDialog(
            title = "Delete Savings Goal?",
            message = "Are you sure you want to delete '${goalToDelete?.name}'? Your saved progress will be permanently removed.",
            onDismissRequest = { goalToDelete = null },
            onConfirm = {
                goalToDelete?.id?.let { viewModel.deleteGoal(it) }
                goalToDelete = null
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onDepositClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isCompleted = goal.currentAmount >= goal.targetAmount
    val progressPercentInt = (goal.progressPercentage * 100).toInt()

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = goal.progressPercentage,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "goalProgress"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeIncreased,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Goal Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Achieved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDeleteClick()
                    },
                    // 48dp is the minimum accessible touch target. This was 28dp
                    // on a destructive action.
                    modifier = Modifier.size(MinTouchTarget)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Hapus target",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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
                    if (!goal.targetDate.isNullOrBlank()) {
                        Text(
                            text = "Target ${DateUtils.formatDisplayDate(goal.targetDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Smooth Animated Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCompleted) "🎉 Target Tercapai!" else "Kurang ${CurrencyFormatter.formatRupiah(goal.remainingAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$progressPercentInt%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick Deposit/Withdraw Button
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onDepositClick()
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Setor / Tarik Tabungan", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long, targetDate: String, colorHex: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val amount = rawAmount.toLongOrNull() ?: 0L
    val isValid = name.isNotBlank() && amount > 0

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = if (targetDate.isNotBlank()) DateUtils.parseIsoToMillis(targetDate) else null,
            onDateSelected = { millis ->
                targetDate = DateUtils.formatMillisToIso(millis)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AppFormDialog(
        title = "New Savings Goal",
        icon = Icons.Default.Payments,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = "Create Goal",
        isConfirmEnabled = isValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            if (isValid) {
                onConfirm(name, amount, targetDate, "#4E73DF", notes)
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Goal Name") },
            placeholder = { Text("e.g. Dana Darurat, Liburan Bali") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input.filter { it.isDigit() }.take(12)
            },
            label = { Text("Target Amount") },
            placeholder = { Text("Rp 0") },
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = if (targetDate.isNotBlank()) DateUtils.formatDisplayDate(targetDate) else "",
            onValueChange = { },
            label = { Text("Target Date (Optional)") },
            placeholder = { Text("Pilih tanggal (opsional)") },
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
}

@Composable
fun DepositGoalDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var isWithdraw by remember { mutableStateOf(false) }

    val amount = rawAmount.toLongOrNull() ?: 0L
    val isValid = amount > 0

    AppFormDialog(
        title = if (isWithdraw) "Tarik Tabungan" else "Setor Tabungan",
        icon = if (isWithdraw) Icons.Default.Payments else Icons.Default.Savings,
        iconTint = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        confirmButtonText = if (isWithdraw) "Tarik Dana" else "Setor Sekarang",
        confirmButtonContainerColor = if (isWithdraw) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        isConfirmEnabled = isValid,
        onDismissRequest = onDismiss,
        onConfirm = {
            if (isValid) {
                val finalAmount = if (isWithdraw) -amount else amount
                onConfirm(finalAmount)
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
                label = { Text("➕ Setor (Deposit)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isWithdraw,
                onClick = { isWithdraw = true },
                label = { Text("➖ Tarik (Withdraw)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rawAmount,
            onValueChange = { input ->
                rawAmount = input.filter { it.isDigit() }.take(12)
            },
            label = { Text("Nominal") },
            placeholder = { Text("Rp 0") },
            visualTransformation = CurrencyVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
