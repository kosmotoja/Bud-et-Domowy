package com.example.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BudgetBackupManager {

    suspend fun createJsonBackup(dao: BudgetDao): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // 1. Budgets
        val budgets = dao.getAllBudgetsSync()
        val budgetsArray = JSONArray()
        budgets.forEach { b ->
            val obj = JSONObject().apply {
                put("yearMonth", b.yearMonth)
                put("income", b.income)
                put("savings", b.savings)
                put("updatedAt", b.updatedAt)
            }
            budgetsArray.put(obj)
        }
        root.put("budgets", budgetsArray)

        // 2. Expenses
        val expenses = dao.getAllExpensesSync()
        val expensesArray = JSONArray()
        expenses.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("yearMonth", e.yearMonth)
                put("category", e.category)
                put("amount", e.amount)
                put("note", e.note)
                put("timestamp", e.timestamp)
            }
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        // 3. Recurring Expenses
        val recurring = dao.getAllRecurringExpensesSync()
        val recurringArray = JSONArray()
        recurring.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("amount", r.amount)
                put("category", r.category)
                put("dayOfMonth", r.dayOfMonth)
                put("isActive", r.isActive)
                put("lastBookedMonth", r.lastBookedMonth)
            }
            recurringArray.put(obj)
        }
        root.put("recurringExpenses", recurringArray)

        // 4. Category Limits
        val limits = dao.getAllCategoryLimitsSync()
        val limitsArray = JSONArray()
        limits.forEach { l ->
            val obj = JSONObject().apply {
                put("category", l.category)
                put("monthlyLimit", l.monthlyLimit)
            }
            limitsArray.put(obj)
        }
        root.put("categoryLimits", limitsArray)

        // 5. Savings Goals
        val goals = dao.getAllSavingsGoalsSync()
        val goalsArray = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("targetAmount", g.targetAmount)
                put("currentAmount", g.currentAmount)
                put("iconName", g.iconName)
                put("createdAt", g.createdAt)
            }
            goalsArray.put(obj)
        }
        root.put("savingsGoals", goalsArray)

        // 6. Settings
        val settings = dao.getAllSettingsSync()
        val settingsArray = JSONArray()
        settings.forEach { s ->
            val obj = JSONObject().apply {
                put("key", s.key)
                put("value", s.value)
            }
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        return root.toString(2)
    }

    suspend fun createCsvExport(dao: BudgetDao): String {
        val expenses = dao.getAllExpensesSync()
        val sb = StringBuilder()
        sb.append("ID;Miesiąc;Kategoria;Kwota;Notatka;Data\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        expenses.forEach { e ->
            val dateStr = dateFormat.format(Date(e.timestamp))
            val cleanNote = e.note.replace(";", ",").replace("\n", " ")
            sb.append("${e.id};${e.yearMonth};${e.category};${e.amount};${cleanNote};${dateStr}\n")
        }
        return sb.toString()
    }

    suspend fun restoreFromJson(dao: BudgetDao, jsonString: String, replaceExisting: Boolean = true): Result<Int> {
        return runCatching {
            val root = JSONObject(jsonString)

            val budgetsList = mutableListOf<MonthBudgetEntity>()
            val budgetsArray = root.optJSONArray("budgets")
            if (budgetsArray != null) {
                for (i in 0 until budgetsArray.length()) {
                    val obj = budgetsArray.getJSONObject(i)
                    budgetsList.add(
                        MonthBudgetEntity(
                            yearMonth = obj.getString("yearMonth"),
                            income = obj.getDouble("income"),
                            savings = obj.optDouble("savings", 0.0),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val expensesList = mutableListOf<ExpenseEntity>()
            val expensesArray = root.optJSONArray("expenses")
            if (expensesArray != null) {
                for (i in 0 until expensesArray.length()) {
                    val obj = expensesArray.getJSONObject(i)
                    expensesList.add(
                        ExpenseEntity(
                            id = if (replaceExisting) obj.optLong("id", 0L) else 0L,
                            yearMonth = obj.getString("yearMonth"),
                            category = obj.getString("category"),
                            amount = obj.getDouble("amount"),
                            note = obj.optString("note", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val recurringList = mutableListOf<RecurringExpenseEntity>()
            val recurringArray = root.optJSONArray("recurringExpenses")
            if (recurringArray != null) {
                for (i in 0 until recurringArray.length()) {
                    val obj = recurringArray.getJSONObject(i)
                    recurringList.add(
                        RecurringExpenseEntity(
                            id = if (replaceExisting) obj.optLong("id", 0L) else 0L,
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            dayOfMonth = obj.optInt("dayOfMonth", 1),
                            isActive = obj.optBoolean("isActive", true),
                            lastBookedMonth = obj.optString("lastBookedMonth", "")
                        )
                    )
                }
            }

            val limitsList = mutableListOf<CategoryLimitEntity>()
            val limitsArray = root.optJSONArray("categoryLimits")
            if (limitsArray != null) {
                for (i in 0 until limitsArray.length()) {
                    val obj = limitsArray.getJSONObject(i)
                    limitsList.add(
                        CategoryLimitEntity(
                            category = obj.getString("category"),
                            monthlyLimit = obj.getDouble("monthlyLimit")
                        )
                    )
                }
            }

            val goalsList = mutableListOf<SavingsGoalEntity>()
            val goalsArray = root.optJSONArray("savingsGoals")
            if (goalsArray != null) {
                for (i in 0 until goalsArray.length()) {
                    val obj = goalsArray.getJSONObject(i)
                    goalsList.add(
                        SavingsGoalEntity(
                            id = if (replaceExisting) obj.optLong("id", 0L) else 0L,
                            name = obj.getString("name"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = obj.optDouble("currentAmount", 0.0),
                            iconName = obj.optString("iconName", "star"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val settingsList = mutableListOf<SettingEntity>()
            val settingsArray = root.optJSONArray("settings")
            if (settingsArray != null) {
                for (i in 0 until settingsArray.length()) {
                    val obj = settingsArray.getJSONObject(i)
                    settingsList.add(
                        SettingEntity(
                            key = obj.getString("key"),
                            value = obj.getString("value")
                        )
                    )
                }
            }

            // Perform atomic database transaction
            dao.importFullBackup(
                budgets = budgetsList,
                expenses = expensesList,
                recurring = recurringList,
                limits = limitsList,
                goals = goalsList,
                settings = settingsList,
                replaceExisting = replaceExisting
            )

            expensesList.size + recurringList.size
        }
    }

    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
