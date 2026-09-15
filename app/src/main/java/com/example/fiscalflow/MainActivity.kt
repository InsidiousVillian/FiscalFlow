package com.example.fiscalflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fiscalflow.ui.theme.FiscalFlowTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiscalFlowTheme {
                // Single ViewModel scoped to this Activity. It exposes StateFlows backed by Room.
                val vm: FiscalFlowViewModel = viewModel(factory = FiscalFlowViewModel.Factory)
                val scope = rememberCoroutineScope()

                // collectAsStateWithLifecycle stops collecting when the Activity is stopped,
                // which avoids battery drain and unnecessary DB reads in the background.
                val categories by vm.categories.collectAsStateWithLifecycle()
                val expenses by vm.expenses.collectAsStateWithLifecycle()
                val budgetGoal by vm.budgetGoal.collectAsStateWithLifecycle()
                // userProgress is observed so future screens can show XP/streak; safe to leave unused for now.
                @Suppress("UNUSED_VARIABLE")
                val userProgress by vm.userProgress.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf("login") }

                // Derived values recomputed automatically whenever expenses/goal change in the DB.
                val totalSpent = expenses.sumOf { it.amount }
                val spendingProgress = budgetGoal?.let {
                    calculateProgress(totalSpent = totalSpent, maximum = it.maximum)
                } ?: 0f

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLogin = { username, password, onResult ->
                                    // Room DAOs are suspend, so hop into a coroutine and post the result back.
                                    scope.launch {
                                        onResult(vm.login(username, password))
                                    }
                                },
                                onLoginSuccess = { currentScreen = "categories" },
                                onNavigateToSignUp = { currentScreen = "signup" }
                            )
                        }
                        "signup" -> {
                            SignUpScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSignUp = { username, password, onResult ->
                                    scope.launch {
                                        onResult(vm.signUp(username, password))
                                    }
                                },
                                onSignUpSuccess = { currentScreen = "categories" },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        "categories" -> {
                            CategoryScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                expenses = expenses,
                                budgetGoal = budgetGoal,
                                totalSpent = totalSpent,
                                spendingProgress = spendingProgress,
                                onAddCategory = { name, onResult ->
                                    vm.addCategory(name, onResult)
                                },
                                onLogout = { currentScreen = "login" },
                                onAddExpense = { currentScreen = "expense" },
                                onOpenGoals = { currentScreen = "goal" },
                                onViewHistory = { currentScreen = "history" }
                            )
                        }
                        "expense" -> {
                            ExpenseScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                onExpenseSaved = { expense ->
                                    vm.addExpense(expense)
                                    currentScreen = "categories"
                                },
                                onBack = { currentScreen = "categories" }
                            )
                        }
                        "goal" -> {
                            BudgetingScreen(
                                Modifier.padding(innerPadding),
                                currentGoal = budgetGoal,
                                onGoalSaved = { goal ->
                                    vm.saveGoal(goal)
                                    currentScreen = "categories"
                                },
                                onBack = { currentScreen = "categories" }
                            )
                        }
                        "history" -> {
                            ExpensesHistoryScreen(
                                Modifier.padding(innerPadding),
                                expenses = expenses,
                                onBack = { currentScreen = "categories" }
                            )
                        }
                    }
                }
            }
        }
    }
}
