package com.example.fiscalflow

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Jet: shared date format used by Progress Report and spending alerts ("D/M/YYYY").
private val expenseDateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

// Jet: one category's month spend vs its fair share of the monthly max.
data class CategorySpendReport(
    val name: String,
    val amount: Double,
    val fairShare: Double,
    val isOverspent: Boolean,
    val percentOver: Double,
    val percentOfMax: Float
)

data class DailySpendBar(
    val label: String,
    val amount: Double
)

// Jet: None = no banner/notification; Approaching = 80%+ of max; Over = past max.
enum class SpendingAlertLevel {
    None,
    Approaching,
    Over
}

data class SpendingAlert(
    val level: SpendingAlertLevel,
    val title: String,
    val message: String,
    val monthlySpent: Double,
    val maximum: Double
)

// Calculates how much of the max budget limit has been spent so far
fun calculateProgress(
    totalSpent: Double,
    maximum: Double
): Float {
    // Avoid dividing by zero if no budget target is set
    if (maximum <= 0) return 0f

    // Return capped spending ratio between 0.0 and 1.0
    return (totalSpent / maximum)
        .coerceIn(0.0, 1.0)
        .toFloat()
}

fun parseExpenseDate(dateStr: String): Date? {
    return try {
        expenseDateFormat.parse(dateStr)
    } catch (_: Exception) {
        null
    }
}

// Jet: keep Progress Report / alerts on the current calendar month only.
fun expensesInCurrentMonth(expenses: List<Expense>): List<Expense> {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    return expenses.filter { expense ->
        val date = parseExpenseDate(expense.startDate) ?: return@filter false
        val expenseCalendar = Calendar.getInstance()
        expenseCalendar.time = date
        expenseCalendar.get(Calendar.MONTH) == currentMonth &&
            expenseCalendar.get(Calendar.YEAR) == currentYear
    }
}

fun monthlyExpenseTotal(monthItems: List<Expense>): Double {
    return monthItems.filter { !it.isIncome }.sumOf { it.amount }
}

fun monthlyIncomeTotal(monthItems: List<Expense>): Double {
    return monthItems.filter { it.isIncome }.sumOf { it.amount }
}

// Jet: a category is overspent if it exceeds monthlyMax / categoryCount (or 40% of spend with no goal).
fun categorySpendReports(
    monthItems: List<Expense>,
    categories: List<String>,
    budgetGoal: BudgetingGoal?
): List<CategorySpendReport> {
    val monthExpenses = monthItems.filter { !it.isIncome }
    val totals = mutableMapOf<String, Double>()
    for (expense in monthExpenses) {
        totals[expense.category] = totals.getOrDefault(expense.category, 0.0) + expense.amount
    }

    val names = (categories + totals.keys).distinct()
    val monthTotal = monthExpenses.sumOf { it.amount }
    val maximum = budgetGoal?.maximum ?: 0.0
    val categoryCount = names.size.coerceAtLeast(1)
    val fairShare = if (maximum > 0) {
        maximum / categoryCount
    } else {
        monthTotal * 0.4
    }

    return names.map { name ->
        val amount = totals.getOrDefault(name, 0.0)
        val isOverspent = if (maximum > 0) {
            amount > fairShare && fairShare > 0
        } else {
            monthTotal > 0 && amount > monthTotal * 0.4
        }
        val percentOver = if (fairShare > 0 && amount > fairShare) {
            ((amount - fairShare) / fairShare) * 100.0
        } else {
            0.0
        }
        val percentOfMax = if (maximum > 0) {
            (amount / maximum).coerceIn(0.0, 1.0).toFloat()
        } else if (monthTotal > 0) {
            (amount / monthTotal).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }
        CategorySpendReport(
            name = name,
            amount = amount,
            fairShare = fairShare,
            isOverspent = isOverspent,
            percentOver = percentOver,
            percentOfMax = percentOfMax
        )
    }.sortedByDescending { it.amount }
}

// Jet: one coaching sentence for the Progress Report hero card.
fun pickProgressMessage(
    budgetGoal: BudgetingGoal?,
    monthlySpent: Double,
    topCategory: String?
): String {
    val maximum = budgetGoal?.maximum ?: 0.0
    val minimum = budgetGoal?.minimum ?: 0.0
    val top = topCategory ?: "your top category"

    return when {
        maximum <= 0 ->
            "Set a monthly budget so FiscalFlow can coach your spending."
        monthlySpent > maximum ->
            "You've gone over your monthly max. ${top} is the biggest pull on the budget."
        monthlySpent >= maximum * 0.8 ->
            "You're over 80% of your monthly max. Slow down before the month ends."
        monthlySpent < minimum ->
            "You're below your minimum goal. Make sure essentials are covered."
        else ->
            "You're on track. Keep logging and stay between your min and max."
    }
}

// Jet: used after saving an expense and for the dashboard banner.
fun evaluateSpendingAlert(
    expenses: List<Expense>,
    budgetGoal: BudgetingGoal?
): SpendingAlert {
    val maximum = budgetGoal?.maximum ?: 0.0
    val monthlySpent = monthlyExpenseTotal(expensesInCurrentMonth(expenses))
    if (maximum <= 0) {
        return SpendingAlert(
            level = SpendingAlertLevel.None,
            title = "",
            message = "",
            monthlySpent = monthlySpent,
            maximum = maximum
        )
    }

    return when {
        monthlySpent > maximum -> SpendingAlert(
            level = SpendingAlertLevel.Over,
            title = "Over your monthly budget",
            message = "You've spent R %.2f of your R %.2f max. Open Progress Report to see which categories are pulling you over."
                .format(monthlySpent, maximum),
            monthlySpent = monthlySpent,
            maximum = maximum
        )
        monthlySpent >= maximum * 0.8 -> SpendingAlert(
            level = SpendingAlertLevel.Approaching,
            title = "Close to your monthly max",
            message = "You've used %.0f%% of your R %.2f max. Slow down before the month ends."
                .format((monthlySpent / maximum) * 100.0, maximum),
            monthlySpent = monthlySpent,
            maximum = maximum
        )
        else -> SpendingAlert(
            level = SpendingAlertLevel.None,
            title = "",
            message = "",
            monthlySpent = monthlySpent,
            maximum = maximum
        )
    }
}

// Jet: seven daily expense totals for the Progress Report column chart.
fun lastSevenDaySpends(expenses: List<Expense>): List<DailySpendBar> {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val bars = mutableListOf<DailySpendBar>()
    val today = Calendar.getInstance()

    for (offset in 6 downTo 0) {
        val day = Calendar.getInstance()
        day.timeInMillis = today.timeInMillis
        day.add(Calendar.DAY_OF_YEAR, -offset)
        day.set(Calendar.HOUR_OF_DAY, 0)
        day.set(Calendar.MINUTE, 0)
        day.set(Calendar.SECOND, 0)
        day.set(Calendar.MILLISECOND, 0)

        val amount = expenses.sumOf { expense ->
            if (expense.isIncome) return@sumOf 0.0
            val date = parseExpenseDate(expense.startDate) ?: return@sumOf 0.0
            val expenseDay = Calendar.getInstance()
            expenseDay.time = date
            expenseDay.set(Calendar.HOUR_OF_DAY, 0)
            expenseDay.set(Calendar.MINUTE, 0)
            expenseDay.set(Calendar.SECOND, 0)
            expenseDay.set(Calendar.MILLISECOND, 0)
            if (expenseDay.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                expenseDay.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
            ) {
                expense.amount
            } else {
                0.0
            }
        }

        bars.add(DailySpendBar(label = dayFormat.format(day.time), amount = amount))
    }

    return bars
}
