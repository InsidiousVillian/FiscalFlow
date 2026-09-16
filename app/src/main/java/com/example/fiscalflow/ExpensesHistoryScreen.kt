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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Theme colors consistent with FiscalFlow design palette
private val HistoryDarkBlue = Color(0xFF172A46)
private val HistoryLavender = Color(0xFFEDEBFA)
private val AccentBlue = Color(0xFF3B82F6)

// Screen for reviewing category totals and expenses across weekly, monthly, or custom timeframes
@Composable
fun ExpensesHistoryScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    val today = Calendar.getInstance()

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val years = (today.get(Calendar.YEAR) - 5..today.get(Calendar.YEAR) + 1).toList()

    var selectedPeriod by remember { mutableStateOf("month") }

    var startDay by remember { mutableIntStateOf(1) }
    var startMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var startYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    var dayEnd by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var monthEnd by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var yearEnd by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    // Set date range for current week
    fun applyThisWeek() {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
        }
        startDay = start.get(Calendar.DAY_OF_MONTH)
        startMonth = start.get(Calendar.MONTH)
        startYear = start.get(Calendar.YEAR)
        dayEnd = end.get(Calendar.DAY_OF_MONTH)
        monthEnd = end.get(Calendar.MONTH)
        yearEnd = end.get(Calendar.YEAR)
    }

    // Set date range for current month
    fun applyThisMonth() {
        val end = Calendar.getInstance()
        startDay = 1
        startMonth = end.get(Calendar.MONTH)
        startYear = end.get(Calendar.YEAR)
        dayEnd = end.get(Calendar.DAY_OF_MONTH)
        monthEnd = end.get(Calendar.MONTH)
        yearEnd = end.get(Calendar.YEAR)
    }

    LaunchedEffect(Unit) {
        applyThisMonth()
    }

    val startingDate = createCalendar(startYear, startMonth, startDay)
    val toDate = createCalendar(yearEnd, monthEnd, dayEnd)

    val filteredExpenses = expenses.filter { expense ->
        parseDate(expense.startDate)?.let { date ->
            !date.before(startingDate) && !date.after(toDate)
        } == true
    }

    val total = filteredExpenses.sumOf { it.amount }

    val totalsByCategory = filteredExpenses
        .groupBy { it.category }
        .map { (category, list) ->
            category to list.sumOf { expense -> expense.amount }
        }
        .sortedByDescending { it.second }

    val periodLabel = when (selectedPeriod) {
        "week" -> "This week"
        "month" -> "This month"
        else -> "Custom period"
    }
    val rangeText = "%d/%d/%d – %d/%d/%d".format(
        startDay, startMonth + 1, startYear,
        dayEnd, monthEnd + 1, yearEnd
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HistoryLavender)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Screen Title
        Text(
            text = "Category Totals & Period Review",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = HistoryDarkBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Review category budgets and totals across custom time periods",
            fontSize = 15.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Select Review Period",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HistoryDarkBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodChoiceButton(
                        label = "This Week",
                        selected = selectedPeriod == "week",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPeriod = "week"
                            applyThisWeek()
                        }
                    )
                    PeriodChoiceButton(
                        label = "This Month",
                        selected = selectedPeriod == "month",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPeriod = "month"
                            applyThisMonth()
                        }
                    )
                    PeriodChoiceButton(
                        label = "Custom",
                        selected = selectedPeriod == "custom",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPeriod = "custom" }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Showing: $periodLabel ($rangeText)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )

                if (selectedPeriod == "custom") {
                    Spacer(modifier = Modifier.height(14.dp))

                    DateSelector(
                        title = "From",
                        day = startDay,
                        month = startMonth,
                        year = startYear,
                        months = months,
                        years = years,
                        onDayChange = { startDay = it },
                        onMonthChange = {
                            startMonth = it
                            startDay = startDay.coerceAtMost(daysInMonth(it, startYear))
                        },
                        onYearChange = {
                            startYear = it
                            startDay = startDay.coerceAtMost(daysInMonth(startMonth, it))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DateSelector(
                        title = "To",
                        day = dayEnd,
                        month = monthEnd,
                        year = yearEnd,
                        months = months,
                        years = years,
                        onDayChange = { dayEnd = it },
                        onMonthChange = {
                            monthEnd = it
                            dayEnd = dayEnd.coerceAtMost(daysInMonth(it, yearEnd))
                        },
                        onYearChange = {
                            yearEnd = it
                            dayEnd = dayEnd.coerceAtMost(daysInMonth(monthEnd, it))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = HistoryDarkBlue)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Total Spent in Period",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R %.2f".format(total),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${filteredExpenses.size} expense entry(ies) in this timeframe",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Category Breakdown",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HistoryDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (totalsByCategory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No expenses recorded for this time period.",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }
        } else {
            totalsByCategory.forEach { (category, categoryTotal) ->
                val categoryPercentage = if (total > 0) (categoryTotal / total) * 100 else 0.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HistoryLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (category.lowercase()) {
                                        "groceries" -> "🛒"
                                        "rent" -> "🏠"
                                        "utilities" -> "⚡"
                                        else -> "🎯"
                                    },
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HistoryDarkBlue
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "%.1f%% of period total".format(categoryPercentage),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "R %.2f".format(categoryTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HistoryDarkBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (categoryPercentage / 100).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentBlue,
                            trackColor = HistoryLavender
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Expenses in Period",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HistoryDarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredExpenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No expenses found for this time period.",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }
        } else {
            filteredExpenses.forEach { expense ->
                ExpenseHistoryCard(expense = expense)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HistoryDarkBlue)
        ) {
            Text(
                text = "← Back to Category Goals",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PeriodChoiceButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HistoryDarkBlue)
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(label, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ExpenseHistoryCard(
    expense: Expense
) {
    var showReceipt by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.category,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HistoryDarkBlue
                )

                Text(
                    text = "R %.2f".format(expense.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE57373)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = expense.description.ifBlank { "No description" },
                fontSize = 15.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${expense.startDate} – ${expense.endDate}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            if (!expense.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showReceipt = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("📷 View Receipt", fontSize = 14.sp)
                }
            }
        }
    }

    if (showReceipt && expense.photoUri != null) {
        AlertDialog(
            onDismissRequest = { showReceipt = false },
            title = { Text("Receipt Photo", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                AsyncImage(
                    model = expense.photoUri,
                    contentDescription = "Receipt photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showReceipt = false }) {
                    Text("Close", fontSize = 15.sp)
                }
            }
        )
    }
}

@Composable
private fun DateSelector(
    title: String,
    day: Int,
    month: Int,
    year: Int,
    months: List<String>,
    years: List<Int>,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val maxDay = daysInMonth(month, year)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = HistoryDarkBlue,
            modifier = Modifier.width(44.dp)
        )
        SimpleHistoryDropdown(
            value = day.toString(),
            options = (1..maxDay).map { it.toString() },
            modifier = Modifier.weight(1f),
            onSelected = { onDayChange(it.toInt()) }
        )
        SimpleHistoryDropdown(
            value = months[month],
            options = months,
            modifier = Modifier.weight(1.2f),
            onSelected = { onMonthChange(months.indexOf(it)) }
        )
        SimpleHistoryDropdown(
            value = year.toString(),
            options = years.map { it.toString() },
            modifier = Modifier.weight(1.2f),
            onSelected = { onYearChange(it.toInt()) }
        )
    }
}

@Composable
private fun SimpleHistoryDropdown(
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(value, fontSize = 14.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 15.sp) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun createCalendar(year: Int, month: Int, day: Int): Calendar {
    return Calendar.getInstance().apply {
        set(year, month, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun parseDate(value: String): Calendar? {
    return try {
        val formatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val date = formatter.parse(value)
        Calendar.getInstance().apply { time = date!! }
    } catch (_: Exception) {
        null
    }
}

private fun daysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
