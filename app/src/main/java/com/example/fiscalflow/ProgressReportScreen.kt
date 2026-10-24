package com.example.fiscalflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
=======
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
>>>>>>> 05452e1e983b2ead020a050e34c6e5f79105c961
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ReportDarkBlue = Color(0xFF172A46)
private val ReportLavender = Color(0xFFEDEBFA)
private val IncomeGreen = Color(0xFF15803D)
private val ExpenseRed = Color(0xFFDC2626)
private val BarBlue = Color(0xFF3B82F6)
private val Grey = Color(0xFF64748B)

@Composable
fun ProgressReportScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    categories: List<String>,
    budgetGoal: BudgetingGoal?,
    onBackToDashboard: () -> Unit
) {
    val monthItems = expensesInCurrentMonth(expenses)
    val monthlySpent = monthlyExpenseTotal(monthItems)
    val monthlyIncome = monthlyIncomeTotal(monthItems)
    val maximum = budgetGoal?.maximum ?: 0.0
    val reports = categorySpendReports(monthItems, categories, budgetGoal)
    val watchList = reports.filter { it.isOverspent }
    val topCategory = reports.firstOrNull { it.amount > 0 }?.name
    val message = pickProgressMessage(budgetGoal, monthlySpent, topCategory)
    val weekBars = lastSevenDaySpends(expenses)
    val overallProgress = calculateProgress(monthlySpent, maximum)
    val monthEmpty = monthItems.none { !it.isIncome }

<<<<<<< HEAD
    ScrollableScreen(
        modifier = modifier.fillMaxSize()
=======
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ReportLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
>>>>>>> 05452e1e983b2ead020a050e34c6e5f79105c961
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToDashboard) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ReportDarkBlue
                )
            }
            Column {
                Text(
                    text = "Progress Report",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportDarkBlue
                )
                Text(
                    text = "This month in Rands",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportDarkBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ReportDarkBlue)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Monthly spending",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R %.2f / R %.2f".format(monthlySpent, maximum),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = if (maximum > 0 && monthlySpent > maximum) ExpenseRed else BarBlue,
                    trackColor = ReportLavender
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Watch list",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReportDarkBlue
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (monthEmpty) {
            EmptyReportCard("No expenses recorded this month yet.")
        } else if (watchList.isEmpty()) {
            EmptyReportCard("No overspent categories. Your splits look balanced.")
        } else {
            watchList.forEach { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = report.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R %.2f vs fair share R %.2f".format(
                                report.amount,
                                report.fairShare
                            ),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ReportDarkBlue
                        )
                        if (report.percentOver > 0) {
                            Text(
                                text = "%.0f%% over".format(report.percentOver),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Category share",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReportDarkBlue
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (reports.none { it.amount > 0 }) {
            EmptyReportCard("Add expenses to see category bars.")
        } else {
            reports.filter { it.amount > 0 }.forEach { report ->
                CategoryShareBar(report)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Last 7 days",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReportDarkBlue
        )
        Spacer(modifier = Modifier.height(10.dp))

        WeekSpendChart(weekBars)

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Income vs expenses",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReportDarkBlue
        )
        Spacer(modifier = Modifier.height(10.dp))

        IncomeExpenseCompare(
            income = monthlyIncome,
            expenses = monthlySpent
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun EmptyReportCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ReportDarkBlue
        )
    }
}

@Composable
private fun CategoryShareBar(report: CategorySpendReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = report.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (report.isOverspent) ExpenseRed else ReportDarkBlue
                )
                Text(
                    text = "R %.2f".format(report.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportDarkBlue
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { report.percentOfMax },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = if (report.isOverspent) ExpenseRed else BarBlue,
                trackColor = ReportLavender
            )
        }
    }
}

@Composable
private fun WeekSpendChart(bars: List<DailySpendBar>) {
    val maxAmount = bars.maxOfOrNull { it.amount }?.takeIf { it > 0 } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val barCount = bars.size.coerceAtLeast(1)
                val gap = 8.dp.toPx()
                val barWidth = ((size.width - gap * (barCount + 1)) / barCount).coerceAtLeast(8f)
                bars.forEachIndexed { index, bar ->
                    val heightRatio = (bar.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                    val barHeight = (size.height * heightRatio).coerceAtLeast(
                        if (bar.amount > 0) 6.dp.toPx() else 2.dp.toPx()
                    )
                    val left = gap + index * (barWidth + gap)
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = if (bar.amount > 0) BarBlue else ReportLavender,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bars.forEach { bar ->
                    Text(
                        text = bar.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Grey
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseCompare(income: Double, expenses: Double) {
    val maxValue = maxOf(income, expenses, 1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CompareBar(label = "Income", amount = income, maxValue = maxValue, color = IncomeGreen)
            Spacer(modifier = Modifier.height(12.dp))
            CompareBar(label = "Expenses", amount = expenses, maxValue = maxValue, color = ExpenseRed)
        }
    }
}

@Composable
private fun CompareBar(
    label: String,
    amount: Double,
    maxValue: Double,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(
                text = "R %.2f".format(amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ReportDarkBlue
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(ReportLavender, RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (amount / maxValue).toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(8.dp))
            )
        }
    }
}
