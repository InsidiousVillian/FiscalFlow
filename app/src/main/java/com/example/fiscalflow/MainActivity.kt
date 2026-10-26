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
import androidx.compose.ui.graphics.Color
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

                val activeUsername by vm.activeUsername.collectAsStateWithLifecycle()
                val categories by vm.categories.collectAsStateWithLifecycle()
                val expenses by vm.expenses.collectAsStateWithLifecycle()
                val budgetGoal by vm.budgetGoal.collectAsStateWithLifecycle()
                @Suppress("UNUSED_VARIABLE")
                val userProgress by vm.userProgress.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf(if (activeUsername.isNullOrBlank()) "welcome" else "dashboard") }
                var graphsReturnScreen by remember { mutableStateOf("dashboard") }

                val loggedInUsername = activeUsername.orEmpty()

                // Automatically navigate to dashboard when a session exists
                LaunchedEffect(activeUsername) {
                    if (!activeUsername.isNullOrBlank() && (currentScreen == "welcome" || currentScreen == "login" || currentScreen == "signup")) {
                        currentScreen = "dashboard"
                    }
                }

                val totalSpent = expenses.sumOf { it.amount }
                val spendingProgress = budgetGoal?.let {
                    calculateProgress(totalSpent = totalSpent, maximum = it.maximum) } ?: 0f

                ProvideAppScrollbar {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                    when (currentScreen) {
                        "welcome" -> {
                            WelcomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onGetStarted = { currentScreen = "signup" },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLogin = { username, password, onResult ->
                                    scope.launch {
                                        onResult(vm.login(username, password))
                                    }
                                },
                                onLoginSuccess = { username ->
                                    vm.setActiveUser(username)
                                    currentScreen = "dashboard"
                                },
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
                                onSignUpSuccess = { username ->
                                    vm.setActiveUser(username)
                                    currentScreen = "dashboard"
                                },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        "dashboard" -> {
                            DashboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                onTransactionsClick = { currentScreen = "transactions" },
                                onBudgetClick = { currentScreen = "budget" },
                                onProgressReportClick = { currentScreen = "progress" },
                                onSpendingGraphsClick = {
                                    graphsReturnScreen = "dashboard"
                                    currentScreen = "graphs"
                                },
                                onGoalsClick = { currentScreen = "categories" },
                                onGamificationClick = { currentScreen = "gamification" },
                                onProfileClick = { currentScreen = "profile" }
                            )
                        }
                        "transactions" -> {
                            TransactionsScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                onDeleteExpense = { expense -> vm.deleteExpense(expense) },
                                onAddExpense = { currentScreen = "expense" },
                                onManageCategories = { currentScreen = "categories" },
                                onBackToDashboard = { currentScreen = "dashboard" }
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
                                onAddCategory = { name, onResult -> vm.addCategory(name, onResult) },
                                onDeleteCategory = { category -> vm.deleteCategory(category) },
                                onLogout = {
                                    vm.clearActiveUser()
                                    currentScreen = "welcome"
                                },
                                onAddExpense = { currentScreen = "expense" },
                                onViewHistory = { currentScreen = "history" },
                                onViewGraphs = {
                                    graphsReturnScreen = "categories"
                                    currentScreen = "graphs"
                                },
                                onBackToDashboard = { currentScreen = "dashboard" }
                            )
                        }
                        "expense" -> {
                            ExpenseScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                onExpenseSaved = { expense ->
                                    vm.addExpense(expense)
                                    currentScreen = "transactions"},
                                onBack = { currentScreen = "transactions" }
                            )
                        }
                        "progress" -> {
                            ProgressReportScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                budgetGoal = budgetGoal,
                                onBackToDashboard = { currentScreen = "dashboard" }
                            )
                        }
                        "budget" -> BudgetingScreen(
                            modifier = Modifier.padding(innerPadding),
                            currentGoal = budgetGoal,
                            totalSpent = totalSpent,
                            currentStreak = userProgress.streak,
                            onGoalSaved = { goal ->vm.saveGoal(goal)},
                            onBack = {currentScreen = "dashboard"}
                        )
                        "graphs" -> {
                            SpendingGraphsScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                budgetGoal = budgetGoal,
                                onBack = { currentScreen = graphsReturnScreen }
                            )
                        }
                        "history" -> {
                            ExpensesHistoryScreen(
                                Modifier.padding(innerPadding),
                                expenses = expenses,
                                onBack = { currentScreen = "categories" }
                            )
                        }
                        "gamification" -> {
                            GamificationScreen(
                                modifier = Modifier.padding(innerPadding),
                                userProgress = userProgress,
                                onBackToDashboard = {currentScreen = "dashboard"}
                            )
                        }
                        "profile" -> {
                            ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                username = loggedInUsername,
                                profileImageUri = null,
                                onBackToDashboard = {currentScreen = "dashboard" },
                                onLogout = {
                                    vm.clearActiveUser()
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}
