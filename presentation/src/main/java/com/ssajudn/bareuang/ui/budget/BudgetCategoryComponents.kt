package com.ssajudn.bareuang.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppFormDialog
import com.ssajudn.bareuang.ui.components.getCategoryIcon
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.BudgetWarningAccent
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.utils.CurrencyFormatter

@Composable
fun CategoryBudgetCard(
    categoryBudget: CategoryBudget,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = categoryBudget.category
    val progress = categoryBudget.progressPercentage
    val progressColor = when {
        categoryBudget.isOverspent -> MaterialTheme.colorScheme.error
        categoryBudget.isWarning -> BudgetWarningAccent
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Squircle,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = categoryColors.container(category),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                tint = categoryColors.onContainer(category),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(category.labelRes()),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${CurrencyFormatter.formatCompact(categoryBudget.spentAmount)} / ${CurrencyFormatter.formatCompact(categoryBudget.limitAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = progressColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    AppIconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    AppIconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCategoryBudgetDialog(
    initialBudget: CategoryBudget?,
    existingCategories: Set<TransactionCategory>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionCategory, Long) -> Unit
) {
    val expenseCategories = TransactionCategory.entries.filter {
        it != TransactionCategory.TRANSFER &&
            it != TransactionCategory.SALARY &&
            it != TransactionCategory.BONUS &&
            it != TransactionCategory.INVESTMENT
    }
    val availableCategories = if (initialBudget != null) {
        listOf(initialBudget.category)
    } else {
        expenseCategories.filter { it !in existingCategories }.ifEmpty { expenseCategories }
    }

    var selectedCategory by remember { mutableStateOf(initialBudget?.category ?: availableCategories.firstOrNull() ?: expenseCategories.first()) }
    var rawAmount by remember { mutableStateOf(initialBudget?.limitAmount?.toString() ?: "") }
    var parsedAmount by remember { mutableStateOf(initialBudget?.limitAmount ?: 0L) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AppFormDialog(
        title = if (initialBudget != null) stringResource(R.string.budget_category_edit) else stringResource(R.string.budget_category_add),
        icon = Icons.Default.Category,
        iconTint = MaterialTheme.colorScheme.primary,
        confirmButtonText = stringResource(R.string.common_save),
        isConfirmEnabled = parsedAmount > 0,
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(selectedCategory, parsedAmount) }
    ) {
        if (initialBudget == null) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stringResource(selectedCategory.labelRes()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.budget_category_select)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    availableCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(stringResource(cat.labelRes())) },
                            leadingIcon = { Icon(getCategoryIcon(cat), contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = { selectedCategory = cat; dropdownExpanded = false }
                        )
                    }
                }
            }
        }
        AmountTextField(
            value = rawAmount,
            onValueChange = { input -> rawAmount = input; parsedAmount = input.toLongOrNull() ?: 0L },
            label = stringResource(R.string.budget_category_limit),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
