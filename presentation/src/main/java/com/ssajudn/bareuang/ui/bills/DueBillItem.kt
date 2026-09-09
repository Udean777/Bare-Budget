package com.ssajudn.bareuang.ui.bills
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledIconToggleButton

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.domain.model.DueBill
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.pressScale
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter

@Composable
fun DueBillItem(
    bill: DueBill,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isPaid = bill.status == DueBillStatus.PAID
    // null = unparseable date: skip the relative badge rather than fabricate one.
    val daysLeft: Long? = remember(bill.dueDate) {
        runCatching { DateUtils.getDaysUntilDue(bill.dueDate) }.getOrNull()
    }
    val isOverdue = !isPaid && daysLeft != null && daysLeft < 0

    val (badgeBgColor, badgeTextColor, statusLabelText) = when {
        isPaid -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(R.string.bills_badge_paid)
        )
        isOverdue -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            stringResource(R.string.bills_badge_overdue, (-daysLeft!!).toInt())
        )
        daysLeft == 0L -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            stringResource(R.string.bills_badge_today)
        )
        // Unknown (unparseable date): no relative badge rather than a fabricated one.
        daysLeft == null -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            ""
        )
        else -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            stringResource(R.string.bills_badge_remaining, daysLeft.toInt())
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource, pressedScale = 0.96f)
            .crispBorder(
                shape = AppShapes.Squircle,
                color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            ),
        shape = AppShapes.Squircle,
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
                    contentDescription = if (isPaid) stringResource(R.string.bills_badge_paid_desc) else stringResource(R.string.bills_badge_unpaid_desc),
                    modifier = Modifier.size(20.dp),
                    tint = if (isPaid) MaterialTheme.colorScheme.onSurface else Color.Transparent
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val iconModel: Any? = remember(bill.providerIconUrl, bill.providerName) {
                        BillProviderCatalog.resolve(context, bill.providerIconUrl, bill.providerName)
                    }

                    if (iconModel != null) {
                        LocalProviderIcon(model = iconModel, size = 28.dp)
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
                    // Visual Status Badge (hidden when the date is unparseable)
                    if (statusLabelText.isNotEmpty()) {
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
                                    text = stringResource(bill.recurringInterval.labelRes()),
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
                    text = stringResource(R.string.bills_due_prefix, DateFormatter.formatDisplayDate(bill.dueDate)),
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
