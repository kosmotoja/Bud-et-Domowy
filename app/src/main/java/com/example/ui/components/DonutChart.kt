package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategorySummary
import com.example.ui.theme.AppBackground
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun DonutChart(
    categorySummaries: List<CategorySummary>,
    totalExpenses: Double,
    modifier: Modifier = Modifier,
    size: Dp = 176.dp,
    strokeWidth: Dp = 26.dp
) {
    val nonZeroCategories = remember(categorySummaries) {
        categorySummaries.filter { it.amount > 0.0 }
    }

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(totalExpenses, nonZeroCategories.size) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,##0", symbols)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokePx) / 2f
            val centerOffset = this.center

            if (totalExpenses <= 0.0 || nonZeroCategories.isEmpty()) {
                // Empty state ring
                drawCircle(
                    color = Slate800,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokePx)
                )
            } else {
                // Background subtle track
                drawCircle(
                    color = Slate800.copy(alpha = 0.5f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokePx)
                )

                var currentStartAngle = -90f
                val gapAngle = if (nonZeroCategories.size > 1) 2.5f else 0f
                val totalGap = gapAngle * nonZeroCategories.size
                val availableAngle = (360f - totalGap).coerceAtLeast(0f)

                nonZeroCategories.forEach { cat ->
                    val sweep = (cat.percentage * availableAngle) * animationProgress.value
                    if (sweep > 0.5f) {
                        drawArc(
                            color = cat.category.color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        currentStartAngle += sweep + gapAngle
                    }
                }
            }
        }

        // Inner circle backdrop: bg-[#0e101a] with centered content
        Box(
            modifier = Modifier
                .size(size - strokeWidth * 2)
                .clip(CircleShape)
                .background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SUMA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    ),
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${currencyFormatter.format(totalExpenses)} zł",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Slate100
                )
            }
        }
    }
}

