package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MonthKey
import com.example.model.MonthOverview
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorExpenseLight
import com.example.ui.theme.ColorSavingsLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun RecentMonthsTrend(
    recentMonths: List<MonthOverview>,
    currentSelectedMonth: MonthKey,
    onSelectMonth: (MonthKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,##0", symbols)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppSurfaceGlass)
            .border(1.dp, AppBorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "HISTORIA I TREND",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 9.sp
            ),
            color = Slate500
        )

        if (recentMonths.isEmpty()) {
            Text(
                text = "Brak wcześniejszej historii. Dane pojawią się wraz z kolejnymi miesiącami.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            recentMonths.forEach { item ->
                val isSelected = item.monthKey == currentSelectedMonth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Slate800.copy(alpha = 0.6f)
                            else Slate800.copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) ColorSavingsLight.copy(alpha = 0.4f)
                            else AppBorderSubtle,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectMonth(item.monthKey)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("trend_month_${item.monthKey.keyString}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.monthKey.displayName(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) Slate200 else Slate400
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Koszty: ${currencyFormatter.format(item.totalExpenses)} zł",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Slate200
                            )

                            Text(
                                text = "+${currencyFormatter.format(item.savings)} zł",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ColorSavingsLight
                            )
                        }
                    }
                }
            }
        }
    }
}

