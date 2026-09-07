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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.SavingsGoalEntity
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.AppSurfaceElevated
import com.example.ui.theme.AppSurfaceGlass
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorSavings
import com.example.ui.theme.ColorSavingsLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun SavingsGoalsDialog(
    goals: List<SavingsGoalEntity>,
    onDismiss: () -> Unit,
    onAddGoal: (name: String, targetAmount: Double, initialAmount: Double) -> Unit,
    onDepositToGoal: (goalId: Long, amount: Double) -> Unit,
    onDeleteGoal: (goalId: Long) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var depositingGoalId by remember { mutableStateOf<Long?>(null) }
    var depositAmountInput by remember { mutableStateOf("") }

    var newGoalName by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalInitial by remember { mutableStateOf("") }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply { groupingSeparator = ' ' }
        DecimalFormat("#,##0", symbols)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = ColorSavingsLight,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Skarbonki Celowe",
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
                    text = "Śledź swoje marzenia i cele finansowe (np. Wakacje, Poduszka Finansowa, Nowy Auto). Wpłacaj środki i obserwuj pasek postępu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                // Deposit mini-dialog
                depositingGoalId?.let { goalId ->
                    val goal = goals.firstOrNull { it.id == goalId }
                    if (goal != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceGlass)
                                .border(1.dp, ColorSavings.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Wpłać do: ${goal.name}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            // Quick deposit buttons: +50, +100, +200, +500
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(50.0, 100.0, 200.0, 500.0).forEach { quickAmt ->
                                    Button(
                                        onClick = {
                                            onDepositToGoal(goal.id, quickAmt)
                                            depositingGoalId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+${quickAmt.toInt()}", fontSize = 11.sp, color = ColorSavingsLight)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = depositAmountInput,
                                    onValueChange = { depositAmountInput = it.replace(',', '.') },
                                    label = { Text("Inna kwota (zł)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = ColorSavings,
                                        unfocusedBorderColor = AppBorderSubtle
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val amt = depositAmountInput.toDoubleOrNull() ?: 0.0
                                        if (amt > 0.0) {
                                            onDepositToGoal(goal.id, amt)
                                            depositAmountInput = ""
                                            depositingGoalId = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorSavings)
                                ) {
                                    Text("Wpłać", color = Color.White)
                                }
                            }

                            TextButton(
                                onClick = { depositingGoalId = null },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Anuluj", color = Slate400, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (showAddForm) {
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
                            text = "Nowy Cel Oszczędnościowy",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = newGoalName,
                            onValueChange = { newGoalName = it },
                            label = { Text("Nazwa celu (np. Wakacje w Grecji)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ColorSavings,
                                unfocusedBorderColor = AppBorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newGoalTarget,
                                onValueChange = { newGoalTarget = it.replace(',', '.') },
                                label = { Text("Cel kwotowy (zł)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ColorSavings,
                                    unfocusedBorderColor = AppBorderSubtle
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = newGoalInitial,
                                onValueChange = { newGoalInitial = it.replace(',', '.') },
                                label = { Text("Na start (zł)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ColorSavings,
                                    unfocusedBorderColor = AppBorderSubtle
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddForm = false }) {
                                Text("Anuluj", color = Slate400)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    val target = newGoalTarget.toDoubleOrNull() ?: 0.0
                                    val initial = newGoalInitial.toDoubleOrNull() ?: 0.0
                                    if (newGoalName.isNotBlank() && target > 0.0) {
                                        onAddGoal(newGoalName, target, initial)
                                        newGoalName = ""
                                        newGoalTarget = ""
                                        newGoalInitial = ""
                                        showAddForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSavings)
                            ) {
                                Text("Dodaj cel", color = Color.White)
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
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorSavingsLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Nowy cel oszczędnościowy", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                if (goals.isEmpty()) {
                    Text(
                        text = "Nie masz jeszcze żadnych celów. Utwórz swój pierwszy cel!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(goals, key = { it.id }) { goal ->
                            val progress = if (goal.targetAmount > 0.0) {
                                (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                            } else 0f
                            val isCompleted = goal.currentAmount >= goal.targetAmount

                            val animatedProgress by animateFloatAsState(
                                targetValue = progress,
                                animationSpec = tween(durationMillis = 600),
                                label = "goalProgress"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppSurfaceGlass)
                                    .border(1.dp, if (isCompleted) ColorFreeFunds.copy(alpha = 0.4f) else AppBorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Celebration,
                                                contentDescription = "Osiągnięto!",
                                                tint = ColorFreeFunds,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = goal.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { depositingGoalId = goal.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = ColorSavings.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("+ Wpłać", fontSize = 10.sp, color = ColorSavingsLight)
                                        }

                                        IconButton(
                                            onClick = { onDeleteGoal(goal.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Usuń",
                                                tint = Slate500,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                // Progress row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${currencyFormatter.format(goal.currentAmount)} / ${currencyFormatter.format(goal.targetAmount)} zł",
                                        fontSize = 11.sp,
                                        color = if (isCompleted) ColorFreeFunds else Slate400
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) ColorFreeFunds else ColorSavingsLight
                                    )
                                }

                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Slate800)
                                ) {
                                    if (animatedProgress > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(animatedProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (isCompleted) ColorFreeFunds else ColorSavings)
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
