package com.example.fiscalflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage

// FiscalFlow colour palette
private val DarkBlue = androidx.compose.ui.graphics.Color(0xFF172554)
private val Blue = androidx.compose.ui.graphics.Color(0xFF3B82F6)
private val Lavender = androidx.compose.ui.graphics.Color(0xFFEDE9FE)
private val LightLavender = androidx.compose.ui.graphics.Color(0xFFF7F5FF)
private val Grey = androidx.compose.ui.graphics.Color(0xFF64748B)
private val White = androidx.compose.ui.graphics.Color.White

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    // Data now comes from Room via ViewModel StateFlows — plain read-only Lists are enough.
    categories: List<String>,
    expenses: List<Expense>,
    budgetGoal: BudgetingGoal?,
    totalSpent: Double,
    spendingProgress: Float,
    // Writes go through this callback so the ViewModel can persist into Room; result reports
    // whether the insert actually happened (false = duplicate name).
    onAddCategory: (name: String, result: (Boolean) -> Unit) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onLogout: () -> Unit,
    onAddExpense: () -> Unit,
    onViewHistory: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // null = show category list, otherwise show expenses for that category
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Goals",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            TextButton(onClick = onLogout) {
                Text(
                    "Log Out",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Blue
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Monthly Spending Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkBlue
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Budget Goal Progress",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "This is your spending goal tracker — not the period review.",
                    fontSize = 12.sp,
                    color = Lavender
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "R%.2f / R%.2f".format(
                        totalSpent,
                        budgetGoal?.maximum ?: 0.0
                    ),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )

                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { spendingProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Blue,
                    trackColor = Lavender
                )

                Spacer(modifier = Modifier.height(9.dp))

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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main actions (kept outside the category list so they only appear once)
        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ) {
            Text(
                "Add Expense",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Button(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ) {
            Text(
                "Review Category Totals by Period",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(7.dp))

        Button(
            onClick = onBackToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ) {
            Text(
                "← Dashboard",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Pick This Week, This Month, or a Custom date range",
            fontSize = 11.sp,
            color = Grey,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

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
                label = {
                    Text(
                        "New Category",
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Button(
                onClick = {
                    val trimmed = categoryName.trim()
                    when {
                        trimmed.isBlank() -> errorMessage = "Category name cannot be empty"
                        // Cheap client-side duplicate check so we can show an error immediately;
                        // the DAO is still the source of truth (PRIMARY KEY on name).
                        categories.any { it.equals(trimmed, ignoreCase = true) } ->
                            errorMessage = "Category already exists"
                        else -> {
                            onAddCategory(trimmed) { inserted ->
                                if (inserted) {
                                    categoryName = ""
                                    errorMessage = null
                                } else {
                                    errorMessage = "Category already exists"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Add",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Your Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Tap a category to view its expenses",
            fontSize = 11.sp,
            color = Grey
        )

        Spacer(modifier = Modifier.height(7.dp))

        // Display Category List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val count = expenses.count { it.category == category }
                val categoryTotal = expenses
                    .filter { it.category == category }
                    .sumOf { it.amount }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = category },
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Visual category icon
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(52.dp)
                                .background(
                                    Lavender,
                                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎯",
                                fontSize = 25.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Category information
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$count expense(s)",
                                fontSize = 12.sp,
                                color = Grey
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "R %.2f spent".format(categoryTotal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                        }

                        // Delete category
                        IconButton(
                            onClick = {
                                onDeleteCategory(category)

                                if (selectedCategory == category) {
                                    selectedCategory = null
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete $category category",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Shows expenses saved under one category
    @Composable
    fun CategoryExpensesView(
        modifier: Modifier = Modifier,
        categoryName: String,
        expenses: List<Expense>,
        onBack: () -> Unit
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )

                TextButton(onClick = onBack) {
                    Text(
                        "Back",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (expenses.isEmpty()) {
                Text(
                    text = "No expenses in this category yet.",
                    fontSize = 14.sp,
                    color = Grey
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = LightLavender
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(15.dp)
                            ) {
                                Text(
                                    text = "Amount: R${expense.amount}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Description: ${expense.description}",
                                    fontSize = 13.sp,
                                    color = Grey
                                )

                                Text(
                                    text = "Start date: ${expense.startDate}",
                                    fontSize = 13.sp,
                                    color = Grey
                                )

                                Text(
                                    text = "End date: ${expense.endDate}",
                                    fontSize = 13.sp,
                                    color = Grey
                                )

                                if (!expense.photoUri.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    AsyncImage(
                                        model = expense.photoUri.toUri(),
                                        contentDescription = "Expense receipt",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
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
}