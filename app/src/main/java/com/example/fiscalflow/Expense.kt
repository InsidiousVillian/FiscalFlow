package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Expense row persisted in Room. Every field maps to a column in the "expenses" table.
// `id` is auto-generated so callers (e.g. ExpenseScreen) can leave it at 0 when creating a new Expense.
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,            // money spent
    val startDate: String,         // start date of expense (kept as text, e.g. "5/9/2026", to match UI)
    val endDate: String,           // end date of expense
    val description: String,       // what it was for
    val category: String,          // category name (matches CategoryEntity.name)
    val photoUri: String? = null   // optional receipt photo Uri as string
)
