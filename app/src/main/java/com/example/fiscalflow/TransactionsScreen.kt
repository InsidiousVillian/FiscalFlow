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

// FiscalFlow Dashboard & Screen Color Palette
private val TransactionDarkBlue = Color(0xFF172A46)
private val TransactionLavender = Color(0xFFEDEBFA)
private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFE57373)

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    categories: List<String>,
    onAddExpense: () -> Unit = {},
    onBackToDashboard: () -> Unit = {}
)
{
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var selectedReceiptUri by remember { mutableStateOf<String?>(null) }

    // Calculate total spent overall
    val totalSpent = expenses.sumOf { it.amount }

    // Total income (Currently hardcoded at R 0.00 until income model is introduced)
    val totalIncome = 0.0

    // Filter expenses based on search query and category filter
    val filteredExpenses = expenses.filter { expense ->
        val matchesSearch = searchQuery.isBlank() ||
                expense.description.contains(searchQuery, ignoreCase = true) ||
                expense.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == "All" ||
                expense.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesSearch && matchesCategory
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
        // Screen Heading
        Text(
            text = "Transactions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TransactionDarkBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Overview of all incomes, expenses & total spent",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Total spent card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = TransactionDarkBlue
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "Total Spent",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R %.2f".format(totalSpent),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${expenses.size} total transaction(s) recorded",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Income and expense summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income summary card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↑", color = IncomeGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Income",
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "R %.2f".format(totalIncome),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransactionDarkBlue
                    )
                }
            }

            // Expenses summary card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Column(Modifier.padding(16.dp))
                {
                    Row(verticalAlignment = Alignment.CenterVertically)
                    {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "↓", color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Expenses",
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "R %.2f".format(totalSpent),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransactionDarkBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search and f
        Text(
            "All Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TransactionDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search description…") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )

            // Category Filter Dropdown
            Box {
                OutlinedButton(
                    onClick = { categoryDropdownExpanded = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(56.dp)
                )
                {
                    Text("Category: $selectedCategoryFilter")
                }

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                )
                {
                    categoryFilterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
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

        // TRANSACTIONS LIST
        if (filteredExpenses.isEmpty())
        {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Text(
                    if (expenses.isEmpty())
                    {
                        "No transactions recorded yet. Click 'Add Expense' below to add your first transaction."
                    }
                    else
                    {
                        "No transactions match your search filter."
                    },
                    modifier = Modifier.padding(20.dp),
                    color = Color.DarkGray
                )
            }
        }
        else
        {
            filteredExpenses.forEach { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        // Category Icon Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TransactionLavender),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                when (expense.category.lowercase())
                                {
                                    "groceries" -> "🛒"
                                    "rent" -> "🏠"
                                    "utilities" -> "⚡"
                                    else -> "💳"
                                },
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Transaction Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                expense.description.ifBlank { expense.category },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TransactionDarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${expense.category} • ${expense.startDate}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            if (!expense.photoUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "📷 View Receipt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.clickable {
                                        selectedReceiptUri = expense.photoUri
                                    }
                                )
                            }
                        }

                        // Amount
                        Text(
                            "- R %.2f".format(expense.amount),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS
        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TransactionDarkBlue)
        )
        {
            Text(
                "+ Add Expense",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBackToDashboard,
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        )
        {
            Text(
                "← Back to Dashboard",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Receipt Modal Dialog
    if (selectedReceiptUri != null) {
        AlertDialog(
            onDismissRequest = { selectedReceiptUri = null },
            title = { Text("Receipt Photo") },
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
                    Text("Close")
                }
            }
        )
    }
}
