
package com.example.fiscalflow.data

import android.util.Log
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.PendingSyncEntity
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FiscalFlowRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetGoalDao: BudgetGoalDao,
    private val userProgressDao: UserProgressDao,
    private val pendingSyncDao: PendingSyncDao
) {
    private val tag = "FiscalFlowRepo"

    // The account currently selected in the app
    private val _activeUsername = MutableStateFlow<String?>(null)
    val activeUsername = _activeUsername.asStateFlow()

    // Call after a successful login or registration
    fun setActiveUser(username: String) {
        _activeUsername.value = username.trim()
    }

    // Hide account-specific records after logout
    fun clearActiveUser() {
        _activeUsername.value = null
    }

    private fun requireActiveUser(): String? =
        _activeUsername.value?.takeIf { it.isNotBlank() }

    private suspend fun queueChange(
        ownerUsername: String,
        entityType: String,
        operation: String,
        recordId: String
    ) {
        pendingSyncDao.insert(
            PendingSyncEntity(
                entityType = entityType,
                operation = operation,
                recordId = recordId,
                ownerUsername = ownerUsername
            )
        )
    }

    suspend fun registerUser(username: String, password: String): Boolean {
        val trimmed = username.trim()
        if (trimmed.isBlank() || password.isBlank()) return false

        if (userDao.findByUsername(trimmed) != null) {
            Log.w(tag, "registerUser: username already exists")
            return false
        }

        userDao.insert(UserEntity(username = trimmed, password = password))
        Log.d(tag, "registerUser: account created")
        return true
    }

    suspend fun authenticate(username: String, password: String): Boolean {
        val user = userDao.findByUsername(username.trim())
        return user != null && user.password == password
    }

    // Link a Firebase UID without replacing a different existing UID.
    suspend fun linkFirebaseUid(username: String, uid: String): Boolean {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || uid.isBlank()) return false

        val user = userDao.findByUsername(trimmedUsername) ?: return false
        // Prevent one Firebase UID from linking to different local accounts
        val linkedUser = userDao.findByFirebaseUid(uid)

        if (linkedUser != null && linkedUser.username != trimmedUsername) {
            Log.w(tag, "linkFirebaseUid: UID already belongs to another local account")
            return false
        }

        //Verify that the local account belongs to the signed-in Firebase user

        // Refuse to link this account to a different Firebase identity
        if (user.firebaseUid != null && user.firebaseUid != uid) {
            Log.w(tag, "linkFirebaseUid: existing UID does not match")
            return false
        }

        //return userDao.updateFirebaseUid(trimmedUsername, uid) == 1
        // TEMPORARY DIAGNOSTIC: check whether Room accepted the UID link
        val updatedRows = userDao.updateFirebaseUid(trimmedUsername, uid)
        Log.d(tag, "UID link result: username=$trimmedUsername, rowsUpdated=$updatedRows")
        return updatedRows == 1

    }
    suspend fun isFirebaseUidLinked(
        username: String,
        firebaseUid: String
    ): Boolean {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || firebaseUid.isBlank()) return false

        val user = userDao.findByUsername(trimmedUsername) ?: return false
        val matches = user.firebaseUid == firebaseUid

        Log.d(tag, "Firebase UID verification: username=$trimmedUsername, matches=$matches")
        return matches
    }

    suspend fun seedDefaultUserIfEmpty() {
        if (userDao.count() == 0) {
            userDao.insert(UserEntity(username = "admin", password = "password123"))
            Log.i(tag, "Seeded default admin account")
        }
    }

    // No account selected means no account records are displayed
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

        val rowId = categoryDao.insert(
            CategoryEntity(name = trimmed, ownerUsername = owner)
        )
        val inserted = rowId != -1L

        if (inserted) {
            queueChange(owner, "category", "upsert", trimmed)
        }

        return inserted
    }

    suspend fun deleteCategory(category: String) {
        val owner = requireActiveUser() ?: return

        // Preserve expenses when removing a category
        categoryDao.deleteByName(owner, category)

        // Queue the category deletion for the correct account
        queueChange(owner, "category", "delete", category)
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        val owner = requireActiveUser() ?: return

        if (categoryDao.count(owner) == 0) {
            listOf("Groceries", "Rent", "Utilities").forEach { name ->
                categoryDao.insert(
                    CategoryEntity(name = name, ownerUsername = owner)
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<Expense>> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(emptyList())
            else expenseDao.observeAll(owner)
        }

    suspend fun addExpense(expense: Expense) {
        val owner = requireActiveUser() ?: return

        // Always assign a new expense to the currently active account
        val toInsert = expense.copy(id = 0, ownerUsername = owner)
        val newId = expenseDao.insert(toInsert)

        if (newId > 0L) {
            queueChange(owner, "expense", "upsert", newId.toString())
        }
    }

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
        queueChange(owner, "budget", "upsert", "current")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProgress: Flow<UsersProgress> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) {
                flowOf(UsersProgress())
            } else {
                userProgressDao.observeCurrent(owner).map { progress ->
                    progress ?: UsersProgress(ownerUsername = owner)
                }
            }
        }

    suspend fun saveProgress(progress: UsersProgress) {
        val owner = requireActiveUser() ?: return

        userProgressDao.upsert(
            progress.copy(
                id = UsersProgress.SINGLE_ROW_ID,
                ownerUsername = owner
            )
        )
        queueChange(owner, "progress", "upsert", "current")
    }

    suspend fun deleteExpense(expense: Expense) {
        val owner = requireActiveUser() ?: return

        // Don't delete an expense that belongs to another account
        val ownedExpense = expenseDao.getById(owner, expense.id) ?: return

        queueChange(owner, "expense", "delete", ownedExpense.id.toString())
        expenseDao.delete(ownedExpense)
    }

    suspend fun queueExistingDataForSync() {
        val owner = requireActiveUser() ?: return

        val categories = categoryDao.observeAll(owner).first()
        val expenses = expenseDao.observeAll(owner).first()
        val budget = budgetGoalDao.observeCurrent(owner).first()
        val progress = userProgressDao.getCurrent(owner)

        for (category in categories) {
            queueChange(owner, "category", "upsert", category.name)
        }
        for (expense in expenses) {
            queueChange(owner, "expense", "upsert", expense.id.toString())
        }
        if (budget != null) queueChange(owner, "budget", "upsert", "current")
        if (progress != null) queueChange(owner, "progress", "upsert", "current")
    }
}

