package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BudgetCategory(
    val id: String,
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val colorHex: String
) {
    CZYNSZ("Czynsz", "Czynsz", Color(0xFF6366F1), Icons.Default.Home, "#6366f1"),
    PRAD("Prąd", "Prąd", Color(0xFFF59E0B), Icons.Default.Bolt, "#f59e0b"),
    GAZ("Gaz", "Gaz", Color(0xFFEC4899), Icons.Default.LocalFireDepartment, "#ec4899"),
    JEDZENIE("Jedzenie", "Jedzenie", Color(0xFF10B981), Icons.Default.Restaurant, "#10b981"),
    RATY("Raty", "Raty", Color(0xFFEF4444), Icons.Default.CreditCard, "#ef4444"),
    MEDIA("Media", "Media", Color(0xFF06B6D4), Icons.Default.Wifi, "#06b6d4"),
    INNE("Inne", "Inne", Color(0xFFA855F7), Icons.Default.Category, "#a855f7");

    companion object {
        fun fromId(id: String): BudgetCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: INNE
        }
    }
}

data class CategorySummary(
    val category: BudgetCategory,
    val amount: Double,
    val percentage: Float, // 0f to 1f of total expenses
    val limit: Double? = null,
    val limitPercentage: Float? = null // percentage of limit consumed (e.g. 0.85 = 85%)
) {
    val isNearLimit: Boolean
        get() = (limitPercentage ?: 0f) >= 0.8f && (limitPercentage ?: 0f) < 1.0f

    val isOverLimit: Boolean
        get() = (limitPercentage ?: 0f) >= 1.0f
}

data class MonthlyHistoryPoint(
    val monthKey: MonthKey,
    val income: Double,
    val expenses: Double,
    val savings: Double,
    val freeFunds: Double
)

data class MonthKey(
    val year: Int,
    val month: Int // 1..12
) : Comparable<MonthKey> {

    val keyString: String
        get() = String.format("%04d-%02d", year, month)

    fun displayName(): String {
        val monthNames = arrayOf(
            "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
        )
        val monthName = if (month in 1..12) monthNames[month - 1] else "Miesiąc $month"
        return "$monthName $year"
    }

    fun next(): MonthKey {
        return if (month == 12) MonthKey(year + 1, 1) else MonthKey(year, month + 1)
    }

    fun previous(): MonthKey {
        return if (month == 1) MonthKey(year - 1, 12) else MonthKey(year, month - 1)
    }

    override fun compareTo(other: MonthKey): Int {
        return if (year != other.year) year.compareTo(other.year) else month.compareTo(other.month)
    }

    companion object {
        fun fromKey(key: String): MonthKey {
            val parts = key.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 10
            return MonthKey(y, m)
        }

        fun current(): MonthKey {
            val cal = java.util.Calendar.getInstance()
            return MonthKey(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
        }
    }
}

data class MonthOverview(
    val monthKey: MonthKey,
    val totalExpenses: Double,
    val savings: Double
)
