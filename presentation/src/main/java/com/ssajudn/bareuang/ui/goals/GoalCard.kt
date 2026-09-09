package com.ssajudn.bareuang.ui.goals
import androidx.compose.material3.Badge

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.domain.model.Goal
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.components.BearPeek
import com.ssajudn.bareuang.ui.components.BearProgressIndicator
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter
import java.time.LocalDate

val presetGoalColors = listOf(
    "#4E73DF", "#2ECC71", "#E74C3C", "#F39C12", "#9B59B6", "#1ABC9C"
)

@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
) {
    val isCompleted = goal.currentAmount >= goal.targetAmount
    val progressPercentInt = (goal.progressPercentage * 100).toInt()

    val daysLeft = goal.daysLeftUntilTarget(LocalDate.now())
    val isNearDeadline = daysLeft != null && daysLeft in 0..30 && goal.progressPercentage < 0.8f

    val (badgeBgColor, badgeTextColor, badgeLabel) = when {
        isCompleted -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, stringResource(R.string.goals_badge_completed))
        isNearDeadline -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, stringResource(R.string.goals_badge_near_deadline))
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, stringResource(R.string.goals_badge_on_track))
    }

    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressPercentage,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "goalProgress"
    )

    val primaryFallback = MaterialTheme.colorScheme.primary
    val cardAccentColor = remember(goal.colorHex) { goal.colorHex.toComposeColorOr(primaryFallback) }

    Box(modifier = Modifier.fillMaxWidth()) {
        BearPeek(visible = isCompleted, modifier = Modifier.align(Alignment.TopEnd).offset(y = (-12).dp, x = 6.dp), size = 40.dp)
        if (isCompleted) {
            com.ssajudn.bareuang.ui.components.ConfettiBurst(trigger = true, modifier = Modifier.matchParentSize())
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isCompleted) 14.dp else 0.dp)
                .crispBorder(
                    shape = MaterialTheme.shapes.medium,
                    color = cardAccentColor.copy(alpha = 0.35f)
                )
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isCompleted) 1.dp else 2.dp)
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
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
                        text = stringResource(R.string.goals_collected),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(goal.currentAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.goals_target_prefix, CurrencyFormatter.formatRupiah(goal.targetAmount)),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    goal.targetDate?.let { targetDate ->
                        Text(
                            text = stringResource(R.string.goals_until, DateFormatter.formatDisplayDate(targetDate)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Progress Bar with signature Bear Mascot indicator
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BearProgressIndicator(
                    progress = goal.progressPercentage,
                    color = cardAccentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    trackHeight = 10.dp,
                    bearSize = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted) {
                        Text(
                            text = stringResource(R.string.goals_completed_msg),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.goals_remaining, CurrencyFormatter.formatRupiah(goal.remainingAmount)),
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
            goal.suggestedSavingsPace(LocalDate.now())?.takeIf { !isCompleted }?.let { pace ->
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
                            text = stringResource(R.string.goals_recommend, CurrencyFormatter.formatCompact(pace.first), CurrencyFormatter.formatCompact(pace.second)),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            text = stringResource(R.string.goals_accent),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetGoalColors.forEach { colorHex ->
                val color = remember(colorHex) { colorHex.toComposeColorOr(Color(0xFFD8C3AD)) }
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

internal fun String.toComposeColorOr(fallback: Color): Color =
    try { Color(android.graphics.Color.parseColor(this)) } catch (_: Exception) { fallback }
