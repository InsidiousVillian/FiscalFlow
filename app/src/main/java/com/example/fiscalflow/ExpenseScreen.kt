package com.example.fiscalflow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.fiscalflow.R
import java.util.Calendar

// Screen for creating expenses or income entries, picking dates, categories, and attaching receipt images
@Composable
fun ExpenseScreen(
    modifier: Modifier = Modifier,
    categories: List<String>,
    initialIsIncome: Boolean = false,
    initialPhotoUri: String? = null,
    initialPhotoName: String? = null,
    onOpenGallery: () -> Unit = {},
    onExpenseSaved: (Expense) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val today = remember { Calendar.getInstance() }
    val currentYear = today.get(Calendar.YEAR)

    // Toggle state: false = Expense, true = Income
    var isIncomeType by remember(initialIsIncome) { mutableStateOf(initialIsIncome) }

    // Form field states
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Start date picker state
    var startDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var startMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var startYear by remember { mutableIntStateOf(currentYear) }

    // End date picker state
    var endDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    var endMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var endYear by remember { mutableIntStateOf(currentYear) }

    // Default categories depending on Expense or Income mode
    val incomeCategories = remember { listOf("Salary", "Freelance", "Investment", "Business", "Gift", "General Income") }
    val activeCategories = if (isIncomeType) incomeCategories else categories

    var selectedCategory by remember(isIncomeType) {
        mutableStateOf(activeCategories.firstOrNull().orEmpty())
    }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Sync image from gallery selection if passed from MainActivity
    LaunchedEffect(initialPhotoUri, initialPhotoName) {
        if (!initialPhotoUri.isNullOrBlank()) {
            photoUri = initialPhotoUri.toUri()
            selectedImageName = initialPhotoName ?: "Gallery Image"
        }
    }

    val categoryValue = when {
        activeCategories.isEmpty() -> ""
        selectedCategory in activeCategories -> selectedCategory
        else -> activeCategories.first()
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

    // Opens system file picker for selecting a receipt image
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
            selectedImageName = getUriFileName(context, uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEDEBFA))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Screen Header Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isIncomeType) Color(0xFF15803D) else Color(0xFF172A46))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text(
                    text = if (isIncomeType) "Add Income Entry" else "Add Expense Entry",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isIncomeType) "Record new earnings in Rands" else "Log spending and attach receipt image",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // TRANSACTION TYPE SELECTOR (EXPENSE vs INCOME)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { isIncomeType = false },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isIncomeType) Color(0xFFE57373) else Color.White
                )
            ) {
                Text(
                    text = "↓ Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isIncomeType) Color.White else Color(0xFF172A46)
                )
            }

            Button(
                onClick = { isIncomeType = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncomeType) Color(0xFF4CAF50) else Color.White
                )
            ) {
                Text(
                    text = "↑ Income",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncomeType) Color.White else Color(0xFF172A46)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Expense / Income Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = null
            },
            label = { Text("Amount in Rands (R)", fontSize = 15.sp) },
            placeholder = { Text("e.g. 250.00", fontSize = 15.sp) },
            prefix = { Text("R ", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Start date roller
        DateRollerRow(
            title = "Start Date",
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

        Spacer(modifier = Modifier.height(14.dp))

        // End date roller
        DateRollerRow(
            title = "End Date",
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
                endDay = endDay.coerceAtMost(daysInMonth(startMonth, year))
                errorMessage = null
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                errorMessage = null
            },
            label = { Text("Description", fontSize = 15.sp) },
            placeholder = { Text("e.g. Monthly salary, Grocery run", fontSize = 14.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category dropdown
        SimpleDropdown(
            label = "Category",
            value = categoryValue,
            options = activeCategories,
            modifier = Modifier.fillMaxWidth(),
            onOptionSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ATTACH RECEIPT PHOTO SECTION
        Text(
            text = "Attach Receipt / Document Image",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172A46)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Gallery & File options row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF172A46))
            ) {
                Text("🖼️ Sample Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = { pickImage.launch(arrayOf("image/*")) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📁 Device Files", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // DISPLAY SELECTED FILE NAME & PREVIEW BOX
        if (selectedImageName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📎 Attached: $selectedImageName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            photoUri = null
                            selectedImageName = null
                        }
                    ) {
                        Text("Remove", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Receipt Image Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = selectedImageName ?: "Receipt image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📷",
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No receipt image attached yet",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Transaction Button (Expense or Income)
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
                    errorMessage = "Enter a valid amount in Rands"
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
                    photoUri = photoUri?.toString(),
                    isIncome = isIncomeType
                )

                onExpenseSaved(expense)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isIncomeType) Color(0xFF15803D) else Color(0xFF172A46)
            )
        ) {
            Text(
                text = if (isIncomeType) "Save Income Entry" else "Save Expense Entry",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel & Return", fontSize = 15.sp)
        }
    }
}

private fun getUriFileName(context: Context, uri: Uri): String {
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) return name
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore query exception
        }
    }
    val lastSegment = uri.lastPathSegment?.substringAfterLast('/')
    if (!lastSegment.isNullOrBlank()) {
        return lastSegment
    }
    return "receipt_image.jpg"
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
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF172A46)
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
            label = { Text(label, fontSize = 14.sp) },
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
                    text = { Text(option, fontSize = 15.sp) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
