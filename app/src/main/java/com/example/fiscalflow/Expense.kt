package com.example.fiscalflow

// Model representing a tracked expense or income transaction.
data class Expense(
    val id: Long = 0,
    val amount: Double,            // amount spent or earned
    val startDate: String,         // start date string ("D/M/YYYY")
    val endDate: String,           // end date string
    val description: String,       // description/title
    val category: String,          // category name
    val photoUri: String? = null,  // optional receipt image URI
    val isIncome: Boolean = false, // false = Expense, true = Income
    val ownerUsername: String = "unassigned_legacy" // account that owns this record
)
