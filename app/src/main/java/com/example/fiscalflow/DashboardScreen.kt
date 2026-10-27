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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.fiscalflow.ui.theme.FiscalFlowPurple

// Theme colors for dashboard UI
private val DashboardDarkBlue = Color(0xFF172A46)
private val IncomeGreen = Color(0xFF15803D)
private val ExpenseRed = Color(0xFFDC2626)

// Main overview dashboard displaying monthly spend, income, top categories, and quick navigation
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onTransactionsClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onProgressReportClick: () -> Unit,
    onSpendingGraphsClick: () -> Unit,
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
        if (!expenses[totalIdx].isIncome) {
            totalExpenses += expenses[totalIdx].amount
        }
        totalIdx++
    }

    // Filter current month transactions using a for loop
    val currentMonthTransactions = mutableListOf<Expense>()
    for (expense in expenses) {
        val expenseDate = parseExpenseDate(expense.startDate, dateFormat)
        if (expenseDate != null) {
            val expenseCalendar = Calendar.getInstance()
            expenseCalendar.time = expenseDate
            if (expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                expenseCalendar.get(Calendar.YEAR) == currentYear
            ) {
                currentMonthTransactions.add(expense)
            }
        }
    }

    // Calculate monthly expenses & income using a for loop
    var monthlyExpenses = 0.0
    var monthlyIncome = 0.0
    for (item in currentMonthTransactions) {
        if (item.isIncome) {
            monthlyIncome += item.amount
        } else {
            monthlyExpenses += item.amount
        }
    }

    // Calculate category totals using a for loop (expenses only)
    val categoryMap = mutableMapOf<String, Double>()
    for (expense in currentMonthTransactions) {
        if (!expense.isIncome) {
            val currentCatTotal = categoryMap.getOrDefault(expense.category, 0.0)
            categoryMap[expense.category] = currentCatTotal + expense.amount
        }
    }

    val categoryTotals = categoryMap.toList().sortedByDescending { it.second }

    // Calculate category total for percentage using a while loop
    var categoryTotal = 0.0
    var catIdx = 0
    while (catIdx < categoryTotals.size) {
        categoryTotal += categoryTotals[catIdx].second
        catIdx++
    }

    ScrollableScreen(
        modifier = modifier.fillMaxSize()
    ) {

        Text(
            text = "FiscalFlow",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172554)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your financial overview in Rands",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172554)
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
                    text = "Total Expenses",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R %.2f".format(totalExpenses),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Spending for this month: R %.2f".format(monthlyExpenses),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Overview for this month",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172554)
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
                        text = "Income",
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "R %.2f".format(monthlyIncome),
                        fontSize = 22.sp,
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
                        text = "Expenses",
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "R %.2f".format(monthlyExpenses),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardDarkBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Categories",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = FiscalFlowPurple
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (categoryTotals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No expenses recorded this month yet.",
                    modifier = Modifier.padding(18.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardDarkBlue
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
                                text = category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DashboardDarkBlue
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "R %.2f".format(amount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashboardDarkBlue
                            )
                        }

                        Text(
                            text = "%.1f%%".format(percentage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardDarkBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Quick Access",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = FiscalFlowPurple
        )

        Spacer(modifier = Modifier.height(12.dp))

        QuickAccessGrid(
            onTransactionsClick = onTransactionsClick,
            onBudgetClick = onBudgetClick,
            onProgressReportClick = onProgressReportClick,
            onSpendingGraphsClick = onSpendingGraphsClick,
            onGoalsClick = onGoalsClick,
            onGamificationClick = onGamificationClick,
            onProfileClick = onProfileClick
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun parseExpenseDate(dateStr: String, dateFormat: SimpleDateFormat): Date? {
    return try {
        dateFormat.parse(dateStr)
    } catch (_: Exception) {
        null
    }
}

private data class QuickAccessItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun QuickAccessGrid(
    onTransactionsClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onProgressReportClick: () -> Unit,
    onSpendingGraphsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onGamificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val items = listOf(
        QuickAccessItem("Transactions", Icons.Filled.DirectionsCar, onTransactionsClick),
        QuickAccessItem("Budget", Icons.Filled.ShoppingBag, onBudgetClick),
        QuickAccessItem("Progress Report", Icons.Filled.Assessment, onProgressReportClick),
        QuickAccessItem("Spending Graphs", Icons.Filled.ShowChart, onSpendingGraphsClick),
        QuickAccessItem("Goals", Icons.Filled.GpsFixed, onGoalsClick),
        QuickAccessItem("XP & Milestones", Icons.Filled.Star, onGamificationClick),
        QuickAccessItem("Profile", Icons.Filled.Person, onProfileClick)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { item ->
                    QuickAccessCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = item.onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = DashboardDarkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DashboardDarkBlue,
                maxLines = 2
            )
        }
    }
}
