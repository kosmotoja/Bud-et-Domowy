package com.example.data

import com.example.model.MonthKey
import com.example.model.MonthOverview
import com.example.model.MonthlyHistoryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BudgetRepository(val dao: BudgetDao) {

    private val KEY_DEFAULT_INCOME = "default_income"
    private val KEY_BIOMETRIC_ENABLED = "biometric_lock_enabled"
    private val KEY_ROLLOVER_ENABLED = "rollover_free_funds_enabled"
    private val INITIAL_DEFAULT_INCOME = 6500.0 // Default income

    fun getMonthBudget(yearMonth: String): Flow<MonthBudgetEntity?> {
        return dao.getMonthBudget(yearMonth)
    }

    fun getExpensesForMonth(yearMonth: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesForMonth(yearMonth)
    }

    suspend fun getDefaultIncome(): Double {
        val setting = dao.getSetting(KEY_DEFAULT_INCOME)
        return setting?.value?.toDoubleOrNull() ?: INITIAL_DEFAULT_INCOME
    }

    suspend fun ensureMonthBudget(yearMonth: String): MonthBudgetEntity {
        val existing = dao.getMonthBudgetSync(yearMonth)
        if (existing != null) {
            return existing
        }
        val defaultIncome = getDefaultIncome()
        val newBudget = MonthBudgetEntity(
            yearMonth = yearMonth,
            income = defaultIncome,
            savings = 0.0,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateBudget(newBudget)
        return newBudget
    }

    suspend fun updateIncome(yearMonth: String, newIncome: Double) {
        val existing = dao.getMonthBudgetSync(yearMonth)
        val savings = existing?.savings ?: 0.0
        val updated = MonthBudgetEntity(
            yearMonth = yearMonth,
            income = newIncome,
            savings = savings,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateBudget(updated)
        // Store as default for future new months
        dao.insertSetting(SettingEntity(KEY_DEFAULT_INCOME, newIncome.toString()))
    }

    suspend fun setSavings(yearMonth: String, savings: Double) {
        val existing = dao.getMonthBudgetSync(yearMonth)
        val income = existing?.income ?: getDefaultIncome()
        val updated = MonthBudgetEntity(
            yearMonth = yearMonth,
            income = income,
            savings = savings.coerceAtLeast(0.0),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateBudget(updated)
    }

    suspend fun addSavings(yearMonth: String, additional: Double) {
        val existing = dao.getMonthBudgetSync(yearMonth)
        val income = existing?.income ?: getDefaultIncome()
        val currentSavings = existing?.savings ?: 0.0
        val updated = MonthBudgetEntity(
            yearMonth = yearMonth,
            income = income,
            savings = (currentSavings + additional).coerceAtLeast(0.0),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateBudget(updated)
    }

    suspend fun addExpense(yearMonth: String, category: String, amount: Double, note: String = "") {
        if (amount <= 0.0) return
        // Ensure month budget entry exists
        ensureMonthBudget(yearMonth)
        val expense = ExpenseEntity(
            yearMonth = yearMonth,
            category = category,
            amount = amount,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        dao.insertExpense(expense)
    }

    suspend fun resetCategory(yearMonth: String, category: String) {
        dao.deleteExpensesByCategory(yearMonth, category)
    }

    fun getAllMonthsOverviewFlow(): Flow<List<MonthOverview>> {
        return combine(dao.getAllBudgets(), dao.getAllExpenses()) { budgets, expenses ->
            val expensesByMonth = expenses.groupBy { it.yearMonth }
            val allMonthKeys = (budgets.map { it.yearMonth } + expensesByMonth.keys).distinct()

            allMonthKeys.map { key ->
                val monthKey = MonthKey.fromKey(key)
                val budget = budgets.firstOrNull { it.yearMonth == key }
                val monthExpenses = expensesByMonth[key].orEmpty()
                val totalExp = monthExpenses.sumOf { it.amount }
                val savings = budget?.savings ?: 0.0
                MonthOverview(monthKey, totalExp, savings)
            }.sortedByDescending { it.monthKey }
        }
    }

    // --- Recurring Expenses (Stałe Opłaty / Subskrypcje) ---
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>> =
        dao.getAllRecurringExpenses()

    suspend fun addRecurringExpense(
        name: String,
        amount: Double,
        category: String,
        dayOfMonth: Int
    ): Long {
        return dao.insertRecurringExpense(
            RecurringExpenseEntity(
                name = name,
                amount = amount,
                category = category,
                dayOfMonth = dayOfMonth.coerceIn(1, 31),
                isActive = true,
                lastBookedMonth = ""
            )
        )
    }

    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity) {
        dao.updateRecurringExpense(expense)
    }

    suspend fun deleteRecurringExpense(id: Long) {
        dao.deleteRecurringExpense(id)
    }

    suspend fun bookRecurringExpenses(
        yearMonth: String,
        expensesToBook: List<RecurringExpenseEntity>
    ) {
        ensureMonthBudget(yearMonth)
        for (recurring in expensesToBook) {
            val expense = ExpenseEntity(
                yearMonth = yearMonth,
                category = recurring.category,
                amount = recurring.amount,
                note = "[Stała opłata] ${recurring.name}",
                timestamp = System.currentTimeMillis()
            )
            dao.insertExpense(expense)
            dao.markRecurringExpenseBooked(recurring.id, yearMonth)
        }
    }

    // --- Category Limits (Limity Kategorii) ---
    fun getAllCategoryLimits(): Flow<List<CategoryLimitEntity>> =
        dao.getAllCategoryLimits()

    suspend fun setCategoryLimit(category: String, limit: Double) {
        if (limit <= 0.0) {
            dao.deleteCategoryLimit(category)
        } else {
            dao.insertOrUpdateCategoryLimit(CategoryLimitEntity(category, limit))
        }
    }

    // --- Savings Goals (Cele Oszczędnościowe) ---
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>> =
        dao.getAllSavingsGoals()

    suspend fun addSavingsGoal(name: String, targetAmount: Double, initialAmount: Double = 0.0, iconName: String = "star"): Long {
        return dao.insertSavingsGoal(
            SavingsGoalEntity(
                name = name,
                targetAmount = targetAmount,
                currentAmount = initialAmount.coerceAtLeast(0.0),
                iconName = iconName
            )
        )
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        dao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(id: Long) {
        dao.deleteSavingsGoal(id)
    }

    suspend fun depositToSavingsGoal(id: Long, amount: Double) {
        dao.depositToSavingsGoal(id, amount)
    }

    // --- History Line Chart Aggregates ---
    fun getHistoryPoints(count: Int = 6): Flow<List<MonthlyHistoryPoint>> {
        return combine(dao.getAllBudgets(), dao.getAllExpenses()) { budgets, expenses ->
            val budgetsMap = budgets.associateBy { it.yearMonth }
            val expensesByMonth = expenses.groupBy { it.yearMonth }

            // Generate list of the last `count` months up to current or latest
            val current = MonthKey.current()
            val points = mutableListOf<MonthlyHistoryPoint>()
            var cur = current
            // Go back count - 1 months
            for (i in 1 until count) {
                cur = cur.previous()
            }
            // Now go forward
            for (i in 0 until count) {
                val key = cur.keyString
                val budget = budgetsMap[key]
                val monthExps = expensesByMonth[key].orEmpty()
                val income = budget?.income ?: 0.0
                val savings = budget?.savings ?: 0.0
                val totalExp = monthExps.sumOf { it.amount }
                val free = income - totalExp - savings

                points.add(
                    MonthlyHistoryPoint(
                        monthKey = cur,
                        income = income,
                        expenses = totalExp,
                        savings = savings,
                        freeFunds = free
                    )
                )
                cur = cur.next()
            }
            points
        }
    }

    // --- Rollover & Settings ---
    fun isBiometricEnabled(): Flow<Boolean> =
        dao.getSettingFlow(KEY_BIOMETRIC_ENABLED).map { it?.value == "true" }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dao.insertSetting(SettingEntity(KEY_BIOMETRIC_ENABLED, enabled.toString()))
    }

    fun isRolloverEnabled(): Flow<Boolean> =
        dao.getSettingFlow(KEY_ROLLOVER_ENABLED).map { it?.value != "false" } // Enabled by default

    suspend fun setRolloverEnabled(enabled: Boolean) {
        dao.insertSetting(SettingEntity(KEY_ROLLOVER_ENABLED, enabled.toString()))
    }

    companion object {
        @Volatile
        private var INSTANCE: BudgetRepository? = null

        fun getInstance(context: android.content.Context): BudgetRepository {
            return INSTANCE ?: synchronized(this) {
                val db = BudgetDatabase.getDatabase(context)
                val repo = BudgetRepository(db.budgetDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
