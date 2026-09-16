package com.example.fiscalflow.data

import android.util.Log
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Repository managing data flow between Room DAOs and the ViewModel.
class FiscalFlowRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetGoalDao: BudgetGoalDao,
    private val userProgressDao: UserProgressDao
) {

    private val tag = "FiscalFlowRepo"

    // User management

    suspend fun registerUser(username: String, password: String): Boolean {
        // Prevent registering matching duplicate usernames
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

    // Seed default admin account on first app launch
    suspend fun seedDefaultUserIfEmpty() {
        if (userDao.count() == 0) {
            userDao.insert(UserEntity(username = "admin", password = "password123"))
            Log.i(tag, "Seeded default admin account")
        }
    }

    // Categories

    val categories: Flow<List<String>> =
        categoryDao.observeAll().map { rows -> rows.map { it.name } }

    suspend fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val rowId = categoryDao.insert(CategoryEntity(name = trimmed))
        val inserted = rowId != -1L
        Log.d(tag, "addCategory('$trimmed') inserted=$inserted")
        return inserted
    }

    // Remove category along with any associated expenses
    suspend fun deleteCategory(category: String) {
        expenseDao.deleteByCategory(category)
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

    // Expenses

    val expenses: Flow<List<Expense>> = expenseDao.observeAll()

    suspend fun addExpense(expense: Expense) {
        val toInsert = if (expense.id == 0L) expense else expense.copy(id = 0)
        val newId = expenseDao.insert(toInsert)
        Log.d(tag, "addExpense saved id=$newId amount=${expense.amount} category=${expense.category}")
    }

    // Budget goal

    val budgetGoal: Flow<BudgetingGoal?> = budgetGoalDao.observeCurrent()

    suspend fun saveGoal(goal: BudgetingGoal) {
        budgetGoalDao.upsert(goal.copy(id = BudgetingGoal.SINGLE_ROW_ID))
        Log.d(tag, "saveGoal min=${goal.minimum} max=${goal.maximum}")
    }

    // User progress tracking

    val userProgress: Flow<UsersProgress> =
        userProgressDao.observeCurrent().map { it ?: UsersProgress() }

    suspend fun saveProgress(progress: UsersProgress) {
        userProgressDao.upsert(progress.copy(id = UsersProgress.SINGLE_ROW_ID))
    }
}
