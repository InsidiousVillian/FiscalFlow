package com.example.fiscalflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
                var graphsReturnScreen by remember { mutableStateOf("dashboard") }
                // Jet: in-app popup after an expense pushes monthly spend to 80% or over max.
                var spendingAlertPopup by remember { mutableStateOf<SpendingAlert?>(null) }
                val context = LocalContext.current
                val currentSpendingAlert = evaluateSpendingAlert(expenses, budgetGoal)
                val notificationPermission = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

                // Jet: ask for POST_NOTIFICATIONS on Android 13+ after login.
                LaunchedEffect(loggedInUsername) {
                    if (loggedInUsername.isBlank()) return@LaunchedEffect
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Derived values recomputed automatically whenever expenses/goal change in the DB.
                val totalSpent = expenses.sumOf { it.amount }
                val spendingProgress = budgetGoal?.let {
                    calculateProgress(totalSpent = totalSpent, maximum = it.maximum) } ?: 0f

                ProvideAppScrollbar {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                    when (currentScreen) {
                        // Jet: app starts here (WelcomeScreen — GET STARTED / Log in).
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
                                    vm.setActiveUser(username)
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
                                    vm.setActiveUser(username)
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
                                spendingAlert = currentSpendingAlert,
                                // Open Transactions.
                                onTransactionsClick = { currentScreen = "transactions" },
                                // Open Budget.
                                onBudgetClick = { currentScreen = "budget" },
                                // Open Progress Report.
                                onProgressReportClick = { currentScreen = "progress" },
                                // Open Spending Graphs.
                                onSpendingGraphsClick = {
                                    graphsReturnScreen = "dashboard"
                                    currentScreen = "graphs"
                                },
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
                                onDeleteExpense = { expense -> vm.deleteExpense(expense) },
                                onAddExpense = { currentScreen = "expense" },
                                onBackToDashboard = { currentScreen = "dashboard" },
                                onManageCategories = { currentScreen = "categories"}
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
                                    vm.clearActiveUser()
                                    loggedInUsername = ""
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
                        //Expense screen
                        "expense" -> {
                            ExpenseScreen(
                                modifier = Modifier.padding(innerPadding),
                                categories = categories,
                                onExpenseSaved = { expense ->
                                    vm.addExpense(expense)
                                    // Jet: income does not count toward the monthly max alert.
                                    if (!expense.isIncome) {
                                        val projected = expenses + expense
                                        val alert = evaluateSpendingAlert(projected, budgetGoal)
                                        if (alert.level != SpendingAlertLevel.None) {
                                            SpendingNotifier.notify(this@MainActivity, alert)
                                            spendingAlertPopup = alert
                                        }
                                    }
                                    currentScreen = "transactions"
                                },
                                onBack = { currentScreen = "transactions" }
                            )
                        }
                        // Jet: Progress Report screen.
                        "progress" -> {
                            ProgressReportScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                budgetGoal = budgetGoal,
                                onBackToDashboard = { currentScreen = "dashboard" }
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
                        // Spending graphs
                        "graphs" -> {
                            SpendingGraphsScreen(
                                modifier = Modifier.padding(innerPadding),
                                expenses = expenses,
                                categories = categories,
                                budgetGoal = budgetGoal,
                                onBack = { currentScreen = graphsReturnScreen }
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
                                    vm.clearActiveUser()
                                    loggedInUsername = ""
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                    }
                }

                // Jet: popup after save; View report opens Progress Report.
                spendingAlertPopup?.let { alert ->
                    AlertDialog(
                        onDismissRequest = { spendingAlertPopup = null },
                        title = { Text(alert.title) },
                        text = { Text(alert.message) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    spendingAlertPopup = null
                                    currentScreen = "progress"
                                }
                            ) {
                                Text("View report")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { spendingAlertPopup = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}
