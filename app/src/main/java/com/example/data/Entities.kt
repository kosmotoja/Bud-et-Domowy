package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "month_budgets")
data class MonthBudgetEntity(
    @PrimaryKey val yearMonth: String, // Format "YYYY-MM", e.g. "2026-10"
    val income: Double,
    val savings: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["yearMonth"]), Index(value = ["category"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String, // e.g. "2026-10"
    val category: String,  // e.g. "Czynsz", "Prąd", etc.
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
