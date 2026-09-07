package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BudgetDatabase
import com.example.data.BudgetRepository
import com.example.model.BudgetCategory
import com.example.model.CategorySummary
import com.example.model.MonthKey
import com.example.model.MonthOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OperationsTab {
    ADD_EXPENSE,
    INCOME_AND_GOALS
}

enum class SavingsInputMode {
    SET, // Ustal kwotę na sztywno
    ADD  // + Dopłać
}

data class BudgetUiState(
    val selectedMonth: MonthKey = MonthKey.current(),
    val isCurrentMonth: Boolean = true,
    val income: Double = 0.0,
    val savings: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val freeFunds: Double = 0.0,
    val isDeficit: Boolean = false,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val recentMonths: List<MonthOverview> = emptyList(),
    val activeTab: OperationsTab = OperationsTab.ADD_EXPENSE,
    val availableMonths: List<MonthKey> = emptyList(),
    val isLoading: Boolean = false
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    private val _selectedMonth = MutableStateFlow(MonthKey.current())
    val selectedMonth: StateFlow<MonthKey> = _selectedMonth.asStateFlow()

    private val _activeTab = MutableStateFlow(OperationsTab.ADD_EXPENSE)
    val activeTab: StateFlow<OperationsTab> = _activeTab.asStateFlow()

    private val _savingsMode = MutableStateFlow(SavingsInputMode.SET)
    val savingsMode: StateFlow<SavingsInputMode> = _savingsMode.asStateFlow()

    val uiState: StateFlow<BudgetUiState>

    init {
        val db = BudgetDatabase.getDatabase(application)
        repository = BudgetRepository(db.budgetDao())

        val currentMonth = MonthKey.current()

        // Generate dropdown months: 12 months back and 6 months forward
        val available = mutableListOf<MonthKey>()
        var cur = currentMonth
        for (i in 1..12) {
            cur = cur.previous()
        }
        for (i in 0..18) {
            available.add(cur)
            cur = cur.next()
        }

        // Initialize current month in DB
        viewModelScope.launch {
            repository.ensureMonthBudget(currentMonth.keyString)
        }

        uiState = _selectedMonth.flatMapLatest { month ->
            combine(
                repository.getMonthBudget(month.keyString),
                repository.getExpensesForMonth(month.keyString),
                repository.getAllMonthsOverviewFlow(),
                _activeTab
            ) { budget, expenses, allOverviews, tab ->
                val income = budget?.income ?: 0.0
                val savings = budget?.savings ?: 0.0
                val totalExp = expenses.sumOf { it.amount }
                val free = income - totalExp - savings

                val categories = BudgetCategory.entries.map { cat ->
                    val catTotal = expenses.filter { it.category == cat.id }.sumOf { it.amount }
                    val pct = if (totalExp > 0.0) (catTotal / totalExp).toFloat() else 0f
                    CategorySummary(cat, catTotal, pct)
                }

                // Recent months for bottom preview (last 3-4 months)
                val recent = allOverviews.filter { it.monthKey <= currentMonth }.take(4)

                BudgetUiState(
                    selectedMonth = month,
                    isCurrentMonth = month == currentMonth,
                    income = income,
                    savings = savings,
                    totalExpenses = totalExp,
                    freeFunds = free,
                    isDeficit = free < 0.0,
                    categorySummaries = categories,
                    recentMonths = recent,
                    activeTab = tab,
                    availableMonths = available,
                    isLoading = false
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetUiState(
                selectedMonth = currentMonth,
                isCurrentMonth = true,
                availableMonths = available
            )
        )
    }

    fun selectMonth(monthKey: MonthKey) {
        _selectedMonth.value = monthKey
        viewModelScope.launch {
            repository.ensureMonthBudget(monthKey.keyString)
        }
    }

    fun nextMonth() {
        val next = _selectedMonth.value.next()
        selectMonth(next)
    }

    fun previousMonth() {
        val prev = _selectedMonth.value.previous()
        selectMonth(prev)
    }

    fun jumpToToday() {
        selectMonth(MonthKey.current())
    }

    fun setActiveTab(tab: OperationsTab) {
        _activeTab.value = tab
    }

    fun setSavingsMode(mode: SavingsInputMode) {
        _savingsMode.value = mode
    }

    fun addExpense(category: BudgetCategory, amount: Double, note: String = "") {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(_selectedMonth.value.keyString, category.id, amount, note)
        }
    }

    fun resetCategory(category: BudgetCategory) {
        viewModelScope.launch {
            repository.resetCategory(_selectedMonth.value.keyString, category.id)
        }
    }

    fun updateIncome(newIncome: Double) {
        if (newIncome < 0.0) return
        viewModelScope.launch {
            repository.updateIncome(_selectedMonth.value.keyString, newIncome)
        }
    }

    fun saveSavings(amount: Double) {
        if (amount < 0.0) return
        viewModelScope.launch {
            if (_savingsMode.value == SavingsInputMode.SET) {
                repository.setSavings(_selectedMonth.value.keyString, amount)
            } else {
                repository.addSavings(_selectedMonth.value.keyString, amount)
            }
        }
    }
}
