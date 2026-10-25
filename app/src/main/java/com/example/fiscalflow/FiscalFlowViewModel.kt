package com.example.fiscalflow

import android.app.Application
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

// ViewModel exposing room database data as StateFlows to the UI components.
class FiscalFlowViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: FiscalFlowRepository =
        (app as FiscalFlowApp).repository


    init {
        viewModelScope.launch {
            val app = getApplication<FiscalFlowApp>()

            try {
                val hasLocalData = app.hasLocalFinancialData()
                val hasCloudBackup = app.hasCloudBackup()

                when {
                    // Restore the cloud copy only when this device
                    // has no existing financial data.
                    !hasLocalData && hasCloudBackup -> {
                        app.restoreCloudData()
                    }

                    // Keep local data and upload it when available.
                    hasLocalData -> {
                        app.syncLocalDataToCloud()
                    }

                    // Brand-new installation with no cloud backup.
                    else -> {
                        repository.seedDefaultUserIfEmpty()
                        repository.seedDefaultCategoriesIfEmpty()
                        app.syncLocalDataToCloud()
                    }
                }
            } catch (e: Exception) {
                // Keep the app usable if the internet or cloud is unavailable.
                android.util.Log.w(
                    "FiscalFlowViewModel",
                    "Startup sync failed; keeping local data available",
                    e
                )
            }
        }
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

    suspend fun login(username: String, password: String): Boolean =
        repository.authenticate(username, password)

    suspend fun signUp(username: String, password: String): Boolean =
        repository.registerUser(username, password)

    fun addCategory(name: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val added = repository.addCategory(name)
            if (added) {
                getApplication<FiscalFlowApp>().syncLocalDataToCloud()
            }
            onResult(added)
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            val app = getApplication<FiscalFlowApp>()

            try {
                // Remove the category and its associated expenses from Room.
                repository.deleteCategory(category)

                // Remove the same category and its associated expenses from Firebase.
                FiscalFlowCloudSync.deleteCloudCategory(category)

                // Upload the remaining local records.
                app.syncLocalDataToCloud()
            } catch (e: Exception) {
                // Preserve local deletion even if cloud access fails.
                android.util.Log.w(
                    "FiscalFlowViewModel",
                    "Category deleted locally; cloud deletion needs retry",
                    e
                )

                // Queue an upload attempt when a network connection is available.
                app.syncLocalDataToCloud()
            }
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            getApplication<FiscalFlowApp>().syncLocalDataToCloud()
        }
    }

    fun saveGoal(goal: BudgetingGoal) {
        viewModelScope.launch {
            repository.saveGoal(goal)

            // Reward 10 XP whenever a new target budget is saved
            val current = userProgress.value
            repository.saveProgress(current.copy(xp = current.xp + 10))

            getApplication<FiscalFlowApp>().syncLocalDataToCloud()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)

            // Attempt to sync after the local deletion.
            getApplication<FiscalFlowApp>().syncLocalDataToCloud()
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as FiscalFlowApp
                FiscalFlowViewModel(app)
            }
        }
    }
}
