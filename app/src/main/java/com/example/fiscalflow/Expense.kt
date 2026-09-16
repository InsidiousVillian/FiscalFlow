package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Database model representing a tracked expense or income transaction.
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,            // amount spent or earned
    val startDate: String,         // start date string ("D/M/YYYY")
    val endDate: String,           // end date string
    val description: String,       // description/title
    val category: String,          // category name
    val photoUri: String? = null,  // optional receipt image URI
    val isIncome: Boolean = false  // false = Expense, true = Income
)
