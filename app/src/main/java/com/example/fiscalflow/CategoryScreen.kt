package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage

// Category theme colors
private val DarkBlue = Color(0xFF172554)
private val Blue = Color(0xFF3B82F6)
private val Lavender = Color(0xFFEDE9FE)
private val Grey = Color(0xFF64748B)
private val White = Color.White

// Category goals screen for creating categories and tracking category spending
@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    categories: List<String>,
    expenses: List<Expense>,
    budgetGoal: BudgetingGoal?,
    totalSpent: Double,
    spendingProgress: Float,
    onAddCategory: (name: String, result: (Boolean) -> Unit) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onLogout: () -> Unit,
    onAddExpense: () -> Unit,
    onViewHistory: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

        // Spending progress summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlue),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
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

                LinearProgressIndicator(
                    progress = { spendingProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Blue,
                    trackColor = Lavender
                )

                Spacer(modifier = Modifier.height(9.dp))

                budgetGoal?.let { goal ->
                    val message = when {
                        totalSpent < goal.minimum -> "🎯 You're below your minimum spending goal."
                        totalSpent <= goal.maximum -> "⭐ Great! You're within your spending goal."
                        else -> "⚠️ You've exceeded your maximum goal."
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

        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ) {
            Text("Add Expense", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
            Text("← Dashboard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = "Pick This Week, This Month, or a Custom date range",
            fontSize = 11.sp,
            color = Grey,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // New category input row
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
                label = { Text("New Category", fontSize = 13.sp) },
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
                Text("Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

        // Category list
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
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(52.dp)
                                .background(
                                    Lavender,
                                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 25.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
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
}
