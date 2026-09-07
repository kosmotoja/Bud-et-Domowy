package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BudgetBackupManager
import com.example.data.BudgetDatabase
import com.example.data.BudgetRepository
import com.example.data.CategoryLimitEntity
import com.example.data.ExpenseEntity
import com.example.data.MonthBudgetEntity
import com.example.data.RecurringExpenseEntity
import com.example.data.SavingsGoalEntity
import com.example.model.BudgetCategory
import com.example.model.CategorySummary
import com.example.model.MonthKey
import com.example.model.MonthOverview
import com.example.model.MonthlyHistoryPoint
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

enum class ActiveModal {
    RECURRING,
    LIMITS,
    GOALS,
    HISTORY,
    BACKUP,
    SETTINGS
}

data class BudgetUiState(
    val selectedMonth: MonthKey = MonthKey.current(),
    val isCurrentMonth: Boolean = true,
    val income: Double = 0.0,
    val savings: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val freeFunds: Double = 0.0,
    val rolloverAmount: Double = 0.0,
    val effectiveFreeFunds: Double = 0.0,
    val isDeficit: Boolean = false,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val recentMonths: List<MonthOverview> = emptyList(),
    val activeTab: OperationsTab = OperationsTab.ADD_EXPENSE,
    val availableMonths: List<MonthKey> = emptyList(),
    val recurringExpenses: List<RecurringExpenseEntity> = emptyList(),
    val unbookedRecurringInSelectedMonth: List<RecurringExpenseEntity> = emptyList(),
    val categoryLimits: Map<String, Double> = emptyMap(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val historyPoints: List<MonthlyHistoryPoint> = emptyList(),
    val isBiometricEnabled: Boolean = false,
    val isRolloverEnabled: Boolean = true,
    val activeModal: ActiveModal? = null,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    val repository: BudgetRepository

    private val _selectedMonth = MutableStateFlow(MonthKey.current())
    val selectedMonth: StateFlow<MonthKey> = _selectedMonth.asStateFlow()

    private val _activeTab = MutableStateFlow(OperationsTab.ADD_EXPENSE)
    val activeTab: StateFlow<OperationsTab> = _activeTab.asStateFlow()

    private val _savingsMode = MutableStateFlow(SavingsInputMode.SET)
    val savingsMode: StateFlow<SavingsInputMode> = _savingsMode.asStateFlow()

    private val _activeModal = MutableStateFlow<ActiveModal?>(null)
    val activeModal: StateFlow<ActiveModal?> = _activeModal.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

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
                repository.getAllCategoryLimits(),
                repository.getAllRecurringExpenses(),
                repository.getAllSavingsGoals(),
                repository.getHistoryPoints(6),
                repository.isBiometricEnabled(),
                repository.isRolloverEnabled(),
                _activeTab,
                _activeModal,
                _snackbarMessage
            ) { args: Array<Any?> ->
                val budget = args[0] as? MonthBudgetEntity
                @Suppress("UNCHECKED_CAST")
                val expenses = (args[1] as? List<ExpenseEntity>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val allOverviews = (args[2] as? List<MonthOverview>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val limitsList = (args[3] as? List<CategoryLimitEntity>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val recurringList = (args[4] as? List<RecurringExpenseEntity>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val goalsList = (args[5] as? List<SavingsGoalEntity>).orEmpty()
                @Suppress("UNCHECKED_CAST")
                val historyPointsList = (args[6] as? List<MonthlyHistoryPoint>).orEmpty()
                val biometricEnabled = args[7] as? Boolean ?: false
                val rolloverEnabled = args[8] as? Boolean ?: true
                val tab = args[9] as? OperationsTab ?: OperationsTab.ADD_EXPENSE
                val modal = args[10] as? ActiveModal
                val snackbar = args[11] as? String

                val income = budget?.income ?: 0.0
                val savings = budget?.savings ?: 0.0
                val totalExp = expenses.sumOf { it.amount }
                val rawFree = income - totalExp - savings

                // 8. Rollover calculation from previous month
                val prevMonthKey = month.previous()
                val prevOverview = allOverviews.firstOrNull { it.monthKey == prevMonthKey }
                val prevBudget = if (prevOverview != null) {
                    // Previous month had some budget / expenses
                    allOverviews.firstOrNull { it.monthKey == prevMonthKey }
                } else null

                // Compute previous month free funds if exists
                val prevMonthExpenses = prevOverview?.totalExpenses ?: 0.0
                val prevMonthSavings = prevOverview?.savings ?: 0.0
                // We check if previous month was recorded in overviews
                val rolloverAmount = if (prevOverview != null) {
                    // Income of previous month can be found or estimated
                    val prevBudgetItem = allOverviews.firstOrNull { it.monthKey == prevMonthKey }
                    // To be precise: free = prevIncome - prevExpenses - prevSavings
                    // If we don't have prevIncome directly in overview, let's derive it or calculate
                    val estimatedPrevIncome = repository.getDefaultIncome()
                    // If there's an actual budget entity, calculate exact
                    (estimatedPrevIncome - prevMonthExpenses - prevMonthSavings).coerceAtLeast(-50000.0)
                } else {
                    0.0
                }

                val effectiveFree = if (rolloverEnabled) (rawFree + rolloverAmount) else rawFree

                val limitsMap = limitsList.associate { it.category to it.monthlyLimit }

                val categories = BudgetCategory.entries.map { cat ->
                    val catTotal = expenses.filter { it.category == cat.id }.sumOf { it.amount }
                    val pct = if (totalExp > 0.0) (catTotal / totalExp).toFloat() else 0f
                    val catLimit = limitsMap[cat.id]
                    val limitPct = if (catLimit != null && catLimit > 0.0) {
                        (catTotal / catLimit).toFloat()
                    } else null

                    CategorySummary(
                        category = cat,
                        amount = catTotal,
                        percentage = pct,
                        limit = catLimit,
                        limitPercentage = limitPct
                    )
                }

                // Recurring expenses unbooked in selected month
                val unbookedRecurring = recurringList.filter { r ->
                    r.isActive && r.lastBookedMonth != month.keyString
                }

                // Recent months for bottom preview (last 3-4 months)
                val recent = allOverviews.filter { it.monthKey <= currentMonth }.take(4)

                BudgetUiState(
                    selectedMonth = month,
                    isCurrentMonth = month == currentMonth,
                    income = income,
                    savings = savings,
                    totalExpenses = totalExp,
                    freeFunds = rawFree,
                    rolloverAmount = rolloverAmount,
                    effectiveFreeFunds = effectiveFree,
                    isDeficit = effectiveFree < 0.0,
                    categorySummaries = categories,
                    recentMonths = recent,
                    activeTab = tab,
                    availableMonths = available,
                    recurringExpenses = recurringList,
                    unbookedRecurringInSelectedMonth = unbookedRecurring,
                    categoryLimits = limitsMap,
                    savingsGoals = goalsList,
                    historyPoints = historyPointsList,
                    isBiometricEnabled = biometricEnabled,
                    isRolloverEnabled = rolloverEnabled,
                    activeModal = modal,
                    snackbarMessage = snackbar,
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

    fun openModal(modal: ActiveModal?) {
        _activeModal.value = modal
    }

    fun closeModal() {
        _activeModal.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun addExpense(category: BudgetCategory, amount: Double, note: String = "") {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addExpense(_selectedMonth.value.keyString, category.id, amount, note)
            showSnackbar("Dodano wydatek: ${category.displayName} ($amount zł)")
        }
    }

    fun resetCategory(category: BudgetCategory) {
        viewModelScope.launch {
            repository.resetCategory(_selectedMonth.value.keyString, category.id)
            showSnackbar("Wyzerowano kategorię: ${category.displayName}")
        }
    }

    fun updateIncome(newIncome: Double) {
        if (newIncome < 0.0) return
        viewModelScope.launch {
            repository.updateIncome(_selectedMonth.value.keyString, newIncome)
            showSnackbar("Zaktualizowano wypłatę: $newIncome zł")
        }
    }

    fun saveSavings(amount: Double) {
        if (amount < 0.0) return
        viewModelScope.launch {
            if (_savingsMode.value == SavingsInputMode.SET) {
                repository.setSavings(_selectedMonth.value.keyString, amount)
                showSnackbar("Ustalono oszczędności: $amount zł")
            } else {
                repository.addSavings(_selectedMonth.value.keyString, amount)
                showSnackbar("Dopłacono do oszczędności: +$amount zł")
            }
        }
    }

    // --- 2. Recurring Expenses ---
    fun addRecurringExpense(name: String, amount: Double, category: String, dayOfMonth: Int) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.addRecurringExpense(name.trim(), amount, category, dayOfMonth)
            showSnackbar("Dodano stałą opłatę: $name")
        }
    }

    fun deleteRecurringExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(id)
            showSnackbar("Usunięto stałą opłatę")
        }
    }

    fun bookRecurringExpenses(expenses: List<RecurringExpenseEntity>) {
        bookRecurringExpensesForCurrentMonth(expenses)
    }

    fun bookRecurringExpensesForCurrentMonth(expenses: List<RecurringExpenseEntity>) {
        if (expenses.isEmpty()) return
        viewModelScope.launch {
            repository.bookRecurringExpenses(_selectedMonth.value.keyString, expenses)
            showSnackbar("Zaksięgowano ${expenses.size} stałych opłat!")
        }
    }

    fun bookAllUnbookedRecurring() {
        bookRecurringExpensesForCurrentMonth(uiState.value.unbookedRecurringInSelectedMonth)
    }

    // --- 3. Category Limits ---
    fun saveCategoryLimit(category: String, limit: Double) {
        setCategoryLimit(category, limit)
    }

    fun setCategoryLimit(category: String, limit: Double) {
        viewModelScope.launch {
            repository.setCategoryLimit(category, limit)
            showSnackbar("Zapisano limit dla: $category")
        }
    }

    // --- 4. Savings Goals ---
    fun addSavingsGoal(name: String, targetAmount: Double, initialAmount: Double, iconName: String = "savings") {
        if (name.isBlank() || targetAmount <= 0.0) return
        viewModelScope.launch {
            repository.addSavingsGoal(name.trim(), targetAmount, initialAmount, iconName)
            showSnackbar("Utworzono cel: $name")
        }
    }

    fun depositToSavingsGoal(id: Long, amount: Double) {
        depositToGoal(id, amount)
    }

    fun depositToGoal(id: Long, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.depositToSavingsGoal(id, amount)
            showSnackbar("Wpłacono $amount zł do skarbonki!")
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
            showSnackbar("Usunięto cel oszczędnościowy")
        }
    }

    // --- 7 & 8. Settings & Rollover ---
    fun setBiometricEnabled(enabled: Boolean) {
        toggleBiometric(enabled)
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
            showSnackbar(if (enabled) "Włączono blokadę biometryczną" else "Wyłączono blokadę biometryczną")
        }
    }

    fun setRolloverEnabled(enabled: Boolean) {
        toggleRollover(enabled)
    }

    fun toggleRollover(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRolloverEnabled(enabled)
            showSnackbar(if (enabled) "Przenoszenie wolnych środków włączone" else "Przenoszenie wolnych środków wyłączone")
        }
    }

    // --- 1. Export / Import ---
    suspend fun exportBackupJson(): String = exportJson()

    suspend fun exportExpensesCsv(): String = exportCsv()

    suspend fun importBackupJson(jsonContent: String, replaceExisting: Boolean): Result<Int> =
        importJson(jsonContent, replaceExisting)

    suspend fun exportJson(): String {
        return BudgetBackupManager.createJsonBackup(repository.dao)
    }

    suspend fun exportCsv(): String {
        return BudgetBackupManager.createCsvExport(repository.dao)
    }

    suspend fun importJson(jsonContent: String, replaceExisting: Boolean): Result<Int> {
        val result = BudgetBackupManager.restoreFromJson(repository.dao, jsonContent, replaceExisting)
        if (result.isSuccess) {
            showSnackbar("Pomyślnie zaimportowano bazę danych!")
            selectMonth(_selectedMonth.value)
        } else {
            showSnackbar("Błąd importu: ${result.exceptionOrNull()?.localizedMessage}")
        }
        return result
    }
}

