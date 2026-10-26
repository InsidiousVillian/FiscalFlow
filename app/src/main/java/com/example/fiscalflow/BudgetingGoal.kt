package com.example.fiscalflow

import androidx.room.Entity

// Stores monthly spending goals in Room per user.
@Entity(tableName = "budget_goal", primaryKeys = ["ownerUsername", "id"])
data class BudgetingGoal(
    val minimum: Double,
    val maximum: Double,
    val month: Int,
    val year: Int,
    val id: Int = SINGLE_ROW_ID,
    val ownerUsername: String = "unassigned_legacy"
) {
    companion object {
        const val SINGLE_ROW_ID: Int = 1
    }
}
