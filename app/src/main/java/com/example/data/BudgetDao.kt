package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM month_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getMonthBudget(yearMonth: String): Flow<MonthBudgetEntity?>

    @Query("SELECT * FROM month_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getMonthBudgetSync(yearMonth: String): MonthBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: MonthBudgetEntity)

    @Query("SELECT * FROM expenses WHERE yearMonth = :yearMonth ORDER BY timestamp DESC")
    fun getExpensesForMonth(yearMonth: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE yearMonth = :yearMonth")
    suspend fun getExpensesForMonthSync(yearMonth: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE yearMonth = :yearMonth AND category = :category")
    suspend fun deleteExpensesByCategory(yearMonth: String, category: String)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM month_budgets ORDER BY yearMonth DESC")
    fun getAllBudgets(): Flow<List<MonthBudgetEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): SettingEntity?

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<SettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)
}
