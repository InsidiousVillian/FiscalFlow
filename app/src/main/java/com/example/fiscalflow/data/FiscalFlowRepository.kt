package com.example.fiscalflow.data

import android.content.SharedPreferences
import android.util.Log
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.FirebaseIdentity
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Repository managing data flow between Room DAOs and the ViewModel.
class FiscalFlowRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetGoalDao: BudgetGoalDao,
    private val userProgressDao: UserProgressDao,
    private val prefs: SharedPreferences? = null
) {

    private val tag = "FiscalFlowRepo"

    // Active user session state flow, restored from SharedPreferences if available
    private val _activeUsername = MutableStateFlow<String?>(
        prefs?.getString("active_username", null)
    )
    val activeUsername = _activeUsername.asStateFlow()

    fun setActiveUser(username: String) {
        val trimmed = username.trim()
        _activeUsername.value = trimmed
        prefs?.edit()?.putString("active_username", trimmed)?.apply()
    }

    fun clearActiveUser() {
        _activeUsername.value = null
        prefs?.edit()?.remove("active_username")?.apply()
        FirebaseIdentity.signOut()
    }

    private fun requireActiveUser(): String? =
        _activeUsername.value?.takeIf { it.isNotBlank() }

    // User management

    suspend fun registerUser(username: String, password: String): Boolean {
        val trimmed = username.trim()
        if (trimmed.isBlank() || password.isBlank()) return false

        // Prevent registering matching duplicate usernames
        val existing = userDao.findByUsername(trimmed)
        if (existing != null) {
            Log.w(tag, "registerUser: username '$trimmed' already exists")
            return false
        }
        userDao.insert(UserEntity(username = trimmed, password = password))
        setActiveUser(trimmed)
        Log.d(tag, "registerUser: created account for '$trimmed'")
        return true
    }

    suspend fun authenticate(username: String, password: String): Boolean {
        val trimmed = username.trim()
        val user = userDao.findByUsername(trimmed)
        val ok = user != null && user.password == password
        if (ok) {
            setActiveUser(user.username)
        }
        Log.d(tag, "authenticate('$trimmed') -> $ok")
        return ok
    }

    // Seed default admin account on first app launch
    suspend fun seedDefaultUserIfEmpty() {
        if (userDao.count() == 0) {
            userDao.insert(UserEntity(username = "admin", password = "password123"))
            Log.i(tag, "Seeded default admin account")
        }
    }

    // Categories filtered by active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: Flow<List<String>> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) {
                flowOf(emptyList())
            } else {
                categoryDao.observeAll(owner).map { rows ->
                    rows.map { it.name }
                }
            }
        }

    suspend fun addCategory(name: String): Boolean {
        val owner = requireActiveUser() ?: return false
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val rowId = categoryDao.insert(CategoryEntity(name = trimmed, ownerUsername = owner))
        val inserted = rowId != -1L
        Log.d(tag, "addCategory('$trimmed') inserted=$inserted for $owner")
        return inserted
    }

    // Remove category along with any associated expenses for active account
    suspend fun deleteCategory(category: String) {
        val owner = requireActiveUser() ?: return
        expenseDao.deleteByCategory(owner, category)
        categoryDao.deleteByName(owner, category)
        Log.d(tag, "deleteCategory('$category') completed for $owner")
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        val owner = requireActiveUser() ?: return
        if (categoryDao.count(owner) == 0) {
            listOf("Groceries", "Rent", "Utilities").forEach {
                categoryDao.insert(CategoryEntity(it, ownerUsername = owner))
            }
            Log.i(tag, "Seeded default categories for $owner")
        }
    }

    // Expenses filtered by active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<Expense>> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(emptyList())
            else expenseDao.observeAll(owner)
        }

    suspend fun addExpense(expense: Expense): Long {
        val owner = requireActiveUser() ?: return -1L
        val toInsert = expense.copy(id = 0, ownerUsername = owner)
        val newId = expenseDao.insert(toInsert)
        Log.d(tag, "addExpense saved id=$newId amount=${expense.amount} category=${expense.category} owner=$owner")
        return newId
    }

    // Budget goal filtered by active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetGoal: Flow<BudgetingGoal?> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(null)
            else budgetGoalDao.observeCurrent(owner)
        }

    suspend fun saveGoal(goal: BudgetingGoal) {
        val owner = requireActiveUser() ?: return
        budgetGoalDao.upsert(
            goal.copy(
                id = BudgetingGoal.SINGLE_ROW_ID,
                ownerUsername = owner
            )
        )
        Log.d(tag, "saveGoal min=${goal.minimum} max=${goal.maximum} owner=$owner")
    }

    // User progress tracking filtered by active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val userProgress: Flow<UsersProgress> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(UsersProgress())
            else userProgressDao.observeCurrent(owner).map { it ?: UsersProgress(ownerUsername = owner) }
        }

    suspend fun saveProgress(progress: UsersProgress) {
        val owner = requireActiveUser() ?: return
        userProgressDao.upsert(
            progress.copy(
                id = UsersProgress.SINGLE_ROW_ID,
                ownerUsername = owner
            )
        )
    }

    suspend fun deleteExpense(expense: Expense) {
        val owner = requireActiveUser() ?: return
        val ownedExpense = expenseDao.getById(owner, expense.id) ?: return
        expenseDao.delete(ownedExpense)
        Log.d(tag, "deleteExpense: removed expense id=${expense.id} owner=$owner")
    }
}
