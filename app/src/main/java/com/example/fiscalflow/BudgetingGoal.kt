package com.example.fiscalflow

// Stores monthly spending goals per user.
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
