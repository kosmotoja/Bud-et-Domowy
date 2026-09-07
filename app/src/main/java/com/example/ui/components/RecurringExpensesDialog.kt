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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecurringExpenseEntity
import com.example.model.BudgetCategory
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurface
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
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun RecurringExpensesDialog(
    recurringExpenses: List<RecurringExpenseEntity>,
    currentMonthKey: String,
    onDismiss: () -> Unit,
    onAddRecurring: (name: String, amount: Double, category: String, dayOfMonth: Int) -> Unit,
    onDeleteRecurring: (Long) -> Unit,
    onBookExpenses: (List<RecurringExpenseEntity>) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BudgetCategory.CZYNSZ) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var dayInput by remember { mutableStateOf("1") }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply { groupingSeparator = ' ' }
        DecimalFormat("#,##0", symbols)
    }

    val unbookedForMonth = recurringExpenses.filter { it.lastBookedMonth != currentMonthKey }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = ColorIncome,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Stałe Opłaty i Subskrypcje",
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
                Text(
                    text = "Zarządzaj opłatami cyklicznymi (np. Czynsz, Netflix, Internet). Możesz je zaksięgować jednym kliknięciem każdego miesiąca.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                if (unbookedForMonth.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorFreeFunds.copy(alpha = 0.12f))
                            .border(1.dp, ColorFreeFunds.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Niezaksięgowane w tym m-cu:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${unbookedForMonth.size} opłat (${currencyFormatter.format(unbookedForMonth.sumOf { it.amount })} zł)",
                                    fontSize = 10.sp,
                                    color = ColorFreeFunds
                                )
                            }
                            Button(
                                onClick = { onBookExpenses(unbookedForMonth) },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorFreeFunds),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                    Text("Księguj", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }

                if (showAddForm) {
                    // Form to add new recurring
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppSurfaceGlass)
                            .border(1.dp, AppBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nowa stała opłata",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nazwa (np. Czynsz, Netflix)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ColorIncome,
                                unfocusedBorderColor = AppBorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it.replace(',', '.') },
                                label = { Text("Kwota (zł)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ColorIncome,
                                    unfocusedBorderColor = AppBorderSubtle
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = dayInput,
                                onValueChange = { dayInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Dzień m-ca") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ColorIncome,
                                    unfocusedBorderColor = AppBorderSubtle
                                ),
                                modifier = Modifier.width(90.dp)
                            )
                        }

                        // Category Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                                    .clickable { categoryDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(selectedCategory.color)
                                    )
                                    Text(
                                        text = "Kategoria: ${selectedCategory.displayName}",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(AppSurfaceElevated)
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
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(cat.color)
                                                )
                                                Text(cat.displayName, color = TextPrimary)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddForm = false }) {
                                Text("Anuluj", color = Slate400)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                                    val day = dayInput.toIntOrNull() ?: 1
                                    if (nameInput.isNotBlank() && amt > 0.0) {
                                        onAddRecurring(nameInput, amt, selectedCategory.id, day)
                                        nameInput = ""
                                        amountInput = ""
                                        showAddForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorIncome)
                            ) {
                                Text("Zapisz", color = Color.White)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showAddForm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorIncome)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Dodaj nową stałą opłatę", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // List of existing recurring
                if (recurringExpenses.isEmpty()) {
                    Text(
                        text = "Brak zdefiniowanych stałych opłat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recurringExpenses, key = { it.id }) { item ->
                            val cat = BudgetCategory.fromId(item.category)
                            val isBookedThisMonth = item.lastBookedMonth == currentMonthKey

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppSurfaceGlass)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${cat.displayName} • dzień ${item.dayOfMonth} • ${if (isBookedThisMonth) "Zaksięgowano" else "Oczekuje"}",
                                            fontSize = 9.sp,
                                            color = if (isBookedThisMonth) ColorFreeFunds else Slate500
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${currencyFormatter.format(item.amount)} zł",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )

                                    IconButton(
                                        onClick = { onDeleteRecurring(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            tint = Slate500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
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
