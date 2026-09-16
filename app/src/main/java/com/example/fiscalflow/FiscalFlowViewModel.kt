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
        // Populate default account & categories if database is brand new
        viewModelScope.launch {
            repository.seedDefaultUserIfEmpty()
            repository.seedDefaultCategoriesIfEmpty()
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
            onResult(repository.addCategory(name))
        }
    }

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
            // Reward 10 XP whenever a new target budget is saved
            val current = userProgress.value
            repository.saveProgress(current.copy(xp = current.xp + 10))
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
