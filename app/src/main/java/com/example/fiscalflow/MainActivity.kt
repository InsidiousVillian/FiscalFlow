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
import com.example.fiscalflow.ui.theme.FiscalFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiscalFlowTheme {
                var currentScreen by remember { mutableStateOf("login") }

                // temporary user credential deposit to hold usernames before database
                val userAccounts = remember { mutableStateMapOf("admin" to "password123") }

                // categories used on Category screen and Expense dropdown
                val categories = remember {
                    mutableStateListOf("Groceries", "Rent", "Utilities")
                }

                // temporary expenses list (no database yet)
                val expenses = remember { mutableStateListOf<Expense>() }
                // monthly spending goal
                var budgetGoal by remember {
                    mutableStateOf<BudgetingGoal?>(null)
                }

                Scaffold(modifier = Modifier.fillMaxSize())
                { innerPadding ->
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { currentScreen = "categories" },
                                onNavigateToSignUp = { currentScreen = "signup" },
                                userAccounts = userAccounts
                            )
                        }
                        "signup" -> {
                            SignUpScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSignUpSuccess = { newUsername, newPassword ->
                                    userAccounts[newUsername] = newPassword
                                    currentScreen = "categories"
                                },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        "categories" -> {
                            CategoryScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                expenses = expenses,
                                onLogout = { currentScreen = "login" },
                                onAddExpense = { currentScreen = "expense" }
                            )
                        }
                        "expense" -> {
                            ExpenseScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                onExpenseSaved = { expense ->
                                    expenses.add(expense)
                                    currentScreen = "categories" // go back after save
                                },
                                onBack = { currentScreen = "categories" }
                            )
                        }
                        "goal" -> {
                            BudgetingScreen(
                                modifier = Modifier.padding(innerPadding),
                                currentGoal = budgetGoal,

                                onGoalSaved = { goal ->
                                    budgetGoal = goal
                                    currentScreen = "categories"
                                },

                                onBack = {
                                    currentScreen = "categories"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
