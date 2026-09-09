package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.PriceDisplayStyle
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun DailyPacingCard(
    dailyAllowance: Long,
    todaySpent: Long,
    dailyProgress: Float,
    remainingToday: Long,
    remainingDays: Int,
    modifier: Modifier = Modifier,
) {
    val dailyExceeded = remainingToday < 0
    val accent = if (dailyExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val darkTheme = isSystemInDarkTheme()
    val cardContent = when {
        dailyExceeded -> MaterialTheme.colorScheme.onErrorContainer
        darkTheme -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .crispBorder(shape = AppShapes.Squircle, color = accent.copy(alpha = 0.35f)),
        shape = AppShapes.Squircle,
        color = if (dailyExceeded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.dashboard_daily_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = cardContent)
                    Text(stringResource(R.string.dashboard_daily_auto_desc), style = MaterialTheme.typography.bodySmall, color = cardContent.copy(alpha = 0.75f))
                }
                if (dailyExceeded) Icon(Icons.Default.Warning, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val metricColor = when {
                    dailyExceeded -> MaterialTheme.colorScheme.onErrorContainer
                    darkTheme -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                DailyPacingMetric(stringResource(R.string.dashboard_daily_target), CurrencyFormatter.formatRupiah(dailyAllowance), Modifier.weight(1f), metricColor)
                DailyPacingMetric(stringResource(R.string.dashboard_daily_spent), CurrencyFormatter.formatRupiah(todaySpent), Modifier.weight(1f), metricColor)
                DailyPacingMetric(stringResource(R.string.dashboard_daily_remaining_label), CurrencyFormatter.formatRupiah(remainingToday.coerceAtLeast(0L)), Modifier.weight(1f), if (dailyExceeded) accent else metricColor)
            }
            Spacer(Modifier.height(12.dp))
            BearProgressIndicator(
                progress = dailyProgress,
                modifier = Modifier.fillMaxWidth(),
                color = accent,
                trackColor = if (dailyExceeded) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else (if (darkTheme) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.18f),
                trackHeight = 8.dp,
                bearSize = 28.dp,
                indicatorRes = R.drawable.ic_app_logo,
            )
            if (remainingDays > 0) {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.dashboard_daily_allowance, remainingDays, CurrencyFormatter.formatRupiah(dailyAllowance)), style = MaterialTheme.typography.labelSmall, color = cardContent.copy(alpha = 0.75f))
            }
            if (dailyExceeded) {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.tx_error_daily_exceeded), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
            }
        }
    }
}

@Composable
private fun DailyPacingMetric(label: String, value: String, modifier: Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = valueColor, maxLines = 1)
    }
}
