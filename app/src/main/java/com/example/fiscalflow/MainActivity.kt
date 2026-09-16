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
                // Initial screen state
                var currentScreen by remember { mutableStateOf("welcome") }
                var loggedInUsername by remember { mutableStateOf("") }

                // Derived values recomputed automatically whenever expenses/goal change in the DB.
                val totalSpent = expenses.sumOf { it.amount }
                val spendingProgress = budgetGoal?.let {
                    calculateProgress(totalSpent = totalSpent, maximum = it.maximum) } ?: 0f

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        // Welcome screen
                        "welcome" -> {
                            WelcomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onGetStarted = { currentScreen = "signup" },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        // Login screen
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLogin = { username, password, onResult ->
                                    // Room DAOs are suspend, so hop into a coroutine and post the result back.
                                    scope.launch {
                                        onResult(vm.login(username, password))
                                    }
                                },
                                onLoginSuccess = { username ->
                                    loggedInUsername = username
                                    currentScreen = "dashboard"
                                },
                                onNavigateToSignUp = { currentScreen = "signup" }
                            )
                        }
                        //Sign up screen
                        "signup" -> {
                            SignUpScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSignUp = { username, password, onResult ->
                                    //Create the account on Room
                                    scope.launch {
                                        onResult(vm.signUp(username, password))
                                    }
                                },
                                //Go to dashboard when signed in
                                onSignUpSuccess = { username ->
                                    loggedInUsername = username
                                    currentScreen = "dashboard"
                                },
                                //Return to log in screen
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        //Dashboard
                        "dashboard" -> {
                            DashboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                // Send the existing expenses to Dashboard.
                                expenses = expenses,
                                // Open Transactions.
                                onTransactionsClick = { currentScreen = "transactions" },
                                // Open Budget.
                                onBudgetClick = { currentScreen = "budget" },
                                // Open Goals.
                                onGoalsClick = { currentScreen = "categories" },
                                // Open XP & Milestones.
                                onGamificationClick = { currentScreen = "gamification" },
                                // Open Profile.
                                onProfileClick = { currentScreen = "profile" }
                            )
                        }
                        //Transactions screen
                        "transactions" -> {
                            TransactionsScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                onAddExpense = { currentScreen = "expense" },
                                onBackToDashboard = { currentScreen = "dashboard" }
                            )
                        }
                        //Category screen
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
                                    loggedInUsername = ""
                                    currentScreen = "welcome"
                                },
                                onAddExpense = { currentScreen = "expense" },
                                onViewHistory = { currentScreen = "history" },
                                onBackToDashboard = { currentScreen = "dashboard" }
                            )
                        }
                        //Expense screen
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
                        //Budget screen
                        "budget" -> BudgetingScreen(
                            modifier = Modifier.padding(innerPadding),
                            currentGoal = budgetGoal,
                            totalSpent = totalSpent,
                            currentStreak = userProgress.streak,
                            onGoalSaved = { goal ->vm.saveGoal(goal)},
                            onBack = {currentScreen = "dashboard"}
                        )
                        //History screen
                        "history" -> {
                            ExpensesHistoryScreen(
                                Modifier.padding(innerPadding),
                                expenses = expenses,
                                onBack = { currentScreen = "categories" }
                            )
                        }
                        //Gamified screen
                        "gamification" -> {
                            GamificationScreen(
                                modifier = Modifier.padding(innerPadding),
                                userProgress = userProgress,
                                onBackToDashboard = {currentScreen = "dashboard"}
                            )
                        }
                        //Profile screen
                        "profile" -> {
                            ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                // Send the username of the currently logged-in user
                                username = loggedInUsername,
                                // No profile image is currently connected
                                profileImageUri = null,
                                // Return to dashboard
                                onBackToDashboard = {currentScreen = "dashboard" },
                                // Log out
                                onLogout = {
                                    loggedInUsername = ""
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
