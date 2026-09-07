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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorDeficit
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorExpenseLight
import com.example.ui.theme.ColorExpenseShine
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorFreeFundsLight
import com.example.ui.theme.ColorFreeFundsShine
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorIncomeLight
import com.example.ui.theme.ColorIncomeShine
import com.example.ui.theme.ColorSavings
import com.example.ui.theme.ColorSavingsLight
import com.example.ui.theme.ColorSavingsShine
import com.example.ui.theme.Slate500
import com.example.ui.theme.TextPrimary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun MetricCardsGrid(
    income: Double,
    totalExpenses: Double,
    savings: Double,
    freeFunds: Double,
    effectiveFreeFunds: Double,
    rolloverAmount: Double,
    isRolloverEnabled: Boolean,
    isDeficit: Boolean,
    onEditIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onEditSavings: () -> Unit,
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
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Zarobki & Wydatki
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImmersiveMetricTile(
                title = "Zarobki",
                amountText = "${currencyFormatter.format(income)} zł",
                titleColor = ColorIncomeLight,
                valueColor = ColorIncomeShine,
                accentColor = ColorIncome,
                actionSymbol = "✎",
                onAction = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditIncome()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_tile_income")
            )

            ImmersiveMetricTile(
                title = "Wydatki",
                amountText = "${currencyFormatter.format(totalExpenses)} zł",
                titleColor = ColorExpenseLight,
                valueColor = ColorExpenseShine,
                accentColor = ColorExpense,
                actionSymbol = "+",
                onAction = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddExpense()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_tile_expenses")
            )
        }

        // Row 2: Oszczędności & Wolne środki
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImmersiveMetricTile(
                title = "Oszczędności",
                amountText = "${currencyFormatter.format(savings)} zł",
                titleColor = ColorSavingsLight,
                valueColor = ColorSavingsShine,
                accentColor = ColorSavings,
                actionSymbol = "✎",
                onAction = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditSavings()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_tile_savings")
            )

            val displayFunds = if (isRolloverEnabled) effectiveFreeFunds else freeFunds
            val freeFundsFormatted = if (displayFunds >= 0) {
                "+${currencyFormatter.format(displayFunds)} zł"
            } else {
                "${currencyFormatter.format(displayFunds)} zł"
            }

            val rolloverSubtitle = if (isRolloverEnabled && rolloverAmount != 0.0) {
                val sign = if (rolloverAmount > 0) "+" else ""
                "$sign${currencyFormatter.format(rolloverAmount)} zł z poprz. m-ca"
            } else null

            ImmersiveMetricTile(
                title = "Wolne środki",
                amountText = freeFundsFormatted,
                subtitle = rolloverSubtitle,
                titleColor = if (isDeficit) ColorExpenseLight else ColorFreeFundsLight,
                valueColor = if (isDeficit) ColorExpenseShine else ColorFreeFundsShine,
                accentColor = if (isDeficit) ColorDeficit else ColorFreeFunds,
                actionSymbol = if (isDeficit) "!" else null,
                onAction = null,
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_tile_free_funds")
            )
        }
    }
}

@Composable
fun ImmersiveMetricTile(
    title: String,
    amountText: String,
    titleColor: Color,
    valueColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionSymbol: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurfaceGlass)
            .border(1.dp, AppBorderSubtle, RoundedCornerShape(16.dp))
            .then(
                if (onAction != null) Modifier.clickable { onAction() } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = titleColor
                )

                if (actionSymbol != null) {
                    Text(
                        text = actionSymbol,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = valueColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Slate500
                )
            }
        }

        // Bottom Accent Line Glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(accentColor.copy(alpha = 0.35f))
        )
    }
}

