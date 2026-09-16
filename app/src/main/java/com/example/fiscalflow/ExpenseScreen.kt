package com.example.fiscalflow

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fiscalflow.R
import java.util.Calendar

@Composable
fun ExpenseScreen(
    modifier: Modifier = Modifier,
    categories: List<String>,                 // categories loaded from Room via ViewModel
    onExpenseSaved: (Expense) -> Unit = {},   // called after save
    onBack: () -> Unit = {}                   // goes back
) {
    val today = remember { Calendar.getInstance() }
    val currentYear = today.get(Calendar.YEAR)

    // Form fields
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Start date
    var startDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var startMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var startYear by remember { mutableIntStateOf(currentYear) }

    // End date
    var endDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var endMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var endYear by remember { mutableIntStateOf(currentYear) }

    // Category chosen for this expense
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull().orEmpty())
    }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoryValue = when {
        categories.isEmpty() -> ""
        selectedCategory in categories -> selectedCategory
        else -> categories.first()
    }

    val months = remember {
        listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
    }
    val years = remember(currentYear) {
        ((currentYear - 5)..(currentYear + 1)).toList()
    }
    //belinda
    val startDate = Calendar.getInstance().apply {
        set(
            startYear,
            startMonth,
            startDay,
            0,
            0,
            0
        )
        set(Calendar.MILLISECOND, 0)
    }
    //belinda
    val endDate = Calendar.getInstance().apply {
        set(
            endYear,
            endMonth,
            endDay,
            23,
            59,
            59
        )
        set(Calendar.MILLISECOND, 999)
    }
    val context = LocalContext.current

    // Opens file picker for images (works better on emulator than GetContent alone)
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Still try to preview even if persist fails
            }
            photoUri = uri
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Blue header block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1976D2))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Fill in details and attach a receipt if you have one",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE3F2FD)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = null
            },
            label = { Text("Amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Start date
        DateRollerRow(
            title = "Start date",
            selectedDay = startDay,
            selectedMonth = startMonth,
            selectedYear = startYear,
            months = months,
            years = years,
            onDaySelected = {
                startDay = it
                errorMessage = null
            },
            onMonthSelected = { month ->
                startMonth = month
                startDay = startDay.coerceAtMost(daysInMonth(month, startYear))
                errorMessage = null
            },
            onYearSelected = { year ->
                startYear = year
                startDay = startDay.coerceAtMost(daysInMonth(startMonth, year))
                errorMessage = null
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // End date
        DateRollerRow(
            title = "End date",
            selectedDay = endDay,
            selectedMonth = endMonth,
            selectedYear = endYear,
            months = months,
            years = years,
            onDaySelected = {
                endDay = it
                errorMessage = null
            },
            onMonthSelected = { month ->
                endMonth = month
                endDay = endDay.coerceAtMost(daysInMonth(month, endYear))
                errorMessage = null
            },
            onYearSelected = { year ->
                endYear = year
                endDay = endDay.coerceAtMost(daysInMonth(endMonth, year))
                errorMessage = null
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                errorMessage = null
            },
            label = { Text("Description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category dropdown (same categories as Category screen)
        SimpleDropdown(
            label = "Category",
            value = categoryValue,
            options = categories.toList(),
            modifier = Modifier.fillMaxWidth(),
            onOptionSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Attach photo button
        Button(
            onClick = { pickImage.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Attach photo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Receipt preview box (placeholder drawable when empty)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Receipt preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_receipt_placeholder),
                        contentDescription = "Receipt placeholder",
                        modifier = Modifier.height(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No receipt attached yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save expense button
        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                val startDateText = "$startDay/${startMonth + 1}/$startYear"
                val endDateText = "$endDay/${endMonth + 1}/$endYear"

                if (amount.isBlank() || description.isBlank()) {
                    errorMessage = "Please fill in amount and description"
                    return@Button
                }
                if (categoryValue.isBlank()) {
                    errorMessage = "Please select a category"
                    return@Button
                }
                if (parsedAmount == null || parsedAmount <= 0.0) {
                    errorMessage = "Enter a valid amount"
                    return@Button
                }

                val startCal = Calendar.getInstance().apply {
                    set(startYear, startMonth, startDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (endCal.before(startCal)) {
                    errorMessage = "End date cannot be before start date"
                    return@Button
                }

                val expense = Expense(
                    amount = parsedAmount,
                    startDate = startDateText,
                    endDate = endDateText,
                    description = description.trim(),
                    category = categoryValue,
                    photoUri = photoUri?.toString()
                )

                // Save then return to categories (handled in MainActivity)
                onExpenseSaved(expense)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}

private fun daysInMonth(month: Int, year: Int): Int {
    val temp = Calendar.getInstance()
    temp.set(Calendar.YEAR, year)
    temp.set(Calendar.MONTH, month)
    temp.set(Calendar.DAY_OF_MONTH, 1)
    return temp.getActualMaximum(Calendar.DAY_OF_MONTH)
}

@Composable
private fun DateRollerRow(
    title: String,
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    months: List<String>,
    years: List<Int>,
    onDaySelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    val maxDay = remember(selectedMonth, selectedYear) {
        daysInMonth(selectedMonth, selectedYear)
    }
    val days = remember(maxDay) { (1..maxDay).toList() }

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SimpleDropdown(
            label = "Day",
            value = selectedDay.toString(),
            options = days.map { it.toString() },
            modifier = Modifier.weight(1f),
            onOptionSelected = { onDaySelected(it.toInt()) }
        )

        SimpleDropdown(
            label = "Month",
            value = months[selectedMonth],
            options = months,
            modifier = Modifier.weight(1.2f),
            onOptionSelected = { onMonthSelected(months.indexOf(it)) }
        )

        SimpleDropdown(
            label = "Year",
            value = selectedYear.toString(),
            options = years.map { it.toString() },
            modifier = Modifier.weight(1.2f),
            onOptionSelected = { onYearSelected(it.toInt()) }
        )
    }
}

@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
