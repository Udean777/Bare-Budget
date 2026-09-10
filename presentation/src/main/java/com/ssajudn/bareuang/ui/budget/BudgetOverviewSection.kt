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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.ui.tour.tourAnchor
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.ui.components.AmountTextField

import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar
import com.ssajudn.bareuang.ui.components.AppConfirmDialog
import com.ssajudn.bareuang.ui.components.BearPeek
import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.TransactionCategory

@Composable
internal fun BudgetOverviewSection(
    uiState: BudgetUiState,
    viewModel: BudgetViewModel
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
    
}

