package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BudgetCategory
import com.example.model.MonthKey
import com.example.ui.components.CalendarHeader
import com.example.ui.components.CategoryProgressList
import com.example.ui.components.DonutChart
import com.example.ui.components.MetricCardsGrid
import com.example.ui.components.OperationsPanel
import com.example.ui.components.RecentMonthsTrend
import com.example.ui.theme.AppBackground

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val savingsMode by viewModel.savingsMode.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppBackground
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
                // 1. Calendar & App Header
                CalendarHeader(
                    selectedMonth = uiState.selectedMonth,
                    isCurrentMonth = uiState.isCurrentMonth,
                    availableMonths = uiState.availableMonths,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onJumpToToday = { viewModel.jumpToToday() }
                )

                // 2. 2x2 Metric Cards Grid
                MetricCardsGrid(
                    income = uiState.income,
                    totalExpenses = uiState.totalExpenses,
                    savings = uiState.savings,
                    freeFunds = uiState.freeFunds,
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

                // 3. Multi-color Donut Chart
                DonutChart(
                    categorySummaries = uiState.categorySummaries,
                    totalExpenses = uiState.totalExpenses,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 4. Two-tab Operations Panel (Add Expense / Salary & Savings)
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

                // 5. Category Breakdown with Mini Progress Bars
                CategoryProgressList(
                    categorySummaries = uiState.categorySummaries,
                    totalExpenses = uiState.totalExpenses,
                    onResetCategory = { cat ->
                        viewModel.resetCategory(cat)
                    }
                )

                // 6. Recent Months Trend & Quick Jump
                RecentMonthsTrend(
                    recentMonths = uiState.recentMonths,
                    currentSelectedMonth = uiState.selectedMonth,
                    onSelectMonth = { viewModel.selectMonth(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
