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
    //Gets today's date to use as the default date.
    val today = Calendar.getInstance()

    //Store the month names used in the month dropdown.
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    //Allow the user to select dates from the last five years.
    val years = (
            today.get(Calendar.YEAR) - 5
                    ..today.get(Calendar.YEAR) + 1
            ).toList()

    //Store the selected starting date.
    var startDay by remember {
        mutableIntStateOf(1)
    }
    var startMonth by remember {
        mutableIntStateOf(today.get(Calendar.MONTH))
    }
    var startYear by remember {
        mutableIntStateOf(today.get(Calendar.YEAR))
    }

    //Store the selected ending date.
    var dayEnd by remember {
        mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH))
    }
    var monthEnd by remember {
        mutableIntStateOf(today.get(Calendar.MONTH))
    }
    var yearEnd by remember {
        mutableIntStateOf(today.get(Calendar.YEAR))
    }

    //Create Calendar elements using the selected dates.
    val startingDate = createCalendar(
        startYear,
        startMonth,
        startDay
    )
    val toDate = createCalendar(
        yearEnd,
        monthEnd,
        dayEnd
    )

    // Only keeps expenses that fall inside the selected date range.
    val filteredExpenses = expenses.filter { expense ->
        parseDate(expense.startDate)?.let { date ->
            !date.before(startingDate) &&
                    !date.after(toDate)
        } == true
    }

    // Adds together the amounts of the filtered expenses.
    val total = filteredExpenses.sumOf {it.amount}

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFFE8E9F5))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    )
    {
        //Display the title at the top of the history screen.
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1119A8)
        )

        Spacer(Modifier.height(16.dp))

        // Provides controls for selecting the date range.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
        {
            Column(modifier = Modifier.padding(16.dp))
            {
                Text(
                    text = "Select a Period",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                //Allow the user to choose the starting date.
                DateSelector(
                    title = "From",
                    day = startDay,
                    month = startMonth,
                    year = startYear,
                    months = months,
                    years = years,
                    onDayChange = {
                        startDay = it
                    },
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
                        startDay = startDay.coerceAtMost(
                            daysInMonth(
                                startMonth,
                                it
                            )
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))

                //Allow the user to choose the ending date.
                DateSelector(
                    title = "To",
                    day = dayEnd,
                    month = monthEnd,
                    year = yearEnd,
                    months = months,
                    years = years,
                    onDayChange = {
                        dayEnd = it
                    },
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
                                it
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Display the total amount spent in that time period
        Text(
            text = "Total spent: R %.2f".format(total),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1119A8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Set an error message when their is not an expense in that time period
        if (filteredExpenses.isEmpty()) {
            Card(Modifier.fillMaxWidth())
            {
                Text(
                    text = "No expenses found for this time period.",
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
        else
        {
            //Display each expense that matches the selected date range.
            filteredExpenses.forEach { expense ->
                ExpenseHistoryCard(expense = expense)

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        //Return to the prvious screen
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text("Back")
        }
    }
}

@Composable
private fun ExpenseHistoryCard(
    expense: Expense
) {
    //Controls whether the image is being displayed
    var showReceipt by remember {mutableStateOf(false)}

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    )
    {
        Column(modifier = Modifier.padding(16.dp))
        {
            //Dsiplay the category
            Text(
                text = expense.category,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            //Display the description of that specific expense
            Text(text = expense.description)

            Spacer(Modifier.height(6.dp))

            //Show the amount used/spent
            Text(
                text = "R %.2f".format(expense.amount),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(6.dp))

            //Displays the start and end dates of the expense
            Text("${expense.startDate} - ${expense.endDate}")

            //Show the image when it is present
            if (expense.photoUri != null) {
                Spacer(Modifier.height(10.dp))

                OutlinedButton(onClick = {showReceipt = true }
                )
                {
                    Text("View Imagery")
                }
            }
        }
    }

    //Show the image onn a pop up
    if (showReceipt && expense.photoUri != null) {
        AlertDialog(
            onDismissRequest = {showReceipt = false},
            title = {Text("Image")},
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
                TextButton(
                    onClick = {
                        showReceipt = false
                    }
                )
                {
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
    //Calcuate the amount of days remaining
    val maxDay = daysInMonth(
        month,
        year
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    )
    {
        Text(
            text = title,
            modifier = Modifier.width(35.dp)
        )

        //Create drop down to selecting the day
        SimpleHistoryDropdown(
            value = day.toString(),
            options = (1..maxDay).map {
                it.toString()
            },
            modifier = Modifier.weight(1f),
            onSelected = {
                onDayChange(it.toInt())
            }
        )

        //Create drop down to selecting the month
        SimpleHistoryDropdown(
            value = months[month],
            options = months,
            modifier = Modifier.weight(1.3f),
            onSelected = {
                onMonthChange(
                    months.indexOf(it)
                )
            }
        )

        //Create drop down to selecting the year
        SimpleHistoryDropdown(
            value = year.toString(),
            options = years.map {
                it.toString()
            },
            modifier = Modifier.weight(1.2f),
            onSelected = {
                onYearChange(it.toInt())
            }
        )
    }
}

@Composable
private fun SimpleHistoryDropdown(
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
)
{
    //Controller when the drop down is open
    var expanded by remember {mutableStateOf(false) }

    Box(modifier)
    {
        //Open the dropdown when the button is clicked
        OutlinedButton(
            onClick = {expanded = true},
            modifier = Modifier.fillMaxWidth()
        )
        {
            Text(value)
        }

        //Shw the available options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        )
        {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

//Creates a Calendar object using the selected date
private fun createCalendar(
    year: Int,
    month: Int,
    day: Int
): Calendar {
    return Calendar.getInstance().apply {
        set(
            year,
            month,
            day,
            0,
            0,
            0
        )

        //Removes milliseconds so date comparisons are consistent
        set(
            Calendar.MILLISECOND,
            0
        )
    }
}

//Converts the stored date text into a Calendar element
private fun parseDate(
    value: String
): Calendar? {
    return try {
        val formatter = SimpleDateFormat(
            "d/M/yyyy",
            Locale.getDefault()
        )
        val date = formatter.parse(value)
        Calendar.getInstance().apply {time = date!!}
    }
    catch (e: Exception) {
        //Returns null when the date cannot be converted.
        null
    }
}

//Finding the number of days in the selected month
private fun daysInMonth(
    month: Int,
    year: Int
): Int {
    val calendar = Calendar.getInstance()

    calendar.set(
        year,
        month,
        1
    )

    return calendar.getActualMaximum(
        Calendar.DAY_OF_MONTH
    )
}