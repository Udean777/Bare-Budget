package com.ssajudn.bareuang.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.utils.CurrencyFormatter

@Composable
fun CashflowBarChart(
    data: List<CashflowDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.analytics_no_cashflow_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(data.size - 1) }
    val maxVal = remember(data) {
        val maxIn = data.maxOfOrNull { it.income } ?: 0L
        val maxEx = data.maxOfOrNull { it.expense } ?: 0L
        maxOf(maxIn, maxEx).coerceAtLeast(100_000L)
    }

    val incomeColor = IncomeAccent
    val expenseColor = ExpenseAccent

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend & Selection Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(incomeColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.analytics_income),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(expenseColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.analytics_expense),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = stringResource(R.string.analytics_tap_bar_detail),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected month details card
        selectedIndex?.let { idx ->
            if (idx in data.indices) {
                val item = data[idx]
                val net = item.income - item.expense
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.analytics_month_prefix, item.label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (net >= 0) stringResource(R.string.analytics_surplus_prefix, CurrencyFormatter.formatRupiah(net)) else stringResource(R.string.analytics_deficit_prefix, CurrencyFormatter.formatRupiah(-net)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (net >= 0) incomeColor else expenseColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${CurrencyFormatter.formatRupiah(item.income)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = incomeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "-${CurrencyFormatter.formatRupiah(item.expense)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = expenseColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Custom Canvas Bar Chart
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val groupWidth = size.width / data.size
                            val clickedIndex = (offset.x / groupWidth).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = clickedIndex
                        }
                    }
            ) {
                val chartHeight = size.height - 24.dp.toPx()
                val groupWidth = size.width / data.size
                val barWidth = (groupWidth * 0.32f).coerceAtMost(16.dp.toPx())
                val spacingBetweenBars = 4.dp.toPx()

                // Draw 3 horizontal grid lines
                for (i in 0..2) {
                    val y = chartHeight * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                data.forEachIndexed { index, point ->
                    val groupCenterX = groupWidth * index + groupWidth / 2f
                    val incomeHeight = (point.income.toFloat() / maxVal) * chartHeight
                    val expenseHeight = (point.expense.toFloat() / maxVal) * chartHeight

                    val isSelected = index == selectedIndex

                    // Income Bar (Left)
                    val incomeLeft = groupCenterX - barWidth - (spacingBetweenBars / 2f)
                    val incomeTop = chartHeight - incomeHeight
                    drawRoundRect(
                        color = if (isSelected) incomeColor else incomeColor.copy(alpha = 0.75f),
                        topLeft = Offset(incomeLeft, incomeTop),
                        size = Size(barWidth, incomeHeight.coerceAtLeast(4.dp.toPx())),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Expense Bar (Right)
                    val expenseLeft = groupCenterX + (spacingBetweenBars / 2f)
                    val expenseTop = chartHeight - expenseHeight
                    drawRoundRect(
                        color = if (isSelected) expenseColor else expenseColor.copy(alpha = 0.75f),
                        topLeft = Offset(expenseLeft, expenseTop),
                        size = Size(barWidth, expenseHeight.coerceAtLeast(4.dp.toPx())),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Selection highlight indicator
                    if (isSelected) {
                        drawCircle(
                            color = incomeColor.copy(alpha = 0.15f),
                            radius = groupWidth * 0.45f,
                            center = Offset(groupCenterX, chartHeight / 2f)
                        )
                    }
                }
            }

            // Month Labels below chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                data.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else onSurfaceColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NetWorthLineChart(
    data: List<NetWorthDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.analytics_no_networth_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(data.size - 1) }

    val minVal = remember(data) { data.minOfOrNull { it.netWorth } ?: 0L }
    val maxVal = remember(data) { data.maxOfOrNull { it.netWorth } ?: 0L }
    val range = remember(minVal, maxVal) { (maxVal - minVal).coerceAtLeast(100_000L).toFloat() }

    val lineColor = MaterialTheme.colorScheme.primary
    val gradientBottomColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
    val gradientTopColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected Value Banner
        selectedIndex?.let { idx ->
            if (idx in data.indices) {
                val item = data[idx]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.analytics_networth_month, item.label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatRupiah(item.netWorth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = stringResource(R.string.analytics_tap_point_detail),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Custom Canvas Line Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                            val clickedIndex = ((offset.x + stepX / 2f) / stepX).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = clickedIndex
                        }
                    }
            ) {
                val chartHeight = size.height - 24.dp.toPx()
                val stepX = size.width / (data.size - 1).coerceAtLeast(1)

                // Grid lines
                for (i in 0..2) {
                    val y = chartHeight * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (data.size > 1) {
                    val path = Path()
                    val fillPath = Path()

                    val points = data.mapIndexed { index, point ->
                        val x = index * stepX
                        val normalized = ((point.netWorth - minVal).toFloat() / range).coerceIn(0f, 1f)
                        val y = chartHeight - (normalized * (chartHeight * 0.75f) + chartHeight * 0.12f)
                        Offset(x, y)
                    }

                    // Build smooth cubic bezier curve
                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, chartHeight)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(points.last().x, chartHeight)
                    fillPath.close()

                    // Draw Gradient Fill under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientTopColor, gradientBottomColor),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw Main Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Dots on each data point
                    points.forEachIndexed { index, point ->
                        val isSelected = index == selectedIndex
                        if (isSelected) {
                            drawCircle(
                                color = lineColor.copy(alpha = 0.25f),
                                radius = 10.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 5.5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = point
                            )
                        } else {
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // Month Labels below chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                data.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else onSurfaceColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
