package com.example.fiscalflow.data

import android.util.Log
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository sits between the ViewModel and the DAOs.
 *
 * Two benefits for this project:
 *   1. The ViewModel never needs to know Room exists — swap the backend later without changing UI.
 *   2. We can add cross-cutting concerns here (logging, seeding defaults, mapping entities to
 *      simpler UI types) in one place.
 */
class FiscalFlowRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetGoalDao: BudgetGoalDao,
    private val userProgressDao: UserProgressDao
) {

    private val tag = "FiscalFlowRepo"

    // ---------- Users ----------

    suspend fun registerUser(username: String, password: String): Boolean {
        // Return false if the username is already taken so SignUpScreen can show an error.
        val existing = userDao.findByUsername(username)
        if (existing != null) {
            Log.w(tag, "registerUser: username '$username' already exists")
            return false
        }
        userDao.insert(UserEntity(username = username, password = password))
        Log.d(tag, "registerUser: created account for '$username'")
        return true
    }

    suspend fun authenticate(username: String, password: String): Boolean {
        val user = userDao.findByUsername(username)
        val ok = user != null && user.password == password
        Log.d(tag, "authenticate('$username') -> $ok")
        return ok
    }

    /** Seeds a default admin account the first time the app runs so testers can log in immediately. */
    suspend fun seedDefaultUserIfEmpty() {
        if (userDao.count() == 0) {
            userDao.insert(UserEntity(username = "admin", password = "password123"))
            Log.i(tag, "Seeded default admin/password123 account")
        }
    }

    // ---------- Categories ----------

    // Flow<List<String>> is what the UI actually wants (screens don't care about the entity type).
    val categories: Flow<List<String>> =
        categoryDao.observeAll().map { rows -> rows.map { it.name } }

    suspend fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        // insert() returns -1L when OnConflictStrategy.IGNORE dropped a duplicate row.
        val rowId = categoryDao.insert(CategoryEntity(name = trimmed))
        val inserted = rowId != -1L
        Log.d(tag, "addCategory('$trimmed') inserted=$inserted")
        return inserted
    }

    // Delete a category and all expenses saved under it
    suspend fun deleteCategory(category: String) {

        // Delete the expenses belonging to this category first
        expenseDao.deleteByCategory(category)

        // Then delete the category itself
        categoryDao.deleteByName(category)

        Log.d(tag, "deleteCategory('$category') completed")
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        if (categoryDao.count() == 0) {
            listOf("Groceries", "Rent", "Utilities").forEach {
                categoryDao.insert(CategoryEntity(it))
            }
            Log.i(tag, "Seeded default categories")
        }
    }

    // ---------- Expenses ----------

    val expenses: Flow<List<Expense>> = expenseDao.observeAll()

    suspend fun addExpense(expense: Expense) {
        // Ensure the auto-generated id is used by passing id = 0 (the entity default).
        val toInsert = if (expense.id == 0L) expense else expense.copy(id = 0)
        val newId = expenseDao.insert(toInsert)
        Log.d(tag, "addExpense saved id=$newId amount=${expense.amount} category=${expense.category}")
    }

    // ---------- Budget goal ----------

    val budgetGoal: Flow<BudgetingGoal?> = budgetGoalDao.observeCurrent()

    suspend fun saveGoal(goal: BudgetingGoal) {
        // Always upsert into the single-row id so we overwrite the previous month's goal.
        budgetGoalDao.upsert(goal.copy(id = BudgetingGoal.SINGLE_ROW_ID))
        Log.d(tag, "saveGoal min=${goal.minimum} max=${goal.maximum}")
    }

    // ---------- User progress ----------

    // Downstream code prefers a non-null value, so we fall back to the default when the row is missing.
    val userProgress: Flow<UsersProgress> =
        userProgressDao.observeCurrent().map { it ?: UsersProgress() }

    suspend fun saveProgress(progress: UsersProgress) {
        userProgressDao.upsert(progress.copy(id = UsersProgress.SINGLE_ROW_ID))
    }
}
