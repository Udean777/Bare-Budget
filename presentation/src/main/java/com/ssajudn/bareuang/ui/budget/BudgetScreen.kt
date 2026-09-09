package com.ssajudn.bareuang.ui.budget
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.ui.tour.tourAnchor
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.ui.components.AmountTextField

import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.components.BearPeek
import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.TransactionCategory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val isOperationLoading = operation is OperationState.Loading
    var pendingDailyMode by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(effect.uiText.asString(context))
                is UiEffect.Navigate -> {}
                is UiEffect.PopBackStack -> {}
            }
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.budget_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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
                    onClick = { viewModel.saveBudget() },
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0 && !uiState.isLocked && !isOperationLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (uiState.isLocked) stringResource(R.string.budget_locked_btn) else stringResource(R.string.budget_save_btn),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .padding(horizontal = Spacing.ScreenHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Explanatory Card (M3 ElevatedCard)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .tourAnchor("budget_explainer")
                    .crispBorder(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.budget_runway_banner_title),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.budget_runway_banner_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (uiState.isLocked) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .crispBorder(
                            shape = AppShapes.Squircle,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                        ),
                    shape = AppShapes.Squircle,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.budget_locked_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.budget_locked_desc, CurrencyFormatter.formatRupiah(uiState.currentLimit)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Amount Input — bear peek when amount entered
            Box(modifier = Modifier.fillMaxWidth()) {
                BearPeek(
                    visible = uiState.parsedAmount > 0,
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-10).dp, x = 4.dp),
                    size = 38.dp
                )
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (uiState.parsedAmount > 0) 12.dp else 0.dp)
                        .tourAnchor("budget_input_amount"),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.budget_input_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AmountTextField(
                        value = uiState.rawAmount,
                        onValueChange = { if (!uiState.isLocked) viewModel.onAmountChange(it) },
                        enabled = !uiState.isLocked,
                        placeholder = {
                            Text(
                                stringResource(R.string.common_rp_zero),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                }
            }

            // Quick Selection Presets
            Text(
                text = stringResource(R.string.budget_presets_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(2_000_000L, 3_500_000L, 5_000_000L).forEach { preset ->
                    SuggestionChip(
                        onClick = { if (!uiState.isLocked) viewModel.onAmountChange(preset.toString()) },
                        enabled = !uiState.isLocked,
                        label = {
                            Text(
                                CurrencyFormatter.formatCompact(preset),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // CATEGORY BUDGETS SECTION — only shown after a budget has been saved
            if (uiState.currentLimit > 0) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .crispBorder(shape = AppShapes.Squircle, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)),
                    shape = AppShapes.Squircle,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Read-only daily pacing info: shows where the per-day number comes from.
                val daysInMonth = java.time.YearMonth.now().lengthOfMonth()
                val dailyAllowance = uiState.currentLimit / daysInMonth
                Surface(
                    shape = AppShapes.Squircle,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.budget_daily_pacing_desc, CurrencyFormatter.formatCompact(dailyAllowance)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.budget_daily_target_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !uiState.isCustomDailyTarget,
                        onClick = { if (uiState.isCustomDailyTarget) pendingDailyMode = false },
                        label = { Text(stringResource(R.string.budget_daily_target_auto)) }
                    )
                    FilterChip(
                        selected = uiState.isCustomDailyTarget,
                        onClick = { if (!uiState.isCustomDailyTarget) pendingDailyMode = true },
                        label = { Text(stringResource(R.string.budget_daily_target_custom)) }
                    )
                }
                if (uiState.isCustomDailyTarget) {
                    AmountTextField(
                        value = uiState.dailyTargetInput,
                        onValueChange = viewModel::onDailyTargetChange,
                        label = stringResource(R.string.budget_daily_target_hint),
                        enabled = !uiState.isDailyTargetSaved,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = {
                            if (uiState.isDailyTargetSaved) viewModel.editCustomDailyTarget()
                            else viewModel.saveCustomDailyTarget()
                        },
                        enabled = uiState.isDailyTargetSaved || uiState.dailyTargetInput.toLongOrNull()?.let { it > 0L } == true
                    ) {
                        Text(
                            stringResource(
                                if (uiState.isDailyTargetSaved) R.string.budget_daily_target_edit
                                else R.string.budget_daily_target_save
                            )
                        )
                    }
                }
                }
                }

                var showCategoryDialog by remember { mutableStateOf(false) }
                var editingCategoryBudget by remember { mutableStateOf<CategoryBudget?>(null) }
                var categoryToDelete by remember { mutableStateOf<CategoryBudget?>(null) }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.budget_category_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.budget_category_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            editingCategoryBudget = null
                            showCategoryDialog = true
                        },
                        shape = AppShapes.Pill,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.common_add), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                // Allocation Summary
                if (uiState.categoryBudgets.isNotEmpty()) {
                    Surface(
                        shape = AppShapes.Squircle,
                        color = if (uiState.isOverAllocated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.isOverAllocated) Icons.Default.Warning else Icons.Default.PieChart,
                                contentDescription = null,
                                tint = if (uiState.isOverAllocated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (uiState.isOverAllocated) {
                                    stringResource(R.string.budget_category_over_warning)
                                } else {
                                    stringResource(
                                        R.string.budget_category_allocated,
                                        CurrencyFormatter.formatCompact(uiState.totalAllocatedCategory),
                                        CurrencyFormatter.formatCompact(uiState.currentLimit)
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (uiState.isOverAllocated) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.isOverAllocated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Category list
                if (uiState.categoryBudgets.isEmpty()) {
                    Surface(
                        shape = AppShapes.Squircle,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.budget_category_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    uiState.categoryBudgets.forEach { catBudget ->
                        CategoryBudgetCard(
                            categoryBudget = catBudget,
                            onEdit = {
                                editingCategoryBudget = catBudget
                                showCategoryDialog = true
                            },
                            onDelete = {
                                categoryToDelete = catBudget
                            }
                        )
                    }
                }

                // Category Form Dialog
                if (showCategoryDialog) {
                    SetCategoryBudgetDialog(
                        initialBudget = editingCategoryBudget,
                        existingCategories = uiState.categoryBudgets.map { it.category }.toSet(),
                        onDismiss = { showCategoryDialog = false },
                        onConfirm = { category, limit ->
                            viewModel.setCategoryBudget(category, limit)
                            showCategoryDialog = false
                        }
                    )
                }

                // Category Delete Confirmation Dialog
                if (categoryToDelete != null) {
                AppConfirmDialog(
                        title = stringResource(R.string.budget_category_delete),
                        message = stringResource(R.string.budget_category_delete_confirm, stringResource(categoryToDelete!!.category.labelRes())),
                        confirmButtonText = stringResource(R.string.common_delete),
                        dismissButtonText = stringResource(R.string.common_cancel),
                        onConfirm = {
                            viewModel.deleteCategoryBudget(categoryToDelete!!.category)
                            categoryToDelete = null
                        },
                        onDismissRequest = { categoryToDelete = null }
                    )
                }
            }

        }
        }
    }

    pendingDailyMode?.let { useCustom ->
        AppConfirmDialog(
            onDismissRequest = { pendingDailyMode = null },
            onConfirm = {
                if (useCustom) viewModel.selectCustomDailyTarget() else viewModel.setAutomaticDailyTarget()
                pendingDailyMode = null
            },
            title = stringResource(
                if (useCustom) R.string.budget_daily_mode_custom_confirm_title
                else R.string.budget_daily_mode_auto_confirm_title
            ),
            message = stringResource(
                if (useCustom) R.string.budget_daily_mode_custom_confirm_message
                else R.string.budget_daily_mode_auto_confirm_message
            ),
            confirmButtonText = stringResource(R.string.budget_daily_mode_confirm),
            dismissButtonText = stringResource(R.string.common_cancel),
            icon = Icons.Default.Today,
            isDestructive = false,
        )
    }
}
