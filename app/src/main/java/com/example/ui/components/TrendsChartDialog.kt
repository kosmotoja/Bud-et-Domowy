package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MonthlyHistoryPoint
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun TrendsChartDialog(
    historyPoints: List<MonthlyHistoryPoint>,
    onDismiss: () -> Unit
) {
    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply { groupingSeparator = ' ' }
        DecimalFormat("#,##0", symbols)
    }

    val avgExpenses = if (historyPoints.isNotEmpty()) {
        historyPoints.map { it.expenses }.average()
    } else 0.0

    val avgIncome = if (historyPoints.isNotEmpty()) {
        historyPoints.map { it.income }.average()
    } else 0.0

    val avgSavings = if (historyPoints.isNotEmpty()) {
        historyPoints.map { it.savings }.average()
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = ColorIncome,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Trendy Finansowe (6 miesięcy)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = ColorIncome, label = "Przychody")
                    LegendItem(color = ColorExpense, label = "Wydatki")
                    LegendItem(color = ColorFreeFunds, label = "Wolne środki")
                }

                // Canvas Line Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppSurfaceGlass)
                        .border(1.dp, AppBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    if (historyPoints.isNotEmpty()) {
                        HistoryLineChart(points = historyPoints)
                    } else {
                        Text(
                            text = "Brak danych historycznych.",
                            color = Slate500,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Month labels below chart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    historyPoints.forEach { pt ->
                        val shortMonth = pt.monthKey.displayName().take(3)
                        Text(
                            text = shortMonth,
                            fontSize = 10.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stat summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatSummaryTile(
                        title = "Śr. wydatki",
                        value = "${currencyFormatter.format(avgExpenses)} zł",
                        color = ColorExpense,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryTile(
                        title = "Śr. przychód",
                        value = "${currencyFormatter.format(avgIncome)} zł",
                        color = ColorIncome,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryTile(
                        title = "Śr. oszczędności",
                        value = "${currencyFormatter.format(avgSavings)} zł",
                        color = ColorFreeFunds,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij", color = TextPrimary)
            }
        },
        containerColor = AppSurfaceElevated
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 10.sp, color = Slate400)
    }
}

@Composable
private fun StatSummaryTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Column {
            Text(text = title, fontSize = 9.sp, color = Slate500)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun HistoryLineChart(
    points: List<MonthlyHistoryPoint>
) {
    val maxVal = remember(points) {
        val maxIncomes = points.maxOfOrNull { it.income } ?: 1000.0
        val maxExps = points.maxOfOrNull { it.expenses } ?: 1000.0
        val maxFree = points.maxOfOrNull { it.freeFunds } ?: 1000.0
        maxOf(maxIncomes, maxExps, maxFree, 1000.0) * 1.15
    }

    val minVal = remember(points) {
        val minFree = points.minOfOrNull { it.freeFunds } ?: 0.0
        minOf(minFree, 0.0)
    }

    val range = (maxVal - minVal).coerceAtLeast(1.0)

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val width = size.width
        val height = size.height
        val stepX = if (points.size > 1) width / (points.size - 1) else width

        // Draw horizontal guide lines (3 dashed/subtle lines)
        val guideYValues = listOf(0.25f, 0.5f, 0.75f)
        guideYValues.forEach { fraction ->
            val y = height * fraction
            drawLine(
                color = Slate800,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        fun getY(value: Double): Float {
            val normalized = (value - minVal) / range
            return (height - (normalized * height).toFloat()).coerceIn(0f, height)
        }

        // Draw Income Line (ColorIncome)
        val incomePath = Path()
        val expensePath = Path()
        val freePath = Path()

        points.forEachIndexed { index, point ->
            val x = index * stepX
            val yInc = getY(point.income)
            val yExp = getY(point.expenses)
            val yFree = getY(point.freeFunds)

            if (index == 0) {
                incomePath.moveTo(x, yInc)
                expensePath.moveTo(x, yExp)
                freePath.moveTo(x, yFree)
            } else {
                incomePath.lineTo(x, yInc)
                expensePath.lineTo(x, yExp)
                freePath.lineTo(x, yFree)
            }
        }

        // Stroke the lines
        drawPath(incomePath, color = ColorIncome, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawPath(expensePath, color = ColorExpense, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawPath(freePath, color = ColorFreeFunds, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

        // Draw dots at each point
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val yInc = getY(point.income)
            val yExp = getY(point.expenses)
            val yFree = getY(point.freeFunds)

            drawCircle(color = ColorIncome, radius = 4.dp.toPx(), center = Offset(x, yInc))
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(x, yInc))

            drawCircle(color = ColorExpense, radius = 4.dp.toPx(), center = Offset(x, yExp))
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(x, yExp))

            drawCircle(color = ColorFreeFunds, radius = 3.5.dp.toPx(), center = Offset(x, yFree))
            drawCircle(color = Color.Black, radius = 1.5.dp.toPx(), center = Offset(x, yFree))
        }
    }
}
