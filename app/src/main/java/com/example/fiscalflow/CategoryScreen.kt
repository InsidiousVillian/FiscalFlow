package com.example.fiscalflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    categories: SnapshotStateList<String>, // shared with Expense screen
    expenses: SnapshotStateList<Expense>,  // saved expenses
    budgetGoal: BudgetingGoal?,
    totalSpent: Double,
    spendingProgress: Float,
    onLogout: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenGoals: () -> Unit,
    onViewHistory: () -> Unit

) {
    var categoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // null = show category list, otherwise show expenses for that category
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    if (selectedCategory != null) {
        CategoryExpensesView(
            modifier = modifier,
            categoryName = selectedCategory.orEmpty(),
            expenses = expenses.filter { it.category == selectedCategory },
            onBack = { selectedCategory = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Categories",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onLogout) {
                Text("Log Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opens the Expense screen

            Text("Expense History")
        }
        // Monthly Spending Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R%.2f / R%.2f".format(
                        totalSpent,
                        budgetGoal?.maximum ?: 0.0
                    ),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { spendingProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                budgetGoal?.let { goal ->

                    val message = when {
                        totalSpent < goal.minimum ->
                            "🎯 You're below your minimum spending goal."

                        totalSpent <= goal.maximum ->
                            "⭐ Great! You're within your spending goal."

                        else ->
                            "⚠️ You've exceeded your maximum goal."
                    }

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Input Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = categoryName,
                onValueChange = {
                    categoryName = it
                    errorMessage = null
                },
                label = { Text("New Category") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        errorMessage = "Category name cannot be empty"
                    } else if (categories.any { it.equals(categoryName.trim(), ignoreCase = true) }) {
                        errorMessage = "Category already exists"
                    } else {
                        categories.add(categoryName.trim())
                        categoryName = ""
                        errorMessage = null
                    }
                }
            ) {
                Text("Add")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Categories",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap a category to view its expenses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display Category List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val count = expenses.count { it.category == category }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = category },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "$count expense(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onAddExpense,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Expense")
                }
                Button(
                    onClick = onOpenGoals
                ) {
                    Text("Monthly Goals")
                }
                Button(
                    onClick = onViewHistory
                )
                {
            }
        }
    }
}

// Shows expenses saved under one category
@Composable
private fun CategoryExpensesView(
    modifier: Modifier = Modifier,
    categoryName: String,
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (expenses.isEmpty()) {
            Text(
                text = "No expenses in this category yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(expenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Amount: R${expense.amount}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Description: ${expense.description}")
                            Text(text = "Start date: ${expense.startDate}")
                            Text(text = "End date: ${expense.endDate}")

                            if (!expense.photoUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = expense.photoUri.toUri(),
                                    contentDescription = "Expense receipt",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
