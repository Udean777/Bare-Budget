package com.ssajudn.barebudget.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.TrendingUp

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssajudn.barebudget.data.model.Transaction
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.ui.theme.Spacing
import com.ssajudn.barebudget.ui.theme.categoryColors
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils

@Composable
fun FinancialRunwayCard(
    remainingBudget: Long,
    netWorth: Long,
    totalBudget: Long,
    estimatedDeathDay: Int,
    daysInMonth: Int,
    message: String,
    modifier: Modifier = Modifier,
    onSetBudgetClick: () -> Unit = {},
) {
    val isDanger = remainingBudget <= 0 || estimatedDeathDay < daysInMonth
    val containerColor = if (isDanger) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isDanger) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val accentColor = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val targetProgress = if (totalBudget > 0) {
        (remainingBudget.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "runwayProgress",
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.ExtraLargeIncreased,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.MediumLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Spacing.Small)
                            .clip(CircleShape)
                            .background(accentColor),
                    )
                    Spacer(Modifier.width(Spacing.Small))
                    Text(
                        text = "SISA ANGGARAN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                FilledTonalButton(
                    onClick = onSetBudgetClick,
                    // surfaceContainerLowest reads as a raised chip on the tonal
                    // card without the translucency the old version used. A
                    // `surface.copy(alpha = 0.85f)` fill composites against
                    // whatever is behind it, so its contrast was unpredictable
                    // and differed between light and dark.
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Spacing.MediumSmall,
                        vertical = Spacing.ExtraSmall,
                    ),
                ) {
                    Text(
                        text = if (totalBudget > 0) "Ubah" else "Atur",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.Medium))

            Text(
                text = CurrencyFormatter.formatRupiah(remainingBudget),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.ExtraSmall))

            Text(
                text = "dari ${CurrencyFormatter.formatRupiah(totalBudget)} anggaran bulanan",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(Spacing.Medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL KEKAYAAN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(netWorth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(Spacing.Medium))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ProgressBarHeight),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Spacer(Modifier.height(Spacing.Medium))

            Surface(
                onClick = onSetBudgetClick,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.MediumSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(StatusIconContainerSize)
                            .clip(CircleShape)
                            .background(
                                if (isDanger) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDanger) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(StatusIconSize),
                        )
                    }

                    Spacer(Modifier.width(Spacing.MediumSmall))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (totalBudget <= 0) {
                            Spacer(Modifier.height(Spacing.ExtraSmall))
                            Text(
                                text = "Atur batas belanja bulanan Anda",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single transaction row.
 *
 * Built on M3 [ListItem] instead of the previous `Card` + `Row`, which
 * reimplemented it with invented padding and no merged semantics — a screen
 * reader read the merchant, the date and the amount as three separate nodes with
 * no hint the row was tappable.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val category = transaction.category
    val colors = categoryColors
    val merchantName = transaction.merchant?.takeIf { it.isNotBlank() } ?: category.displayName
    val amountText = CurrencyFormatter.formatRupiah(transaction.amount)

    ListItem(
        modifier = modifier.clickable(
            // Names the action for accessibility services, which the old
            // bare `clickable {}` did not.
            onClickLabel = "Lihat detail $merchantName",
            onClick = onClick,
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(CategoryIconContainerSize)
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.container(category)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = colors.onContainer(category),
                    modifier = Modifier.size(CategoryIconSize),
                )
            }
        },
        headlineContent = {
            Text(
                text = merchantName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = "${category.displayName} • ${DateUtils.formatDisplayDate(transaction.date)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                text = "-$amountText",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
    )
}

fun getCategoryIcon(category: TransactionCategory): ImageVector = when (category) {
    TransactionCategory.FOOD -> Icons.Default.Restaurant
    TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
    TransactionCategory.BILLS -> Icons.Default.ReceiptLong
    TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
    TransactionCategory.ENTERTAINMENT -> Icons.Default.SportsEsports
    TransactionCategory.SOCIAL -> Icons.Default.Groups
    TransactionCategory.SALARY -> Icons.Default.Payments
    TransactionCategory.BONUS -> Icons.Default.Redeem
    TransactionCategory.INVESTMENT -> Icons.Default.TrendingUp
    TransactionCategory.OTHER -> Icons.Default.Category
    else -> Icons.Default.Category
}

private val ProgressBarHeight = 8.dp
private val StatusIconContainerSize = 40.dp
private val StatusIconSize = 20.dp
private val CategoryIconContainerSize = 40.dp
private val CategoryIconSize = 22.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long? = null,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}

