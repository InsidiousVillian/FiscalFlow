package com.example.fiscalflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val GraphDarkBlue = Color(0xFF172A46)
private val GraphLavender = Color(0xFFEDEBFA)
private val BarBlue = Color(0xFF3B82F6)
private val MinGreen = Color(0xFF15803D)
private val MaxRed = Color(0xFFDC2626)
private val WithinGreen = Color(0xFF16A34A)
private val BelowAmber = Color(0xFFD97706)

private val barColours = listOf(
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFF0EA5E9),
    Color(0xFFF59E0B),
    Color(0xFF14B8A6),
    Color(0xFFEC4899)
)

// Category spend graph for a chosen period, plus a past-month min/max goal check.
@Composable
fun SpendingGraphsScreen(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    categories: List<String>,
    budgetGoal: BudgetingGoal?,
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
    var endDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var endMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var endYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    fun applyThisWeek() {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
        startDay = start.get(Calendar.DAY_OF_MONTH)
        startMonth = start.get(Calendar.MONTH)
        startYear = start.get(Calendar.YEAR)
        endDay = end.get(Calendar.DAY_OF_MONTH)
        endMonth = end.get(Calendar.MONTH)
        endYear = end.get(Calendar.YEAR)
    }

    fun applyThisMonth() {
        val end = Calendar.getInstance()
        startDay = 1
        startMonth = end.get(Calendar.MONTH)
        startYear = end.get(Calendar.YEAR)
        endDay = end.get(Calendar.DAY_OF_MONTH)
        endMonth = end.get(Calendar.MONTH)
        endYear = end.get(Calendar.YEAR)
    }

    LaunchedEffect(Unit) {
        applyThisMonth()
    }

    val rangeStart = graphCalendar(startYear, startMonth, startDay, endOfDay = false)
    val rangeEnd = graphCalendar(endYear, endMonth, endDay, endOfDay = true)
    val rangeInvalid = rangeEnd.before(rangeStart)

    val periodExpenses = if (rangeInvalid) {
        emptyList()
    } else {
        expenses.filter { expense ->
            expenseInRange(expense, rangeStart, rangeEnd)
        }
    }

    val categoryTotals = categoryTotalsForPeriod(periodExpenses, categories)
    val periodTotal = categoryTotals.sumOf { it.second }
    val minimum = budgetGoal?.minimum ?: 0.0
    val maximum = budgetGoal?.maximum ?: 0.0

    val monthItems = expensesInCurrentMonth(expenses)
    val monthlySpent = monthlyExpenseTotal(monthItems)

    val periodLabel = when (selectedPeriod) {
        "week" -> "This week"
        "month" -> "This month"
        else -> "Custom period"
    }
    val rangeText = "%d/%d/%d – %d/%d/%d".format(
        startDay, startMonth + 1, startYear,
        endDay, endMonth + 1, endYear
    )

    ScrollableScreen(
        modifier = modifier.fillMaxSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GraphDarkBlue
                )
            }
            Column {
                Text(
                    text = "Spending Graphs",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = GraphDarkBlue
                )
                Text(
                    text = "Category totals and monthly goals",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select period",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GraphDarkBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GraphPeriodButton(
                        label = "This week",
                        selected = selectedPeriod == "week",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPeriod = "week"
                            applyThisWeek()
                        }
                    )
                    GraphPeriodButton(
                        label = "This month",
                        selected = selectedPeriod == "month",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPeriod = "month"
                            applyThisMonth()
                        }
                    )
                    GraphPeriodButton(
                        label = "Custom",
                        selected = selectedPeriod == "custom",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedPeriod = "custom" }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$periodLabel ($rangeText)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BarBlue
                )
                if (selectedPeriod == "custom") {
                    Spacer(modifier = Modifier.height(12.dp))
                    GraphDateRow(
                        title = "From",
                        day = startDay,
                        month = startMonth,
                        year = startYear,
                        months = months,
                        years = years,
                        onDayChange = { startDay = it },
                        onMonthChange = {
                            startMonth = it
                            startDay = startDay.coerceAtMost(graphDaysInMonth(it, startYear))
                        },
                        onYearChange = {
                            startYear = it
                            startDay = startDay.coerceAtMost(graphDaysInMonth(startMonth, it))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GraphDateRow(
                        title = "To",
                        day = endDay,
                        month = endMonth,
                        year = endYear,
                        months = months,
                        years = years,
                        onDayChange = { endDay = it },
                        onMonthChange = {
                            endMonth = it
                            endDay = endDay.coerceAtMost(graphDaysInMonth(it, endYear))
                        },
                        onYearChange = {
                            endYear = it
                            endDay = endDay.coerceAtMost(graphDaysInMonth(endMonth, it))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Spent per category",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GraphDarkBlue
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Bars are category totals. Dashed lines are your minimum and maximum goals.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (rangeInvalid) {
            GraphMessageCard("The end date cannot be before the start date.")
        } else if (categoryTotals.isEmpty()) {
            GraphMessageCard("Add a category and an expense to see this graph.")
        } else {
            CategorySpendChart(
                totals = categoryTotals,
                minimum = minimum,
                maximum = maximum
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Period total: R %.2f".format(periodTotal),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GraphDarkBlue
            )
            if (maximum <= 0.0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set a minimum and maximum on the Budget screen to draw the goal lines.",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Past month vs goals",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GraphDarkBlue
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "How this calendar month sits between your minimum and maximum.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(10.dp))

        MonthGoalBand(
            spent = monthlySpent,
            minimum = minimum,
            maximum = maximum
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CategorySpendChart(
    totals: List<Pair<String, Double>>,
    minimum: Double,
    maximum: Double
) {
    val highestBar = totals.maxOfOrNull { it.second } ?: 0.0
    val scale = maxOf(highestBar, maximum, 1.0)
    val itemWidth = 72.dp
    val chartWidth = itemWidth * totals.size.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendDot(color = BarBlue, label = "Category")
                if (minimum > 0.0) {
                    LegendDot(color = MinGreen, label = "Min R %.0f".format(minimum))
                }
                if (maximum > 0.0) {
                    LegendDot(color = MaxRed, label = "Max R %.0f".format(maximum))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Canvas(
                    modifier = Modifier
                        .width(chartWidth)
                        .height(200.dp)
                ) {
                    val plotHeight = size.height
                    val slot = size.width / totals.size.coerceAtLeast(1)
                    val barWidth = slot * 0.55f

                    fun valueY(value: Double): Float {
                        val ratio = (value / scale).toFloat().coerceIn(0f, 1f)
                        return plotHeight - (plotHeight * ratio)
                    }

                    if (maximum > 0.0) {
                        val y = valueY(maximum)
                        drawLine(
                            color = MaxRed,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 4f
                        )
                    }
                    if (minimum > 0.0) {
                        val y = valueY(minimum)
                        drawLine(
                            color = MinGreen,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 4f
                        )
                    }

                    totals.forEachIndexed { index, (_, amount) ->
                        val barHeight = (plotHeight * (amount / scale).toFloat()).coerceAtLeast(
                            if (amount > 0.0) 6.dp.toPx() else 2.dp.toPx()
                        )
                        val left = slot * index + (slot - barWidth) / 2f
                        drawRoundRect(
                            color = barColours[index % barColours.size],
                            topLeft = Offset(left, plotHeight - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
                Row(modifier = Modifier.width(chartWidth)) {
                    totals.forEach { (name, _) ->
                        Text(
                            text = name,
                            modifier = Modifier.width(itemWidth),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GraphDarkBlue,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGoalBand(
    spent: Double,
    minimum: Double,
    maximum: Double
) {
    val hasGoals = maximum > 0.0 && minimum >= 0.0 && minimum <= maximum
    val statusText = when {
        !hasGoals -> "Set a minimum and maximum on the Budget screen to use this check."
        spent < minimum -> "Below the minimum. You are R %.2f under R %.2f.".format(
            minimum - spent,
            minimum
        )
        spent <= maximum -> "On track. This month is inside R %.2f to R %.2f.".format(
            minimum,
            maximum
        )
        else -> "Over the maximum by R %.2f.".format(spent - maximum)
    }
    val markerColour = when {
        !hasGoals -> GraphDarkBlue
        spent < minimum -> BelowAmber
        spent <= maximum -> WithinGreen
        else -> MaxRed
    }
    val scale = maxOf(spent, maximum, 1.0) * 1.08

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "R %.2f spent this month".format(spent),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GraphDarkBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Minimum R %.2f    Maximum R %.2f".format(minimum, maximum),
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (hasGoals) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    val corner = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    drawRoundRect(
                        color = GraphLavender,
                        size = size,
                        cornerRadius = corner
                    )
                    val minX = size.width * (minimum / scale).toFloat().coerceIn(0f, 1f)
                    val maxX = size.width * (maximum / scale).toFloat().coerceIn(0f, 1f)
                    val bandLeft = minOf(minX, maxX)
                    val bandRight = maxOf(minX, maxX).coerceAtLeast(bandLeft + 8f)
                    drawRoundRect(
                        color = WithinGreen.copy(alpha = 0.45f),
                        topLeft = Offset(bandLeft, 0f),
                        size = Size(bandRight - bandLeft, size.height),
                        cornerRadius = corner
                    )
                    val markerX = (size.width * (spent / scale).toFloat())
                        .coerceIn(8f, size.width - 8f)
                    drawRoundRect(
                        color = markerColour,
                        topLeft = Offset(markerX - 6f, 0f),
                        size = Size(12f, size.height),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("R0", fontSize = 12.sp, color = Color.Gray)
                    Text("Min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinGreen)
                    Text("Max", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaxRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = markerColour
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(14.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GraphDarkBlue)
    }
}

@Composable
private fun GraphMessageCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GraphDarkBlue
        )
    }
}

@Composable
private fun GraphPeriodButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GraphDarkBlue)
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(42.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(label, fontSize = 13.sp)
        }
    }
}

@Composable
private fun GraphDateRow(
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
    val maxDay = graphDaysInMonth(month, year)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GraphDarkBlue,
            modifier = Modifier.width(40.dp)
        )
        GraphDropdown(
            value = day.toString(),
            options = (1..maxDay).map { it.toString() },
            modifier = Modifier.weight(1f),
            onSelected = { onDayChange(it.toInt()) }
        )
        GraphDropdown(
            value = months[month],
            options = months,
            modifier = Modifier.weight(1.2f),
            onSelected = { onMonthChange(months.indexOf(it)) }
        )
        GraphDropdown(
            value = year.toString(),
            options = years.map { it.toString() },
            modifier = Modifier.weight(1.2f),
            onSelected = { onYearChange(it.toInt()) }
        )
    }
}

@Composable
private fun GraphDropdown(
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
            Text(value, fontSize = 13.sp)
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

private fun categoryTotalsForPeriod(
    periodExpenses: List<Expense>,
    categories: List<String>
): List<Pair<String, Double>> {
    val totals = linkedMapOf<String, Double>()
    for (name in categories) {
        totals[name] = 0.0
    }
    for (expense in periodExpenses) {
        totals[expense.category] = totals.getOrDefault(expense.category, 0.0) + expense.amount
    }
    return totals.map { it.key to it.value }
}

private fun expenseInRange(
    expense: Expense,
    rangeStart: Calendar,
    rangeEnd: Calendar
): Boolean {
    if (expense.isIncome) return false
    val date = parseExpenseDate(expense.startDate) ?: return false
    val whenSpent = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return !whenSpent.before(rangeStart) && !whenSpent.after(rangeEnd)
}

private fun graphCalendar(year: Int, month: Int, day: Int, endOfDay: Boolean): Calendar {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day.coerceAtMost(graphDaysInMonth(month, year)))
        if (endOfDay) {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        } else {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}

private fun graphDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
