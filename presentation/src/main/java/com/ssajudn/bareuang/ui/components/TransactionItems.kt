package com.ssajudn.bareuang.ui.components
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.ssajudn.bareuang.domain.model.RecurringInterval

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter

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
    val merchantName = transaction.merchant?.takeIf { it.isNotBlank() } ?: stringResource(category.labelRes())
    val amountText = CurrencyFormatter.formatRupiah(transaction.amount)

    val (prefix, trailingColor) = when (transaction.type) {
        TransactionType.INCOME   -> "+" to MaterialTheme.colorScheme.secondary
        TransactionType.TRANSFER -> "⇄ " to MaterialTheme.colorScheme.onSurface
        TransactionType.EXPENSE  -> "-" to MaterialTheme.colorScheme.onSurface
    }

    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.98f)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClickLabel = stringResource(R.string.runway_view_detail, merchantName),
                onClick = onClick,
            ),
        colors = ListItemDefaults.colors(
            // White card (#FFFFFF) on cream background (#FDF9F3) = tonal depth without hard shadow
            // DESIGN.MD §6: "surface-container-lowest: #ffffff — Elevated card & modal backgrounds"
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = merchantName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (transaction.isRecurringParent || transaction.recurringInterval != RecurringInterval.NONE || transaction.parentRecurringId != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = AppShapes.Pill,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tx_badge_recurring),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        supportingContent = {
            Text(
                text = if (transaction.type == TransactionType.TRANSFER) {
                    stringResource(R.string.tx_transfer_label) + " • " + DateFormatter.formatDisplayDate(transaction.date)
                } else {
                    stringResource(category.labelRes()) + " • " + DateFormatter.formatDisplayDate(transaction.date)
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

/**
 * Dedicated UI item for scheduled recurring transactions.
 * Shows neutral amount without +/- signs, cycle info, and next scheduled date.
 */
@Composable
fun RecurringTransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val category = transaction.category
    val colors = categoryColors
    val merchantName = transaction.merchant?.takeIf { it.isNotBlank() } ?: stringResource(category.labelRes())
    val amountText = CurrencyFormatter.formatRupiah(transaction.amount)
    val nextDate = transaction.nextOccurrenceDate ?: transaction.date
    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.98f)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(CategoryIconContainerSize)
                    .clip(AppShapes.Squircle)
                    .background(colors.container(category)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = colors.onContainer(category),
                    modifier = Modifier.size(CategoryIconSize)
                )
            }
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = merchantName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = AppShapes.Pill,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(transaction.recurringInterval.labelRes()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.dashboard_recurring_next, DateFormatter.formatDisplayDate(nextDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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

private val CategoryIconContainerSize = 40.dp
private val CategoryIconSize = 22.dp
