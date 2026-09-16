package com.example.fiscalflow

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Theme colors for dashboard UI
private val DashboardDarkBlue = Color(0xFF172A46)
private val DashboardLavender = Color(0xFFEDEBFA)
private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFE57373)

// Main overview dashboard displaying monthly spend, top categories, and quick navigation
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onTransactionsClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onGamificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

    // Calculate total overall expenses using a while loop
    var totalExpenses = 0.0
    var totalIdx = 0
    while (totalIdx < expenses.size) {
        totalExpenses += expenses[totalIdx].amount
        totalIdx++
    }

    // Filter current month expenses using a for loop
    val currentMonthExpenses = mutableListOf<Expense>()
    for (expense in expenses) {
        val expenseDate = parseExpenseDate(expense.startDate, dateFormat)
        if (expenseDate != null) {
            val expenseCalendar = Calendar.getInstance()
            expenseCalendar.time = expenseDate
            if (expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                expenseCalendar.get(Calendar.YEAR) == currentYear
            ) {
                currentMonthExpenses.add(expense)
            }
        }
    }

    // Calculate total monthly expenses using a for loop
    var monthlyExpenses = 0.0
    for (expense in currentMonthExpenses) {
        monthlyExpenses += expense.amount
    }

    // Calculate category totals using a for loop
    val categoryMap = mutableMapOf<String, Double>()
    for (expense in currentMonthExpenses) {
        val currentCatTotal = categoryMap.getOrDefault(expense.category, 0.0)
        categoryMap[expense.category] = currentCatTotal + expense.amount
    }

    val categoryTotals = categoryMap.toList().sortedByDescending { it.second }

    // Calculate category total for percentage using a while loop
    var categoryTotal = 0.0
    var catIdx = 0
    while (catIdx < categoryTotals.size) {
        categoryTotal += categoryTotals[catIdx].second
        catIdx++
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DashboardLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            "FiscalFlow",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Your financial overview",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Total spending summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DashboardDarkBlue)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    "Total Expenses",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "R %.2f".format(totalExpenses),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Spending for this month: R %.2f".format(monthlyExpenses),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Overview for this month",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Income & expense breakdown
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
                    Text(
                        "Income",
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "R 0.00",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardDarkBlue
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Expenses",
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "R %.2f".format(monthlyExpenses),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardDarkBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Top Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (categoryTotals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    "No expenses recorded this month yet.",
                    modifier = Modifier.padding(18.dp),
                    color = Color.DarkGray
                )
            }
        } else {
            for ((category, amount) in categoryTotals) {
                val percentage = if (categoryTotal > 0) (amount / categoryTotal) * 100 else 0.0

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                category,
                                fontWeight = FontWeight.Bold,
                                color = DashboardDarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "R %.2f".format(amount),
                                color = Color.DarkGray
                            )
                        }

                        Text(
                            "%.1f%%".format(percentage),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardDarkBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Quick Access",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        DashboardButton("Transactions", onClick = onTransactionsClick)
        DashboardButton("Budget", onClick = onBudgetClick)
        DashboardButton("Goals", onClick = onGoalsClick)
        DashboardButton("XP & Milestones", onClick = onGamificationClick)
        DashboardButton("Profile", onClick = onProfileClick)

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Safely parses expense date string using try-catch to prevent crashes on invalid strings
private fun parseExpenseDate(dateStr: String, dateFormat: SimpleDateFormat): Date? {
    return try {
        dateFormat.parse(dateStr)
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun DashboardButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, fontSize = 16.sp)
    }
}
