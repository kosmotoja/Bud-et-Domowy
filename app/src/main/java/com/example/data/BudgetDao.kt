package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MonthlyExpenseAggregate(
    val yearMonth: String,
    val totalAmount: Double,
    val count: Int
)

@Dao
interface BudgetDao {

    @Query("SELECT * FROM month_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getMonthBudget(yearMonth: String): Flow<MonthBudgetEntity?>

    @Query("SELECT * FROM month_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getMonthBudgetSync(yearMonth: String): MonthBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: MonthBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<MonthBudgetEntity>)

    @Query("SELECT * FROM expenses WHERE yearMonth = :yearMonth ORDER BY timestamp DESC")
    fun getExpensesForMonth(yearMonth: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE yearMonth = :yearMonth")
    suspend fun getExpensesForMonthSync(yearMonth: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE yearMonth = :yearMonth AND category = :category")
    suspend fun deleteExpensesByCategory(yearMonth: String, category: String)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM month_budgets ORDER BY yearMonth DESC")
    fun getAllBudgets(): Flow<List<MonthBudgetEntity>>

    @Query("SELECT * FROM month_budgets ORDER BY yearMonth ASC")
    suspend fun getAllBudgetsSync(): List<MonthBudgetEntity>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp ASC")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): SettingEntity?

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<SettingEntity?>

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettingsSync(): List<SettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<SettingEntity>)

    // --- Aggregates (SQL SUM() and GROUP BY) ---
    @Query("SELECT yearMonth, SUM(amount) as totalAmount, COUNT(id) as count FROM expenses GROUP BY yearMonth ORDER BY yearMonth ASC")
    fun getMonthlyAggregates(): Flow<List<MonthlyExpenseAggregate>>

    // --- Recurring Expenses ---
    @Query("SELECT * FROM recurring_expenses ORDER BY dayOfMonth ASC, id ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses ORDER BY dayOfMonth ASC, id ASC")
    suspend fun getAllRecurringExpensesSync(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpenses(expenses: List<RecurringExpenseEntity>)

    @Update
    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteRecurringExpense(id: Long)

    @Query("UPDATE recurring_expenses SET lastBookedMonth = :yearMonth WHERE id = :id")
    suspend fun markRecurringExpenseBooked(id: Long, yearMonth: String)

    // --- Category Limits ---
    @Query("SELECT * FROM category_limits")
    fun getAllCategoryLimits(): Flow<List<CategoryLimitEntity>>

    @Query("SELECT * FROM category_limits")
    suspend fun getAllCategoryLimitsSync(): List<CategoryLimitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCategoryLimit(limit: CategoryLimitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryLimits(limits: List<CategoryLimitEntity>)

    @Query("DELETE FROM category_limits WHERE category = :category")
    suspend fun deleteCategoryLimit(category: String)

    // --- Savings Goals ---
    @Query("SELECT * FROM savings_goals ORDER BY createdAt ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals ORDER BY createdAt ASC")
    suspend fun getAllSavingsGoalsSync(): List<SavingsGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoal(id: Long)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun depositToSavingsGoal(id: Long, amount: Double)

    // --- Database reset for import ---
    @Query("DELETE FROM month_budgets")
    suspend fun clearAllBudgets()

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM recurring_expenses")
    suspend fun clearAllRecurringExpenses()

    @Query("DELETE FROM category_limits")
    suspend fun clearAllCategoryLimits()

    @Query("DELETE FROM savings_goals")
    suspend fun clearAllSavingsGoals()

    @Transaction
    suspend fun importFullBackup(
        budgets: List<MonthBudgetEntity>,
        expenses: List<ExpenseEntity>,
        recurring: List<RecurringExpenseEntity>,
        limits: List<CategoryLimitEntity>,
        goals: List<SavingsGoalEntity>,
        settings: List<SettingEntity>,
        replaceExisting: Boolean
    ) {
        if (replaceExisting) {
            clearAllBudgets()
            clearAllExpenses()
            clearAllRecurringExpenses()
            clearAllCategoryLimits()
            clearAllSavingsGoals()
        }
        if (budgets.isNotEmpty()) insertBudgets(budgets)
        if (expenses.isNotEmpty()) insertExpenses(expenses)
        if (recurring.isNotEmpty()) insertRecurringExpenses(recurring)
        if (limits.isNotEmpty()) insertCategoryLimits(limits)
        if (goals.isNotEmpty()) insertSavingsGoals(goals)
        if (settings.isNotEmpty()) insertSettings(settings)
    }
}
