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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*

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
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.ui.theme.Spacing
import com.ssajudn.barebudget.ui.theme.categoryColors
import com.ssajudn.barebudget.ui.theme.crispBorder
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.domain.model.TransactionCategory

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
        modifier = modifier
            .fillMaxWidth()
            .crispBorder(
                shape = AppShapes.AsymmetricHero,
                color = accentColor.copy(alpha = 0.35f)
            ),
        shape = AppShapes.AsymmetricHero,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(AppShapes.Pill)
                        .background(contentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "FINANCIAL RUNWAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                FilledTonalButton(
                    onClick = onSetBudgetClick,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                ) {
                    Text(
                        text = if (totalBudget > 0) "Ubah Target" else "Atur Anggaran",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Big Number Display
            Text(
                text = CurrencyFormatter.formatRupiah(remainingBudget),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
            )

            Spacer(Modifier.height(10.dp))

            // Signature Expressive Gauge Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(AppShapes.Pill)
                    .background(contentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(AppShapes.Pill)
                        .background(accentColor)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Total Net Worth & Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL KEKAYAAN",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(netWorth),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = contentColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // Runway Insights Pill
            Surface(
                shape = AppShapes.Squircle,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

    val (prefix, trailingColor) = when (transaction.type) {
        TransactionType.INCOME -> "+" to MaterialTheme.colorScheme.primary
        TransactionType.TRANSFER -> "⇄ " to MaterialTheme.colorScheme.onSurface
        TransactionType.EXPENSE -> "-" to MaterialTheme.colorScheme.onSurface
    }

    ListItem(
        modifier = modifier.clickable(
            // Names the action for accessibility services
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
                    .clip(AppShapes.Squircle)
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
                text = if (transaction.type == TransactionType.TRANSFER) {
                    "Transfer Antar Dompet • ${DateUtils.formatDisplayDate(transaction.date)}"
                } else {
                    "${category.displayName} • ${DateUtils.formatDisplayDate(transaction.date)}"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                text = "$prefix$amountText",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = trailingColor
            )
        },
    )
}

fun getCategoryIcon(category: TransactionCategory): ImageVector = when (category) {
    TransactionCategory.FOOD -> Icons.Default.Restaurant
    TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
    TransactionCategory.BILLS -> Icons.AutoMirrored.Filled.ReceiptLong
    TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
    TransactionCategory.ENTERTAINMENT -> Icons.Default.SportsEsports
    TransactionCategory.SOCIAL -> Icons.Default.Groups
    TransactionCategory.SALARY -> Icons.Default.Payments
    TransactionCategory.BONUS -> Icons.Default.Redeem
    TransactionCategory.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
    TransactionCategory.TRANSFER -> Icons.Default.SwapHoriz
    TransactionCategory.OTHER -> Icons.Default.Category
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

