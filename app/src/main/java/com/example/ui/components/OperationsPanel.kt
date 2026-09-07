package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BudgetCategory
import com.example.ui.OperationsTab
import com.example.ui.SavingsInputMode
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppBorderLight
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.ColorExpenseLight
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.ColorSavings
import com.example.ui.theme.ColorSavingsLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationsPanel(
    activeTab: OperationsTab,
    onTabSelected: (OperationsTab) -> Unit,
    currentIncome: Double,
    currentSavings: Double,
    onAddExpense: (BudgetCategory, Double, String) -> Unit,
    onUpdateIncome: (Double) -> Unit,
    onSaveSavings: (Double) -> Unit,
    savingsMode: SavingsInputMode,
    onSavingsModeChange: (SavingsInputMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurfaceGlass)
            .border(1.dp, AppBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Tab Switcher (Immersive UI flex gap-2 mb-4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab 1: + Dodaj Wydatek
            val isExpenseTab = activeTab == OperationsTab.ADD_EXPENSE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isExpenseTab) ColorExpense.copy(alpha = 0.15f)
                        else Slate800.copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (isExpenseTab) ColorExpense.copy(alpha = 0.45f)
                        else AppBorderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(OperationsTab.ADD_EXPENSE)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Dodaj Wydatek",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = if (isExpenseTab) ColorExpenseLight else Slate400
                )
            }

            // Tab 2: Wypłata & Cele
            val isIncomeTab = activeTab == OperationsTab.INCOME_AND_GOALS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isIncomeTab) ColorSavings.copy(alpha = 0.15f)
                        else Slate800.copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (isIncomeTab) ColorSavings.copy(alpha = 0.45f)
                        else AppBorderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(OperationsTab.INCOME_AND_GOALS)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wypłata & Cele",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = if (isIncomeTab) ColorSavingsLight else Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == OperationsTab.ADD_EXPENSE) {
            AddExpenseTabContent(
                onAddExpense = onAddExpense
            )
        } else {
            IncomeAndGoalsTabContent(
                currentIncome = currentIncome,
                currentSavings = currentSavings,
                savingsMode = savingsMode,
                onSavingsModeChange = onSavingsModeChange,
                onUpdateIncome = onUpdateIncome,
                onSaveSavings = onSaveSavings
            )
        }
    }
}

