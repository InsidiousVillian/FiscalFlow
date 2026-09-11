package com.example.fiscalflow

// Temporary expense model (no database yet)
data class Expense(
    val amount: Double,          // money spent
    val startDate: String,       // start date of expense
    val endDate: String,         // end date of expense
    val description: String,     // what it was for
    val category: String,        // category name
    val photoUri: String? = null // receipt photo
)
