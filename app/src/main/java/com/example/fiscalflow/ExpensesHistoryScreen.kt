package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * User picks a review period (This Week / This Month / Custom),
 * then sees total spent per category for that period only.
 */
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

    // week | month | custom — user must choose how they want to review
    var selectedPeriod by remember { mutableStateOf("month") }

    var startDay by remember { mutableIntStateOf(1) }
    var startMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var startYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    var dayEnd by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var monthEnd by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var yearEnd by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    // Apply week/month presets whenever the user picks those options
    fun applyThisWeek() {
        // Last 7 days including today (simple weekly review period)
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

    fun applyThisMonth() {
        val end = Calendar.getInstance()
        startDay = 1
        startMonth = end.get(Calendar.MONTH)
        startYear = end.get(Calendar.YEAR)
        dayEnd = end.get(Calendar.DAY_OF_MONTH)
        monthEnd = end.get(Calendar.MONTH)
        yearEnd = end.get(Calendar.YEAR)
    }

    // Default to this month the first time the screen opens
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
        modifier
            .fillMaxSize()
            .background(Color(0xFFE8E9F5))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Category Totals",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Choose a period to review, then see totals for each category.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Select your review period",
                    style = MaterialTheme.typography.titleMedium
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
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1119A8)
                )

                // Custom From / To only when user wants their own range
                if (selectedPeriod == "custom") {
                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

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

        Text(
            text = "2. Total spent in selected period",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "R %.2f".format(total),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "3. Total by category (for that period)",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (totalsByCategory.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No expenses in this period yet. Add expenses, then pick This Week / This Month / Custom.",
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            totalsByCategory.forEach { (category, categoryTotal) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "R %.2f".format(categoryTotal),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1119A8)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Expenses in this period",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredExpenses.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No expenses found for this time period.",
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            filteredExpenses.forEach { expense ->
                ExpenseHistoryCard(expense = expense)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
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
        Button(onClick = onClick, modifier = modifier) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label)
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
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = expense.category,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = expense.description)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "R %.2f".format(expense.amount),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("${expense.startDate} - ${expense.endDate}")

            if (expense.photoUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = { showReceipt = true }) {
                    Text("View Imagery")
                }
            }
        }
    }

    if (showReceipt && expense.photoUri != null) {
        AlertDialog(
            onDismissRequest = { showReceipt = false },
            title = { Text("Image") },
            text = {
                AsyncImage(
                    model = expense.photoUri,
                    contentDescription = "Receipt",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showReceipt = false }) {
                    Text("Close")
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
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.width(35.dp)
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
            modifier = Modifier.weight(1.3f),
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(value)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
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
    } catch (e: Exception) {
        null
    }
}

private fun daysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
