package com.example.fiscalflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// Colours
private val DarkBlue = Color(0xFF172554)
private val Lavender = Color(0xFFEDE9FE)
private val LightLavender = Color(0xFFF7F5FF)
private val Blue = Color(0xFF3B82F6)
private val DarkText = Color(0xFF172033)
private val Grey = Color(0xFF64748B)

@Composable
fun BudgetingScreen(
    modifier: Modifier = Modifier,
    currentGoal: BudgetingGoal?,
    totalSpent: Double = 0.0,
    currentStreak: Int = 0,
    onGoalSaved: (BudgetingGoal) -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    val monthName = calendar.getDisplayName(
        Calendar.MONTH,
        Calendar.LONG,
        java.util.Locale.getDefault()
    ) ?: "Month"

    val minimum = currentGoal?.minimum ?: 0.0
    val maximum = currentGoal?.maximum ?: 0.0
    val remaining = (maximum - totalSpent).coerceAtLeast(0.0)
    val progress = if (maximum > 0) {
        (totalSpent / maximum).coerceIn(0.0, 1.0)
    } else 0.0

    // Calculate days remaining in the current month
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysRemaining = lastDay - today

    // Calculate average daily spending
    val averageDailySpend = if (today > 0) {
        totalSpent / today
    } else 0.0

    // Calculate how much can be spent per remaining day
    val remainingPerDay = if (daysRemaining > 0) {
        remaining / daysRemaining
    } else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightLavender)
            .padding(20.dp)
    ) {

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkBlue
                )
            }

            Column {
                Text(
                    "BUDGET",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Text(
                    "$monthName $year",
                    fontSize = 14.sp,
                    color = Grey
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Monthly budget goal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(DarkBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MONTHLY BUDGET GOAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = .7f)
                )

                Text(
                    "R${"%,.0f".format(maximum)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GoalAmount("MINIMUM", minimum)
                    GoalAmount("MAXIMUM", maximum)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(Lavender)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = DarkBlue
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Set Monthly Budget", color = DarkBlue)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Spent and remaining
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BudgetInfo("SPENT", totalSpent, Modifier.weight(1f))
            BudgetInfo("REMAINING", remaining, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "BUDGET USED                         ${"%.1f".format(progress * 100)}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp),
            color = Blue,
            trackColor = Lavender
        )

        Spacer(Modifier.height(12.dp))

        // Budget status
        InfoCard(
            title = "🎯 BUDGET STATUS",
            text = when {
                maximum <= 0 -> "Set your budget to start tracking your spending."
                progress < 1 -> "You're within your budget! Keep going! 🌟"
                else -> "You've reached your budget limit. Stay focused! 💙"
            }
        )

        Spacer(Modifier.height(10.dp))

        // Monthly summary
        SummaryCard(
            daysRemaining = daysRemaining,
            averageDailySpend = averageDailySpend,
            remainingPerDay = remainingPerDay
        )

        Spacer(Modifier.height(10.dp))

        // Motivation and streak
        InfoCard(
            title = "🌟 KEEP GOING!",
            text = "Stay within your budget to build your consistency streak.\n\n🔥 Current streak: $currentStreak days"
        )
    }

    // Budget popup
    if (showDialog) {
        BudgetGoalDialog(
            currentGoal = currentGoal,
            month = month,
            year = year,
            onDismiss = { showDialog = false },
            onSave = {
                onGoalSaved(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun GoalAmount(title: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = .65f)
        )
        Text(
            "R${"%,.0f".format(amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BudgetInfo(title: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Grey)
            Spacer(Modifier.height(3.dp))
            Text(
                "R${"%,.0f".format(amount)}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
    }
}

@Composable
fun InfoCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(Modifier.height(5.dp))
            Text(text, fontSize = 13.sp, color = Grey)
        }
    }
}

@Composable
fun SummaryCard(
    daysRemaining: Int,
    averageDailySpend: Double,
    remainingPerDay: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                "📅 MONTHLY SUMMARY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            Spacer(Modifier.height(7.dp))

            SummaryRow("Days remaining", "$daysRemaining")
            SummaryRow("Average daily spend", "R${"%,.0f".format(averageDailySpend)}")
            SummaryRow("Remaining per day", "R${"%,.0f".format(remainingPerDay)}")
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Grey)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
    }
}

// Popup for setting minimum and maximum monthly goals
@Composable
fun BudgetGoalDialog(
    currentGoal: BudgetingGoal?,
    month: Int,
    year: Int,
    onDismiss: () -> Unit,
    onSave: (BudgetingGoal) -> Unit
) {
    var minimum by remember(currentGoal) {
        mutableStateOf(currentGoal?.minimum?.toString() ?: "")
    }
    var maximum by remember(currentGoal) {
        mutableStateOf(currentGoal?.maximum?.toString() ?: "")
    }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Monthly Budget",
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        },
        text = {
            Column {
                Text(
                    "💙 Plan your money. Build better habits.",
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Small steps today can lead to bigger financial goals tomorrow! ",
                    fontSize = 13.sp,
                    color = Grey
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = minimum,
                    onValueChange = {
                        minimum = it
                        error = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Minimum Monthly Goal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = maximum,
                    onValueChange = {
                        maximum = it
                        error = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Maximum Monthly Goal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(5.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minimum.toDoubleOrNull()
                    val max = maximum.toDoubleOrNull()

                    when {
                        min == null || max == null ->
                            error = "Please enter both amounts."

                        min < 0 || max < 0 ->
                            error = "Amounts cannot be negative."

                        min > max ->
                            error = "Minimum cannot be greater than maximum."

                        else -> onSave(
                            BudgetingGoal(
                                minimum = min,
                                maximum = max,
                                month = month,
                                year = year
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(DarkBlue)
            ) {
                Text("Save Monthly Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkBlue)
            }
        }
    )
}
