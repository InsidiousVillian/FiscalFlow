
package com.example.fiscalflow

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fiscalflow.data.FiscalFlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel exposing Room database data as StateFlows to the UI components
class FiscalFlowViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: FiscalFlowRepository =
        (app as FiscalFlowApp).repository

    //  Forward account selection to the repository
    fun setActiveUser(username: String) {
        repository.setActiveUser(username)

        // TEMPORARY DIAGNOSTIC: log the Firebase UID, if signed in.
        android.util.Log.d(
            "FiscalFlowUID",
            "Local username=$username, Firebase UID=${FirebaseIdentity.currentUid()}"
        )
    }

    //Clear account selection when the user logs out
    fun clearActiveUser() {
        repository.clearActiveUser()
    }

    init {
        // Cloud restore/upload is paused while local account isolation is completed
        //  Existing local financial records are left untouched
    }

    // Keep database flows warm while UI is active
    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgetGoal: StateFlow<BudgetingGoal?> = repository.budgetGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val userProgress: StateFlow<UsersProgress> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UsersProgress())

    // Actions called by UI screens

    suspend fun login(username: String, password: String): Boolean {
        // First verify the existing local account.
        val localAuthenticated = repository.authenticate(username, password)
        if (!localAuthenticated) return false

        // Never reuse a Firebase session belonging to another account.
        FirebaseIdentity.signOut()

        // Link only after Firebase successfully authenticates this username.
        return try {
            val uid = FirebaseIdentity.signIn(username, password)
            val linked = repository.linkFirebaseUid(username, uid)

            if (!linked) {
                // A conflicting UID must never be used for cloud sync.
                FirebaseIdentity.signOut()
                true
            } else {
                // Verify the UID saved in the local database.
                val verified = repository.isFirebaseUidLinked(username, uid)

                // Sign out if the saved identity does not match.
                if (!verified) {
                    FirebaseIdentity.signOut()
                }

                // Local login still works, but a failed link stays offline.
                true
            }
        } catch (e: Exception) {
            // Firebase may be unavailable; keep local login available.
            FirebaseIdentity.signOut()
            true
        }
    }

    suspend fun signUp(username: String, password: String): Boolean {
        // Create the local account without replacing existing accounts.
        val created = repository.registerUser(username, password)
        if (!created) return false

        //SignUpScreen signs into Firebase by creating the account first.
        val uid = FirebaseIdentity.currentUid() ?: return true

        // Link the new local account to the currently authenticated UID.
        val linked = repository.linkFirebaseUid(username, uid)

        if (!linked) {
            // Do not retain a Firebase session if linking fails.
            FirebaseIdentity.signOut()
        }

        //  Preserve the locally created account if linking fails.
        return true
    }


    fun addCategory(name: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val added = repository.addCategory(name)
            // Cloud sync intentionally paused
            onResult(added)
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            try {
                // Delete locally only; do not access Firebase here
                repository.deleteCategory(category)
            } catch (e: Exception) {
                Log.w(
                    "FiscalFlowViewModel",
                    "Local category deletion failed",
                    e
                )
            }
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            // Cloud sync intentionally paused
        }
    }

    fun saveGoal(goal: BudgetingGoal) {
        viewModelScope.launch {
            repository.saveGoal(goal)

            // Reward 10 XP whenever a budget target is saved
            val current = userProgress.value
            repository.saveProgress(current.copy(xp = current.xp + 10))

            // Cloud sync intentionally paused
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            // Cloud sync intentionally paused
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                            as FiscalFlowApp

                FiscalFlowViewModel(app)
            }
        }
    }
}

