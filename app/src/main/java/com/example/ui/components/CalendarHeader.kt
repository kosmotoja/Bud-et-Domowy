package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorFreeFundsLight
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary

@Composable
fun CalendarHeader(
    selectedMonth: MonthKey,
    isCurrentMonth: Boolean,
    availableMonths: List<MonthKey>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (MonthKey) -> Unit,
    onJumpToToday: () -> Unit,
    onOpenTrends: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BUDŻET DOMOWY",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    fontSize = 12.sp
                ),
                color = Slate400
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Trends / History
                IconButton(
                    onClick = onOpenTrends,
                    modifier = Modifier.size(32.dp).testTag("btn_open_trends")
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = "Trendy", tint = Slate400, modifier = Modifier.size(17.dp))
                }

                // Recurring expenses
                IconButton(
                    onClick = onOpenRecurring,
                    modifier = Modifier.size(32.dp).testTag("btn_open_recurring")
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = "Stałe opłaty", tint = Slate400, modifier = Modifier.size(17.dp))
                }

                // Savings goals
                IconButton(
                    onClick = onOpenGoals,
                    modifier = Modifier.size(32.dp).testTag("btn_open_goals")
                ) {
                    Icon(Icons.Default.Savings, contentDescription = "Cele", tint = Slate400, modifier = Modifier.size(17.dp))
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(32.dp).testTag("btn_open_settings")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Ustawienia", tint = Slate400, modifier = Modifier.size(17.dp))
                }

                // [Dzisiaj] button with Immersive UI pill styling
                if (!isCurrentMonth) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Slate800.copy(alpha = 0.5f))
                            .border(1.dp, ColorFreeFunds.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onJumpToToday()
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("button_today")
                    ) {
                        Text(
                            text = "DZIŚ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp
                            ),
                            color = ColorFreeFundsLight
                        )
                    }
                }
            }
        }

        // Calendar Bar (Immersive rounded card with subtle border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppSurface)
                .border(1.dp, AppBorderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Month [❮]
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPreviousMonth()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Poprzedni miesiąc",
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Month Selector Dropdown
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isDropdownExpanded = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("month_selector_dropdown")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedMonth.displayName(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "▾",
                            fontSize = 10.sp,
                            color = Slate500
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(AppSurfaceElevated)
                            .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
                    ) {
                        availableMonths.forEach { month ->
                            val isCurrentLoop = month == MonthKey.current()
                            val isSelected = month == selectedMonth

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = month.displayName(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) ColorIncome else TextPrimary
                                        )
                                        if (isCurrentLoop) {
                                            Text(
                                                text = "(Bieżący)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ColorFreeFunds
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectMonth(month)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Next Month [❯]
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNextMonth()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Następny miesiąc",
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

