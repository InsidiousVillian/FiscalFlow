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
                //Assign current screen
                var currentScreen by remember { mutableStateOf("login") }
                //Assign the username
                var loggedInUsername by remember { mutableStateOf("") }
                // Derived values recomputed automatically whenever expenses/goal change in the DB.
                val totalSpent = expenses.sumOf { it.amount }
                val spendingProgress = budgetGoal?.let {
                    calculateProgress(totalSpent = totalSpent, maximum = it.maximum) } ?: 0f

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        //Login screen
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLogin = { username, password, onResult ->
                                    // Room DAOs are suspend, so hop into a coroutine and post the result back.
                                    scope.launch {
                                        onResult(vm.login(username, password))
                                    }
                                },
                                onLoginSuccess = { currentScreen = "dashboard" },
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
                                onSignUpSuccess = { currentScreen = "dashboard" },
                                //Return to login screen
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
                                onTransactionsClick = {
                                    currentScreen = "expense"
                                },

                                // Open Budget.
                                onBudgetClick = {
                                    currentScreen = "goal"
                                },

                                // Open Goals.
                                onGoalsClick = {
                                    currentScreen = "goal"
                                },

                                // Open XP & Milestones.
                                onGamificationClick = {
                                    currentScreen = "gamification"
                                },

                                // Open Profile.
                                onProfileClick = {
                                    currentScreen = "profile"
                                }
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
                                onAddCategory = { name, onResult ->
                                    vm.addCategory(name, onResult)
                                },
                                onLogout = { currentScreen = "login" },
                                onAddExpense = { currentScreen = "expense" },
                                onOpenGoals = { currentScreen = "goal" },
                                onViewHistory = { currentScreen = "history" }
                            )
                        }
                        //Expense screen
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
                        //Goal screen
                        "goal" -> {
                            BudgetingScreen(
                                Modifier.padding(innerPadding),
                                currentGoal = budgetGoal,
                                onGoalSaved = { goal ->
                                    vm.saveGoal(goal)
                                    currentScreen = "dashboard"
                                },
                                onBack = { currentScreen = "dashboard" }
                            )
                        }
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
                                userProgress = userProgress
                            )
                        }
                        //Profile screen
                        "profile" -> ProfileScreen(
                            modifier = Modifier.padding(innerPadding),
                            username = loggedInUsername,
                            profileImageUri = null,
                            onBackToDashboard = { currentScreen = "dashboard" },
                            onLogout = { currentScreen = "login" }
                        )
                    }
                }
            }
        }
    }
}
