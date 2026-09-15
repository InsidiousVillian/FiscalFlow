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
import java.util.Locale

// Main dark blue colour used by the FiscalFlow dashboard.
private val DashboardDarkBlue = Color(0xFF172A46)

// Lavender background colour.
private val DashboardLavender = Color(0xFFEDEBFA)

// Green used for income.
private val IncomeGreen = Color(0xFF4CAF50)

// Red used for expenses.
private val ExpenseRed = Color(0xFFE57373)

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
    // Get today's date.
    val calendar = Calendar.getInstance()

    // Get the current month and year.
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Convert the expense date into a Calendar-compatible date.
    val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

    // Only include expenses from the current month.
    val currentMonthExpenses = expenses.filter { expense ->

        try {
            val expenseDate = dateFormat.parse(expense.startDate)

            if (expenseDate != null) {

                val expenseCalendar = Calendar.getInstance()
                expenseCalendar.time = expenseDate

                expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                        expenseCalendar.get(Calendar.YEAR) == currentYear
            } else {
                false
            }

        } catch (exception: Exception) {
            false
        }
    }

    // Calculate the total amount spent this month.
    val monthlyExpenses = currentMonthExpenses.sumOf {
        it.amount
    }

    // Calculate the total amount spent overall.
    val totalExpenses = expenses.sumOf {
        it.amount
    }

    // Calculate category totals for this month.
    val categoryTotals = currentMonthExpenses
        .groupBy { it.category }
        .mapValues { entry ->
            entry.value.sumOf { expense ->
                expense.amount
            }
        }
        .toList()
        .sortedByDescending { it.second }

    // Calculate the total used for percentages.
    val categoryTotal = categoryTotals.sumOf {
        it.second
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DashboardLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Dashboard heading.
        Text(
            text = "FiscalFlow",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Your financial overview",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // TOTAL EXPENSE CARD.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = DashboardDarkBlue
            )
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Text(
                    text = "Total Expenses",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "R %.2f".format(totalExpenses),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Spending for this month: R %.2f"
                        .format(monthlyExpenses),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // MONTHLY OVERVIEW TITLE.
        Text(
            text = "Overview for this month",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // INCOME AND EXPENSES CARDS.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Income card.
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Income",
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Your current Expense model does not contain income.
                    Text(
                        text = "R 0.00",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardDarkBlue
                    )
                }
            }

            // Expenses card.
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Expenses",
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "R %.2f".format(monthlyExpenses),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardDarkBlue
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // TOP CATEGORIES.
        Text(
            text = "Top Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (categoryTotals.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Text(
                    text = "No expenses recorded this month yet.",
                    modifier = Modifier.padding(18.dp),
                    color = Color.DarkGray
                )
            }

        } else {

            categoryTotals.forEach { (category, amount) ->

                // Calculate the percentage for this category.
                val percentage =
                    if (categoryTotal > 0) {
                        (amount / categoryTotal) * 100
                    } else {
                        0.0
                    }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = category,
                                fontWeight = FontWeight.Bold,
                                color = DashboardDarkBlue
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "R %.2f".format(amount),
                                color = Color.DarkGray
                            )
                        }

                        Text(
                            text = "%.1f%%".format(percentage),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardDarkBlue
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // QUICK ACCESS.
        Text(
            text = "Quick Access",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardDarkBlue
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Transactions button.
        DashboardButton(
            text = "Transactions",
            onClick = onTransactionsClick
        )

        // Budget button.
        DashboardButton(
            text = "Budget",
            onClick = onBudgetClick
        )

        // Goals button.
        DashboardButton(
            text = "Goals",
            onClick = onGoalsClick
        )

        // Gamification button.
        DashboardButton(
            text = "XP & Milestones",
            onClick = onGamificationClick
        )

        // Profile button.
        DashboardButton(
            text = "Profile",
            onClick = onProfileClick
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}

// Reusable button used by the Dashboard.
@Composable
private fun DashboardButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}