package com.example.fiscalflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import java.util.Calendar
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
//Create a function to modify, set ad save the goals
fun BudgetingScreen(
    modifier: Modifier = Modifier,
    currentGoal: BudgetingGoal?,
    onGoalSaved: (BudgetingGoal) -> Unit,
    onBack: () -> Unit)
{
//Create and make the values into a string
    var minimum by remember{
        mutableStateOf(
            currentGoal?.minimum.toString()
        )
    }
    var maximum by remember{
        mutableStateOf(
            currentGoal?.maximum.toString()
        )
    }
//Create an error when there is no value
    var errorMessage by remember{
        mutableStateOf("")
    }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Monthly Spending Goal",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Set your minimum and maximum monthly spending."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // Minimum spending goal
        OutlinedTextField(
            value = minimum,
            onValueChange = {
                minimum = it
                errorMessage = ""
            },
            label = {
                Text("Minimum monthly goal")
            },
            placeholder = {
                Text("Example: 3000")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Maximum spending goal
        OutlinedTextField(
            value = maximum,
            onValueChange = {
                maximum = it
                errorMessage = ""
            },
            label = {
                Text("Maximum monthly goal")
            },
            placeholder = {
                Text("Example: 8000")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Error message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {

                val minimumAmount =
                    minimum.toDoubleOrNull()

                val maximumAmount =
                    maximum.toDoubleOrNull()

                // Check that both values are numbers
                if (
                    minimumAmount == null ||
                    maximumAmount == null
                ) {
                    errorMessage =
                        "Please enter valid amounts."
                    return@Button
                }

                // Check for negative values
                if (
                    minimumAmount < 0 ||
                    maximumAmount < 0
                ) {
                    errorMessage =
                        "Amounts cannot be negative."
                    return@Button
                }

                // Minimum cannot be greater than maximum
                if (minimumAmount > maximumAmount) {
                    errorMessage =
                        "Minimum cannot be greater than maximum."
                    return@Button
                }

                // Create the goal
                val goal = BudgetingGoal(
                    minimum = minimumAmount,
                    maximum = maximumAmount,
                    month = Calendar.getInstance().get(Calendar.MONTH),
                    year = Calendar.getInstance().get(Calendar.YEAR)
                )

                // Send goal back to MainActivity
                onGoalSaved(goal)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Goal")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
