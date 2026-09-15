package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Monthly spending goal. Persisted in Room as a single-row table (id = 1 always).
// Storing it as an entity means the goal is restored automatically after the app restarts.
@Entity(tableName = "budget_goal")
data class BudgetingGoal(
    val minimum: Double,
    val maximum: Double,
    val month: Int,
    val year: Int,
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID
) {
    companion object {
        // The goal table only ever contains one row; we reuse the same primary key when saving.
        const val SINGLE_ROW_ID: Int = 1
    }
}
