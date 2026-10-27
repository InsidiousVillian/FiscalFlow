package com.example.fiscalflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    @Test
    fun expenseModel_creationAndOwnership_verifiesCorrectFields() {
        val expense = Expense(
            id = 100L,
            amount = 150.0,
            startDate = "1/1/2026",
            endDate = "1/1/2026",
            description = "Groceries",
            category = "Food",
            isIncome = false,
            ownerUsername = "UserA"
        )

        assertEquals(100L, expense.id)
        assertEquals(150.0, expense.amount, 0.0001)
        assertEquals("UserA", expense.ownerUsername)
    }

    @Test
    fun categoryModel_creation_verifiesNameAndOwner() {
        val category = CategoryEntity(name = "Utilities", ownerUsername = "UserB")
        assertEquals("Utilities", category.name)
        assertEquals("UserB", category.ownerUsername)
    }

    @Test
    fun budgetingGoal_singleRowId_verifiesConstant() {
        val goal = BudgetingGoal(minimum = 100.0, maximum = 500.0, month = 1, year = 2026, ownerUsername = "UserA")
        assertEquals(BudgetingGoal.SINGLE_ROW_ID, goal.id)
        assertEquals(100.0, goal.minimum, 0.0001)
        assertEquals(500.0, goal.maximum, 0.0001)
    }

    @Test
    fun usersProgress_milestonesParsing_verifiesListHelper() {
        val progress = UsersProgress.fromList(
            xp = 100,
            streak = 5,
            milestones = listOf("m1", "m2", "m3"),
            ownerUsername = "UserA"
        )

        assertEquals(100, progress.xp)
        assertEquals(5, progress.streak)
        assertEquals(listOf("m1", "m2", "m3"), progress.milestones)
    }
}
