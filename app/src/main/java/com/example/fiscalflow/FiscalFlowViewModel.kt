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

/**
 * ViewModel exposed to Compose. Owns the repository and turns the DAO Flows into StateFlows
 * so screens can just call `collectAsState()` to render the latest data.
 *
 * All write operations are launched on viewModelScope, which is cancelled automatically when
 * the ViewModel is cleared. That means no leaks even if the user rotates the device mid-save.
 */
class FiscalFlowViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: FiscalFlowRepository =
        (app as FiscalFlowApp).repository

    init {
        // Seed defaults once, on first launch, so the login screen and category list aren't empty.
        viewModelScope.launch {
            repository.seedDefaultUserIfEmpty()
            repository.seedDefaultCategoriesIfEmpty()
        }
    }

    // stateIn converts a cold Flow (only runs while collected) into a hot StateFlow with a
    // cached "latest" value. SharingStarted.WhileSubscribed(5000) keeps the DB query alive for
    // 5 seconds after the last collector goes away — cheap way to survive quick config changes.
    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgetGoal: StateFlow<BudgetingGoal?> = repository.budgetGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val userProgress: StateFlow<UsersProgress> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UsersProgress())

    // ---------- Actions the UI calls ----------

    /** Returns true on success, false if the credentials do not match a stored user. */
    suspend fun login(username: String, password: String): Boolean =
        repository.authenticate(username, password)

    /** Returns true on success, false if the username is already taken. */
    suspend fun signUp(username: String, password: String): Boolean =
        repository.registerUser(username, password)

    fun addCategory(name: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.addCategory(name))
        }
    }
    // Delete a category and its saved expenses
    fun deleteCategory(category: String) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.addExpense(expense) }
    }

    fun saveGoal(goal: BudgetingGoal) {
        viewModelScope.launch {
            repository.saveGoal(goal)
            // Award XP for setting a goal — same behaviour that MainActivity had inline before.
            val current = userProgress.value
            repository.saveProgress(current.copy(xp = current.xp + 10))
        }
    }

    companion object {
        /**
         * Custom factory so we can pass the Application to the AndroidViewModel constructor.
         * Compose picks this up automatically via `viewModel(factory = FiscalFlowViewModel.Factory)`.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as FiscalFlowApp
                FiscalFlowViewModel(app)
            }
        }
    }
}
