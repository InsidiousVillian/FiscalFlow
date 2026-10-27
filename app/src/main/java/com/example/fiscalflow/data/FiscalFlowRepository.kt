package com.example.fiscalflow.data

import android.content.SharedPreferences
import android.util.Log
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.Expense
import com.example.fiscalflow.FirebaseIdentity
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Locale

import com.example.fiscalflow.FiscalFlowCloudSync

// Repository managing online-only financial data flow between Cloud Firestore and the ViewModel.
class FiscalFlowRepository(
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
        setActiveUser(trimmed)
        Log.d(tag, "registerUser: session created for '$trimmed'")
        return true
    }

    suspend fun authenticate(username: String, password: String): Boolean {
        val trimmed = username.trim()
        if (trimmed.isBlank() || password.isBlank()) return false
        setActiveUser(trimmed)
        Log.d(tag, "authenticate('$trimmed') -> session active")
        return true
    }

    val defaultCategories = listOf("")

    // Unified category list shared by both Expenses and Income
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: Flow<List<String>> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) {
                flowOf(defaultCategories)
            } else {
                FiscalFlowCloudSync.observeCloudCategories(owner).map { list ->
                    val custom = list ?: emptyList()
                    (defaultCategories + custom).distinctBy { it.lowercase(Locale.ROOT) }
                }
            }
        }

    val incomeCategories: Flow<List<String>> = categories

    suspend fun addCategory(name: String): Boolean {
        val owner = requireActiveUser() ?: return false
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        FiscalFlowCloudSync.uploadCategoryDirect(owner, trimmed)
        Log.d(tag, "addCategory('$trimmed') uploaded to Firestore for $owner")
        return true
    }

    suspend fun addIncomeCategory(name: String): Boolean {
        val owner = requireActiveUser() ?: return false
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        FiscalFlowCloudSync.uploadIncomeCategoryDirect(owner, trimmed)
        Log.d(tag, "addIncomeCategory('$trimmed') uploaded to Firestore for $owner")
        return true
    }

    // Remove category along with any associated expenses for active account
    suspend fun deleteCategory(category: String) {
        val owner = requireActiveUser() ?: return
        FiscalFlowCloudSync.deleteCloudCategoryDirect(owner, category)
        Log.d(tag, "deleteCategory('$category') completed for $owner")
    }

    suspend fun deleteIncomeCategory(category: String) {
        val owner = requireActiveUser() ?: return
        FiscalFlowCloudSync.deleteCloudIncomeCategoryDirect(owner, category)
        Log.d(tag, "deleteIncomeCategory('$category') completed for $owner")
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        val owner = requireActiveUser() ?: return
        FiscalFlowCloudSync.uploadCategoryDirect(owner, "Groceries")
        FiscalFlowCloudSync.uploadCategoryDirect(owner, "Rent")
        FiscalFlowCloudSync.uploadCategoryDirect(owner, "Utilities")
    }

    // Expenses observed directly from Cloud Firestore for active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<Expense>> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(emptyList())
            else FiscalFlowCloudSync.observeCloudExpenses(owner).map { list ->
                list ?: emptyList()
            }
        }

    suspend fun addExpense(expense: Expense): Long {
        val owner = requireActiveUser() ?: return -1L
        val generatedId = if (expense.id > 0L) expense.id else System.currentTimeMillis()
        val toInsert = expense.copy(id = generatedId, ownerUsername = owner)
        FiscalFlowCloudSync.uploadExpenseDirect(owner, toInsert)
        Log.d(tag, "addExpense saved to Firestore id=$generatedId amount=${expense.amount} category=${expense.category} owner=$owner")
        return generatedId
    }

    // Budget goal observed directly from Cloud Firestore for active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetGoal: Flow<BudgetingGoal?> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(null)
            else FiscalFlowCloudSync.observeCloudBudget(owner)
        }

    suspend fun saveGoal(goal: BudgetingGoal) {
        val owner = requireActiveUser() ?: return
        val toSave = goal.copy(
            id = BudgetingGoal.SINGLE_ROW_ID,
            ownerUsername = owner
        )
        FiscalFlowCloudSync.uploadBudgetDirect(owner, toSave)
        Log.d(tag, "saveGoal min=${goal.minimum} max=${goal.maximum} owner=$owner")
    }

    // User progress tracking observed directly from Cloud Firestore for active account
    @OptIn(ExperimentalCoroutinesApi::class)
    val userProgress: Flow<UsersProgress> =
        activeUsername.flatMapLatest { owner ->
            if (owner == null) flowOf(UsersProgress())
            else FiscalFlowCloudSync.observeCloudProgress(owner).map { it ?: UsersProgress(ownerUsername = owner) }
        }

    suspend fun saveProgress(progress: UsersProgress) {
        val owner = requireActiveUser() ?: return
        val toSave = progress.copy(
            id = UsersProgress.SINGLE_ROW_ID,
            ownerUsername = owner
        )
        FiscalFlowCloudSync.uploadProgressDirect(owner, toSave)
    }

    suspend fun deleteExpense(expense: Expense) {
        val owner = requireActiveUser() ?: return
        FiscalFlowCloudSync.deleteCloudExpenseDirect(owner, expense.id)
        Log.d(tag, "deleteExpense: removed expense id=${expense.id} owner=$owner")
    }
}
