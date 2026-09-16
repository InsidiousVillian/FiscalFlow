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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Theme colors
private val TransactionDarkBlue = Color(0xFF172A46)
private val TransactionLavender = Color(0xFFEDEBFA)
private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFE57373)

// Screen displaying list of recorded transactions, income/expense breakdown, and search filters
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    categories: List<String>,
    onAddExpense: () -> Unit = {},
    onBackToDashboard: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var selectedReceiptUri by remember { mutableStateOf<String?>(null) }

    // Calculate total spent and total income using a while loop
    var totalSpent = 0.0
    var totalIncome = 0.0
    var totalIdx = 0
    while (totalIdx < expenses.size) {
        val item = expenses[totalIdx]
        if (item.isIncome) {
            totalIncome += item.amount
        } else {
            totalSpent += item.amount
        }
        totalIdx++
    }

    // Filter expenses using a for loop
    val filteredExpenses = mutableListOf<Expense>()
    for (expense in expenses) {
        val matchesSearch = searchQuery.isBlank() ||
                expense.description.contains(searchQuery, ignoreCase = true) ||
                expense.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = (selectedCategoryFilter == "All") ||
                expense.category.equals(selectedCategoryFilter, ignoreCase = true)

        if (matchesSearch && matchesCategory) {
            filteredExpenses.add(expense)
        }
    }

    val categoryFilterOptions = remember(categories) {
        listOf("All") + categories
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TransactionLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Transactions",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TransactionDarkBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Overview of all incomes, expenses & total spent in Rands",
            fontSize = 15.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Total spent card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = TransactionDarkBlue)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Total Spent",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R %.2f".format(totalSpent),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${expenses.size} total transaction(s) recorded",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Income & Expense totals summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↑", color = IncomeGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Income",
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "R %.2f".format(totalIncome),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransactionDarkBlue
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↓", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Expenses",
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "R %.2f".format(totalSpent),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransactionDarkBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "All Transactions",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TransactionDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search and category filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search description…", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )

            Box {
                OutlinedButton(
                    onClick = { categoryDropdownExpanded = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Category: $selectedCategoryFilter", fontSize = 14.sp)
                }

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoryFilterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 15.sp) },
                            onClick = {
                                selectedCategoryFilter = option
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction entries list
        if (filteredExpenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = if (expenses.isEmpty()) {
                        "No transactions recorded yet. Click '+ Add Transaction' below to record your first transaction."
                    } else {
                        "No transactions match your search filter."
                    },
                    modifier = Modifier.padding(20.dp),
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }
        } else {
            for (expense in filteredExpenses) {
                val categoryIcon = if (expense.isIncome) "💰" else when (expense.category.lowercase()) {
                    "groceries" -> "🛒"
                    "rent" -> "🏠"
                    "utilities" -> "⚡"
                    "food" -> "🍕"
                    "healthcare", "medical" -> "🩺"
                    "travel" -> "✈️"
                    "entertainment", "movies" -> "🎬"
                    "education", "books" -> "📚"
                    "savings" -> "🐷"
                    "transport" -> "🚗"
                    "shopping", "clothing" -> "👕"
                    else -> "💳"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Thumbnail or Category Icon Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (expense.isIncome) Color(0xFFDCFCE7) else TransactionLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!expense.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = expense.photoUri,
                                    contentDescription = expense.category,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = categoryIcon,
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Transaction Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$categoryIcon ${expense.description.ifBlank { expense.category }}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TransactionDarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${expense.category} • ${expense.startDate}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            if (!expense.photoUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📷 View Receipt Image",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.clickable {
                                        selectedReceiptUri = expense.photoUri
                                    }
                                )
                            }
                        }

                        // Amount (Green for Income, Red for Expense)
                        Text(
                            text = if (expense.isIncome) "+ R %.2f".format(expense.amount) else "- R %.2f".format(expense.amount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (expense.isIncome) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TransactionDarkBlue)
        ) {
            Text(
                text = "+ Add Transaction (Expense / Income)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBackToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "← Back to Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (selectedReceiptUri != null) {
        AlertDialog(
            onDismissRequest = { selectedReceiptUri = null },
            title = { Text("Receipt Photo", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                AsyncImage(
                    model = selectedReceiptUri,
                    contentDescription = "Receipt photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedReceiptUri = null }) {
                    Text("Close", fontSize = 15.sp)
                }
            }
        )
    }
}
