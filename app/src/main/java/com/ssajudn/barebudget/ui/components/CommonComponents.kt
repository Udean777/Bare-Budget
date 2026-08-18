package com.ssajudn.barebudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.barebudget.data.model.Transaction
import com.ssajudn.barebudget.data.model.TransactionCategory
import com.ssajudn.barebudget.ui.theme.*
import com.ssajudn.barebudget.utils.CurrencyFormatter
import com.ssajudn.barebudget.utils.DateUtils

@Composable
fun FinancialRunwayCard(
    remainingBudget: Long,
    totalBudget: Long,
    estimatedDeathDay: Int,
    daysInMonth: Int,
    daysPassed: Int,
    message: String,
    modifier: Modifier = Modifier,
    onSetBudgetClick: () -> Unit = {}
) {
    val isDanger = remainingBudget <= 0 || estimatedDeathDay < daysInMonth
    val containerBg = if (isDanger) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isDanger) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val progress = if (totalBudget > 0) {
        (remainingBudget.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = containerBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDanger) PastelCoralLight else PastelMintLight)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FINANCIAL RUNWAY",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = contentColor
                    )
                }

                Text(
                    text = "Set Limit",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = contentColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSetBudgetClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = CurrencyFormatter.formatRupiah(remainingBudget),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )

            Text(
                text = "of ${CurrencyFormatter.formatRupiah(totalBudget)} monthly budget",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Flat Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isDanger) PastelCoralLight else PastelMintLight,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Honest Status Message / Call to Action
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetBudgetClick() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isDanger) PastelCoralLight else PastelMintLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (totalBudget <= 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap here to set your budget →",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    val categoryIcon = getCategoryIcon(transaction.category)
    val categoryColor = getCategoryPastelColor(transaction.category)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant?.takeIf { it.isNotBlank() } ?: transaction.category.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${transaction.category.displayName} • ${DateUtils.formatDisplayDate(transaction.date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "-${CurrencyFormatter.formatRupiah(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun getCategoryIcon(category: TransactionCategory): ImageVector {
    return when (category) {
        TransactionCategory.FOOD -> Icons.Default.Restaurant
        TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
        TransactionCategory.BILLS -> Icons.Default.ReceiptLong
        TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
        TransactionCategory.ENTERTAINMENT -> Icons.Default.SportsEsports
        TransactionCategory.SOCIAL -> Icons.Default.Groups
        TransactionCategory.OTHER -> Icons.Default.Category
    }
}

@Composable
fun getCategoryPastelColor(category: TransactionCategory): Color {
    return when (category) {
        TransactionCategory.FOOD -> PastelCoralLight
        TransactionCategory.TRANSPORT -> PastelBlueLight
        TransactionCategory.BILLS -> PastelYellowLight
        TransactionCategory.SHOPPING -> PastelMintLight
        TransactionCategory.ENTERTAINMENT -> PastelLavenderLight
        TransactionCategory.SOCIAL -> PastelBlueLight
        TransactionCategory.OTHER -> MaterialTheme.colorScheme.primary
    }
}