@Composable
private fun AddExpenseTabContent(
    onAddExpense: (BudgetCategory, Double, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf(BudgetCategory.JEDZENIE) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    fun submitExpense() {
        val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (amount > 0.0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onAddExpense(selectedCategory, amount, noteText.trim())
            amountText = ""
            noteText = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row with Category Picker & Amount Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Dropdown Button
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppSurfaceElevated)
                    .border(1.dp, selectedCategory.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isCategoryDropdownOpen = true
                    }
                    .padding(horizontal = 10.dp, vertical = 13.dp)
                    .testTag("category_dropdown_trigger")
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(selectedCategory.color)
                        )
                        Text(
                            text = selectedCategory.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Rozwiń listę kategorii",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = isCategoryDropdownOpen,
                    onDismissRequest = { isCategoryDropdownOpen = false },
                    modifier = Modifier
                        .background(AppSurfaceElevated)
                        .border(1.dp, AppBorder, RoundedCornerShape(12.dp))
                ) {
                    BudgetCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = cat.displayName,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedCategory = cat
                                isCategoryDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 8) {
                        amountText = input
                    }
                },
                placeholder = { Text("Kwota zł", color = TextMuted, fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submitExpense() }),
                modifier = Modifier
                    .weight(1.05f)
                    .testTag("input_expense_amount"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorExpense,
                    unfocusedBorderColor = AppBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ColorExpense
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Submit Button
            Button(
                onClick = { submitExpense() },
                enabled = (amountText.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorExpense,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("btn_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Dodaj", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Quick add amount chips for one-hand convenience
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(20, 50, 100, 200).forEach { quickVal ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppSurfaceElevated)
                        .border(1.dp, AppBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val current = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val newVal = current + quickVal
                            amountText = if (newVal % 1.0 == 0.0) {
                                newVal.toInt().toString()
                            } else {
                                String.format(Locale.US, "%.2f", newVal)
                            }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$quickVal zł",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeAndGoalsTabContent(
    currentIncome: Double,
    currentSavings: Double,
    savingsMode: SavingsInputMode,
    onSavingsModeChange: (SavingsInputMode) -> Unit,
    onUpdateIncome: (Double) -> Unit,
    onSaveSavings: (Double) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var incomeInput by remember(currentIncome) {
        mutableStateOf(if (currentIncome > 0) currentIncome.toInt().toString() else "")
    }
    var savingsInput by remember { mutableStateOf("") }
    var showIncomeSavedMessage by remember { mutableStateOf(false) }
    var showSavingsSavedMessage by remember { mutableStateOf(false) }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,##0", symbols)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section 1: Zarobki (Wypłata)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurfaceElevated)
                .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = ColorIncome,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pensja / Zarobki",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Aktualnie: ${currencyFormatter.format(currentIncome)} zł",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorIncome
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 8) {
                            incomeInput = input
                            showIncomeSavedMessage = false
                        }
                    },
                    placeholder = { Text("Wpisz pensję zł", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_income_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorIncome,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ColorIncome
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val amount = incomeInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (amount >= 0.0) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateIncome(amount)
                            showIncomeSavedMessage = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorIncome,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(52.dp).testTag("btn_save_income")
                ) {
                    Text("Zapisz", fontWeight = FontWeight.Bold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (showIncomeSavedMessage) {
                        "✓ Zapisano i ustawiono jako domyślną dla nowych miesięcy!"
                    } else {
                        "Pamięć kwoty: ta wartość będzie automatycznie proponowana w kolejnych miesiącach."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (showIncomeSavedMessage) ColorIncome else TextMuted
                )
            }
        }

        // Section 2: Oszczędności (Skarbonka)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurfaceElevated)
                .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = ColorSavings,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Oszczędności (Skarbonka)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Odłożone: ${currencyFormatter.format(currentSavings)} zł",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorSavings
                )
            }

            // Mode Selector: "Ustaw" vs "+ Dopłać"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppSurface)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // "Ustaw"
                val isSet = savingsMode == SavingsInputMode.SET
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSet) ColorSavings else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSavingsModeChange(SavingsInputMode.SET)
                            showSavingsSavedMessage = false
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ustaw kwotę",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSet) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSet) Color.White else TextSecondary
                    )
                }

                // "+ Dopłać"
                val isAdd = savingsMode == SavingsInputMode.ADD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAdd) ColorSavings else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSavingsModeChange(SavingsInputMode.ADD)
                            showSavingsSavedMessage = false
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Dopłać do skarbonki",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isAdd) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isAdd) Color.White else TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = savingsInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 8) {
                            savingsInput = input
                            showSavingsSavedMessage = false
                        }
                    },
                    placeholder = {
                        Text(
                            if (savingsMode == SavingsInputMode.SET) "Kwota na sztywno zł" else "Dopłata zł",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_savings_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorSavings,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ColorSavings
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val amount = savingsInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (amount > 0.0) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSaveSavings(amount)
                            savingsInput = ""
                            showSavingsSavedMessage = true
                        }
                    },
                    enabled = (savingsInput.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0.0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSavings,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(52.dp).testTag("btn_save_savings")
                ) {
                    Text(
                        text = if (savingsMode == SavingsInputMode.SET) "Ustal" else "+ Dorzuć",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (savingsMode == SavingsInputMode.SET) {
                    "Świadoma deklaracja: ustala dokładnie kwotę odłożoną w tym miesiącu."
                } else {
                    "Stopniowe oszczędzanie: wpisana kwota zostanie dodana do obecnych oszczędności."
                },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted
            )
        }
    }
}
