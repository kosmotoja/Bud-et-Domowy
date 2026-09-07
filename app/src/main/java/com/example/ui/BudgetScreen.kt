package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BackupDialog
import com.example.ui.components.CalendarHeader
import com.example.ui.components.CategoryLimitsDialog
import com.example.ui.components.CategoryProgressList
import com.example.ui.components.DonutChart
import com.example.ui.components.MetricCardsGrid
import com.example.ui.components.OperationsPanel
import com.example.ui.components.RecentMonthsTrend
import com.example.ui.components.RecurringExpensesDialog
import com.example.ui.components.SavingsGoalsDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TrendsChartDialog
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorderSubtle
import com.example.ui.theme.ColorFreeFunds
import com.example.ui.theme.ColorIncome
import com.example.ui.theme.Slate800
import com.example.ui.theme.TextPrimary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val savingsMode by viewModel.savingsMode.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val currencyFormatter = remember {
        val symbols = DecimalFormatSymbols(Locale.GERMAN).apply { groupingSeparator = ' ' }
        DecimalFormat("#,##0", symbols)
    }

    // React to snackbar notifications
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Calendar & App Header with feature icons
                CalendarHeader(
                    selectedMonth = uiState.selectedMonth,
                    isCurrentMonth = uiState.isCurrentMonth,
                    availableMonths = uiState.availableMonths,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onJumpToToday = { viewModel.jumpToToday() },
                    onOpenTrends = { viewModel.openModal(ActiveModal.HISTORY) },
                    onOpenRecurring = { viewModel.openModal(ActiveModal.RECURRING) },
                    onOpenGoals = { viewModel.openModal(ActiveModal.GOALS) },
                    onOpenSettings = { viewModel.openModal(ActiveModal.SETTINGS) }
                )

                // 2. Unbooked Recurring Expenses Banner (Feature 2)
                if (uiState.unbookedRecurringInSelectedMonth.isNotEmpty()) {
                    val count = uiState.unbookedRecurringInSelectedMonth.size
                    val sum = uiState.unbookedRecurringInSelectedMonth.sumOf { it.amount }
                    val firstName = uiState.unbookedRecurringInSelectedMonth.first().name

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800.copy(alpha = 0.85f))
                            .border(1.dp, ColorFreeFunds.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = ColorFreeFunds,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Stałe opłaty za ten miesiąc ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "np. $firstName • Razem: ${currencyFormatter.format(sum)} zł",
                                        fontSize = 10.sp,
                                        color = ColorFreeFunds
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.bookAllUnbookedRecurring() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorFreeFunds),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Black)
                                        Text("Księguj", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. 2x2 Metric Cards Grid (supports free funds carry-over)
                MetricCardsGrid(
                    income = uiState.income,
                    totalExpenses = uiState.totalExpenses,
                    savings = uiState.savings,
                    freeFunds = uiState.freeFunds,
                    effectiveFreeFunds = uiState.effectiveFreeFunds,
                    rolloverAmount = uiState.rolloverAmount,
                    isRolloverEnabled = uiState.isRolloverEnabled,
                    isDeficit = uiState.isDeficit,
                    onEditIncome = {
                        viewModel.setActiveTab(OperationsTab.INCOME_AND_GOALS)
                    },
                    onAddExpense = {
                        viewModel.setActiveTab(OperationsTab.ADD_EXPENSE)
                    },
                    onEditSavings = {
                        viewModel.setActiveTab(OperationsTab.INCOME_AND_GOALS)
                    }
                )

                // 4. Multi-color Donut Chart
                DonutChart(
                    categorySummaries = uiState.categorySummaries,
                    totalExpenses = uiState.totalExpenses,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 5. Two-tab Operations Panel (Add Expense / Salary & Savings)
                OperationsPanel(
                    activeTab = uiState.activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) },
                    currentIncome = uiState.income,
                    currentSavings = uiState.savings,
                    onAddExpense = { cat, amt, note ->
                        viewModel.addExpense(cat, amt, note)
                    },
                    onUpdateIncome = { newIncome ->
                        viewModel.updateIncome(newIncome)
                    },
                    onSaveSavings = { amt ->
                        viewModel.saveSavings(amt)
                    },
                    savingsMode = savingsMode,
                    onSavingsModeChange = { viewModel.setSavingsMode(it) }
                )

                // 6. Category Breakdown with Mini Progress Bars & Limits
                CategoryProgressList(
                    categorySummaries = uiState.categorySummaries,
                    totalExpenses = uiState.totalExpenses,
                    onResetCategory = { cat ->
                        viewModel.resetCategory(cat)
                    },
                    onManageLimits = {
                        viewModel.openModal(ActiveModal.LIMITS)
                    }
                )

                // 7. Recent Months Trend & Quick Jump
                RecentMonthsTrend(
                    recentMonths = uiState.recentMonths,
                    currentSelectedMonth = uiState.selectedMonth,
                    onSelectMonth = { viewModel.selectMonth(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // MODALS & DIALOGS
            when (uiState.activeModal) {
                null -> Unit
                ActiveModal.RECURRING -> {
                    RecurringExpensesDialog(
                        recurringExpenses = uiState.recurringExpenses,
                        currentMonthKey = uiState.selectedMonth.toString(),
                        onDismiss = { viewModel.closeModal() },
                        onAddRecurring = { name, amount, category, day ->
                            viewModel.addRecurringExpense(name, amount, category, day)
                        },
                        onDeleteRecurring = { id ->
                            viewModel.deleteRecurringExpense(id)
                        },
                        onBookExpenses = { expenses ->
                            viewModel.bookRecurringExpenses(expenses)
                        }
                    )
                }
                ActiveModal.LIMITS -> {
                    CategoryLimitsDialog(
                        currentLimits = uiState.categoryLimits,
                        onDismiss = { viewModel.closeModal() },
                        onSaveLimit = { category, limit ->
                            viewModel.saveCategoryLimit(category, limit)
                        }
                    )
                }
                ActiveModal.GOALS -> {
                    SavingsGoalsDialog(
                        goals = uiState.savingsGoals,
                        onDismiss = { viewModel.closeModal() },
                        onAddGoal = { name, target, initial ->
                            viewModel.addSavingsGoal(name, target, initial)
                        },
                        onDepositToGoal = { goalId, amount ->
                            viewModel.depositToSavingsGoal(goalId, amount)
                        },
                        onDeleteGoal = { goalId ->
                            viewModel.deleteSavingsGoal(goalId)
                        }
                    )
                }
                ActiveModal.HISTORY -> {
                    TrendsChartDialog(
                        historyPoints = uiState.historyPoints,
                        onDismiss = { viewModel.closeModal() }
                    )
                }
                ActiveModal.BACKUP -> {
                    BackupDialog(
                        onDismiss = { viewModel.closeModal() },
                        onExportJson = { viewModel.exportBackupJson() },
                        onExportCsv = { viewModel.exportExpensesCsv() },
                        onImportJson = { json, replace -> viewModel.importBackupJson(json, replace) }
                    )
                }
                ActiveModal.SETTINGS -> {
                    SettingsDialog(
                        isBiometricEnabled = uiState.isBiometricEnabled,
                        isRolloverEnabled = uiState.isRolloverEnabled,
                        onToggleBiometric = { viewModel.setBiometricEnabled(it) },
                        onToggleRollover = { viewModel.setRolloverEnabled(it) },
                        onOpenBackup = { viewModel.openModal(ActiveModal.BACKUP) },
                        onDismiss = { viewModel.closeModal() }
                    )
                }
            }
        }
    }
}
