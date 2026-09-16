package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores monthly spending goals in Room as a single row (id = 1).
@Entity(tableName = "budget_goal")
data class BudgetingGoal(
    val minimum: Double,
    val maximum: Double,
    val month: Int,
    val year: Int,
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID
)
{
    companion object {
        // Reuse key 1 so saving updates the existing goal
        const val SINGLE_ROW_ID: Int = 1
    }
}
