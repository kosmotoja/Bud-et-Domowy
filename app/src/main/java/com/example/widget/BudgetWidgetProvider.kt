package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.BudgetDatabase
import com.example.model.MonthKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class BudgetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, BudgetWidgetProvider::class.java)
            )
            updateWidgets(context, appWidgetManager, ids)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val currentMonth = MonthKey.current()
            val symbols = DecimalFormatSymbols(Locale.GERMAN).apply { groupingSeparator = ' ' }
            val df = DecimalFormat("#,##0", symbols)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = BudgetDatabase.getDatabase(context)
                    val dao = db.budgetDao()
                    val budget = dao.getMonthBudgetSync(currentMonth.keyString)
                    val expenses = dao.getExpensesForMonthSync(currentMonth.keyString)

                    val income = budget?.income ?: 0.0
                    val savings = budget?.savings ?: 0.0
                    val totalExpenses = expenses.sumOf { it.amount }
                    val freeFunds = income - totalExpenses - savings

                    val freeFundsStr = if (freeFunds >= 0) "+${df.format(freeFunds)} zł" else "${df.format(freeFunds)} zł"
                    val freeFundsColor = if (freeFunds >= 0) 0xFF06B6D4.toInt() else 0xFFF43F5E.toInt()

                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_budget).apply {
                            setTextViewText(R.id.widget_month_text, currentMonth.displayName())
                            setTextViewText(R.id.widget_free_funds, freeFundsStr)
                            setTextColor(R.id.widget_free_funds, freeFundsColor)
                            setTextViewText(R.id.widget_income, "${df.format(income)} zł")
                            setTextViewText(R.id.widget_expenses, "${df.format(totalExpenses)} zł")
                            setTextViewText(R.id.widget_savings, "${df.format(savings)} zł")
                            setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                        }
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                } catch (e: Exception) {
                    // Fallback views on error
                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_budget).apply {
                            setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                        }
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                }
            }
        }
    }
}
