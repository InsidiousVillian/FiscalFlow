package com.example.fiscalflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val FiscalDarkText = Color(0xFF172033)
@Composable
//Create a function to modify, set ad save the goals
fun BudgetingScreen(
    modifier: Modifier = Modifier,
    currentGoal: BudgetingGoal?,
    onGoalSaved: (BudgetingGoal) -> Unit,
    onBack: () -> Unit) {
//Create and make the values into a string
    var minimum by remember {
        mutableStateOf(currentGoal?.minimum.toString()
        )
    }
    var maximum by remember {
        mutableStateOf(currentGoal?.maximum.toString()
        )
    }
//Create an error when there is no value
    var errorMessage by remember {
        mutableStateOf("")
    }
    //Main screen
    Column(
        modifier = modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    )
    {
        //Header
        Text(
            text = "Monthly Spending Goal",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer( modifier = Modifier.height(16.dp) )

        Text("Set your minimum and maximum monthly spending." )
            Spacer( modifier = Modifier.height(4.dp) )

            // Minimum spending goals implemented
            Text("Minimum monthly goal", fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = FiscalDarkText
            )

            Spacer(modifier = Modifier.height(3.dp))

            OutlinedTextField(
                value = minimum,
                onValueChange = {
                    minimum = it
                    errorMessage = ""
                },
                label = {
                    Text("Minimum monthly goal amount")
                },
                placeholder = {
                    Text("Example: 1000")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Maximum spending goals implemented
            Text(
                "Maximum monthly goal",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = FiscalDarkText
            )

            Spacer(modifier = Modifier.height(3.dp))

            OutlinedTextField(
                value = maximum,
                onValueChange = {
                    maximum = it
                    errorMessage = ""
                },
                label = {
                    Text("Maximum monthly goal amount")
                },
                placeholder = {
                    Text("Example: 8000")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Error message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            //Check values
            Button(
                onClick = {

                    val minimumAmount = minimum.toDoubleOrNull()

                    val maximumAmount = maximum.toDoubleOrNull()

                    //Check the values given are numbers
                    if (minimumAmount == null || maximumAmount == null)
                    {
                        errorMessage = "Please enter valid amounts."
                        return@Button
                    }

                    // Check for negative values
                    if (minimumAmount < 0 || maximumAmount < 0)
                    {
                        errorMessage = "Amounts given cannot be negative."
                        return@Button
                    }

                    //Check that Minimum is not greater than maximum
                    if (minimumAmount > maximumAmount)
                    {
                        errorMessage = "Minimum cannot be greater than maximum."
                        return@Button
                    }

                    //Create the goal
                    val goal = BudgetingGoal(
                        minimum = minimumAmount,
                        maximum = maximumAmount,
                        month = Calendar.getInstance().get(Calendar.MONTH),
                        year = Calendar.getInstance().get(Calendar.YEAR)
                    )

                    //Return the goal
                    onGoalSaved(goal)
                },
                modifier = Modifier.fillMaxWidth())
            {
                Text("Save Goal")
            }

            Spacer(Modifier.height(8.dp))
            //Back button
            Button(
                onClick = onBack,
                Modifier.fillMaxWidth())
            {
                Text("Back")
            }
        }
    }
