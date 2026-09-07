package com.example.data

import com.example.model.MonthKey
import com.example.model.MonthOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BudgetRepository(private val dao: BudgetDao) {

    private val KEY_DEFAULT_INCOME = "default_income"
    private val INITIAL_DEFAULT_INCOME = 6500.0 // Reasonable default if none set yet

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
}
