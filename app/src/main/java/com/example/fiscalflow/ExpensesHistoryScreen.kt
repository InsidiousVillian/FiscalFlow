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

@Composable
fun ExpensesHistoryScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onBack: () -> Unit
) {
    val today =  Calendar.getInstance()
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr","May", "Jun", "Jul", "Aug",
        "Sep", "Oct", "Nov", "Dec"
    )
    val years = (today.get(Calendar.YEAR) - 5
        ..today.get(Calendar.YEAR) + 1).toList()

    var startDay by remember { mutableIntStateOf(1)}
    var dayEnd by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH))}
    var startMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH))}
    var monthEnd by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var startYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var yearEnd by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    val currentYear = today.get(Calendar.YEAR)

    val startingDate = createCalendar(startYear,startMonth,startDay)
    val toDate = createCalendar(yearEnd,monthEnd,dayEnd)

    val filteredExpenses = expenses.filter { parseDate(it.startDate)?.let { date ->
                !date.before(startingDate) && !date.after(toDate)} == true }

    val total = filteredExpenses.sumOf {it.amount}

    Column(modifier
            .fillMaxSize()
            .background(Color(0xFFE8E9F5))
            .verticalScroll(rememberScrollState())
            .padding(20.dp))
    {
        Text( "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1119A8))

        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp))
        {
            Column(Modifier.padding(16.dp))
            {
                Text("Select a Period",
                    style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(12.dp))

                DateSelector(
                    title = "From",
                    day = startDay,
                    month = startMonth,
                    year = startYear,
                    months = months,
                    years = years,

                    onDayChange = {startDay = it},

                    onMonthChange = {
                        startMonth = it
                        startDay = startDay.coerceAtMost(
                                daysInMonth(
                                    it,
                                    startYear
                                )
                            )
                    },

                    onYearChange = {
                        startYear = it
                        startDay =
                            startDay.coerceAtMost(
                                daysInMonth(
                                    startMonth,
                                    it
                                )
                            )
                    }
                )

                Spacer(Modifier.height(12.dp))

                DateSelector(
                    title = "To",
                    day = dayEnd,
                    month = monthEnd,
                    year = yearEnd,
                    months = months,
                    years = years,

                    onDayChange = {dayEnd = it},

                    onMonthChange = {
                        monthEnd = it
                        dayEnd = dayEnd.coerceAtMost(
                                daysInMonth(
                                    it,
                                    yearEnd
                                )
                            )
                    },

                    onYearChange = {
                        yearEnd = it
                        dayEnd = dayEnd.coerceAtMost(
                                daysInMonth(
                                    monthEnd,
                                    it)
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Total spent: R %.2f".format(total),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1119A8))

        Spacer(Modifier.height(16.dp))

        if (filteredExpenses.isEmpty())
        {
            Card(Modifier.fillMaxWidth())
            {
                Text(
                    "No expenses found for this period.",
                    Modifier.padding(20.dp))
            }

        }
        else
        {
            filteredExpenses.forEach {
                expense ->
                ExpenseHistoryCard(expense = expense)
                Spacer(Modifier.height(12.dp)
                )
            }
        }

        TextButton(
            onClick = onBack,
            Modifier.fillMaxWidth())
        {
            Text("Back")
        }
    }
}

// Expense Card
@Composable
private fun ExpenseHistoryCard(expense: Expense)
{
    var showReceipt by remember {mutableStateOf(false)}

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp))
    {
        Column(Modifier.padding(16.dp))
        {
            Text(
                expense.category,
                style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))

            Text(expense.description)

            Spacer(Modifier.height(6.dp))

            Text(
                "R %.2f".format(expense.amount),
                style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(6.dp))

            Text("${expense.startDate} - ${expense.endDate}")

            if (expense.photoUri != null)
            {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {showReceipt = true})
                {
                    Text("View Receipt")
                }
            }
        }
    }

    if (showReceipt && expense.photoUri != null)
    {
        AlertDialog(
            onDismissRequest = {showReceipt = false},
            title = {Text("Receipt")},

            text = {
                AsyncImage(
                    model = expense.photoUri,
                    contentDescription = "Receipt",
                    Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentScale = ContentScale.Fit)
            },

            confirmButton = {TextButton(onClick = {showReceipt = false})
                {
                    Text("Close")
                }
            }
        )
    }
}

// Date Selector

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
    onYearChange: (Int) -> Unit)
{

    val maxDay = daysInMonth(month, year)

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp))
    {
        Text(title, Modifier.width(35.dp))

        SimpleHistoryDropdown(
            value = day.toString(),
            options = (1..maxDay).map { it.toString() },
            Modifier.weight(1f),
            onSelected = {onDayChange(it.toInt())
            }
        )

        SimpleHistoryDropdown(
            value = months[month],
            options = months,
            Modifier.weight(1.3f),
            onSelected = { onMonthChange(months.indexOf(it))
            }
        )

        SimpleHistoryDropdown(
            value = year.toString(),
            options = years.map {it.toString()},
            Modifier.weight(1.2f),
            onSelected = {onYearChange(it.toInt())
            }
        )
    }
}

// History Dropdown
@Composable
private fun SimpleHistoryDropdown(
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit)
{
    var expanded by remember {mutableStateOf(false)
    }

    Box(modifier)
    {
        OutlinedButton(onClick = {expanded = true },
            Modifier.fillMaxWidth())
        {
            Text(value)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false})
        {

            options.forEach { option ->
                DropdownMenuItem( {Text(option)},

                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Date Helpers

private fun createCalendar(year: Int,month: Int,day: Int): Calendar {
    return Calendar.getInstance().apply{

        set(
            year,
            month,
            day,
            0,
            0,
            0
        )

        set(
            Calendar.MILLISECOND,
            0
        )
    }
}


private fun parseDate(value: String): Calendar?
{
    return try {
        val formatter =
            SimpleDateFormat(
                "d/M/yyyy",
                Locale.getDefault()
            )

        val date = formatter.parse(value)

        Calendar.getInstance().apply {
            time = date!!
        }
    }
    catch (e: Exception)
    {
        null
    }
}

private fun daysInMonth(month: Int,year: Int): Int
{
    val calendar = Calendar.getInstance()

    calendar.set(
        year,
        month,
        1
    )

    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}