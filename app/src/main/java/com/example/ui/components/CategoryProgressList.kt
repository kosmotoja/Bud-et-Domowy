package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BudgetCategory
import com.example.model.CategorySummary
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorDeficit
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun CategoryProgressList(
    categorySummaries: List<CategorySummary>,
    totalExpenses: Double,
    onResetCategory: (BudgetCategory) -> Unit,
    onManageLimits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var categoryToReset by remember { mutableStateOf<BudgetCategory?>(null) }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,##0", symbols)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurfaceGlass)
            .border(1.dp, AppBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "KATEGORIE WYDATKÓW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = Slate500
                )

                // Button to manage limits
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate800.copy(alpha = 0.6f))
                        .clickable { onManageLimits() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Limity",
                            tint = Slate400,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "Limity",
                            fontSize = 9.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Suma: ${currencyFormatter.format(totalExpenses)} zł",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        categorySummaries.forEach { summary ->
            CategoryRowItem(
                summary = summary,
                currencyFormatter = currencyFormatter,
                onResetClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    categoryToReset = summary.category
                }
            )
        }
    }

    // Reset confirmation dialog
    categoryToReset?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToReset = null },
            title = {
                Text(
                    text = "Wyzerować kategorię?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Czy na pewno chcesz zresetować wydatki w kategorii „${cat.displayName}” do 0 zł w wybranym miesiącu?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onResetCategory(cat)
                        categoryToReset = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorExpense,
                        contentColor = Color.White
                    )
                ) {
                    Text("Tak, wyzeruj")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToReset = null }) {
                    Text("Anuluj", color = TextSecondary)
                }
            },
            containerColor = AppSurfaceElevated
        )
    }
}

@Composable
private fun CategoryRowItem(
    summary: CategorySummary,
    currencyFormatter: DecimalFormat,
    onResetClick: () -> Unit
) {
    // If a limit is set, progress represents amount / limit; otherwise percentage of total
    val progressFraction = if (summary.limit != null && summary.limit > 0.0) {
        (summary.amount / summary.limit).toFloat().coerceIn(0f, 1f)
    } else {
        summary.percentage.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    // Limit color feedback: orange at >=80%, red at >=100%
    val barColor = when {
        summary.isOverLimit -> ColorDeficit
        summary.isNearLimit -> Color(0xFFF97316) // Amber / Orange warning
        else -> summary.category.color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("category_row_${summary.category.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dot or Warning Icon
        if (summary.isOverLimit) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Przekroczono limit",
                tint = ColorDeficit,
                modifier = Modifier.size(12.dp)
            )
        } else if (summary.isNearLimit) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Blisko limitu",
                tint = Color(0xFFF97316),
                modifier = Modifier.size(12.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(summary.category.color)
            )
        }

        // Middle: Name, Amount & Progress bar
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = summary.category.displayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (summary.isOverLimit) ColorDeficit else Slate300
                    )

                    if (summary.limit != null && summary.limit > 0.0) {
                        Text(
                            text = "(limit: ${currencyFormatter.format(summary.limit)} zł)",
                            fontSize = 9.sp,
                            color = if (summary.isOverLimit) ColorDeficit.copy(alpha = 0.8f) else Slate500
                        )
                    }
                }

                Text(
                    text = "${currencyFormatter.format(summary.amount)} zł",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (summary.isOverLimit) ColorDeficit else Slate400
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Slate800)
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }
        }

        // Cross (×) Reset Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(enabled = summary.amount > 0) { onResetClick() }
                .testTag("btn_reset_category_${summary.category.id}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (summary.amount > 0) Slate400 else Slate600
            )
        }
    }
}


