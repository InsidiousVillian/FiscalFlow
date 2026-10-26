package com.example.fiscalflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUp: (username: String, password: String, result: (Boolean) -> Unit) -> Unit,
    onSignUpSuccess: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    ScrollableScreen(
        modifier = modifier.fillMaxSize(),
        fillViewport = true,
        applyImePadding = true,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(24.dp)
    ) {
        Text(
            text = "Create Account",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMessage = null },
            label = { Text("Username", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            label = { Text("Confirm Password", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            enabled = !submitting,
            onClick = {
                when {
                    //Ensure the all registration fields
                    username.isBlank() ||
                            password.isBlank() ||
                            confirmPassword.isBlank() ->
                        errorMessage = "All fields are required"

                   password != confirmPassword ->
                        errorMessage = "Passwords do not match"

                    else -> {
                        submitting = true
                        errorMessage = null
                        //Register firebase account
                        FirebaseAuthManager().register(
                            email = firebaseEmail,
                            password = password
                        ) { firebaseSuccess, firebaseMessage ->

                            if (firebaseSuccess) {
                                //Allow for the local room database to be created too
                                onSignUp(username.trim(), password) { success ->
                                    submitting = false

                                    if (success) {
                                        onSignUpSuccess(username.trim())
                                    } else {
                                        errorMessage =
                                            "Firebase account created, but local registration failed. Please try signing in or contact support."
                                    }
                                }
                            } else {
                                submitting = false
                                errorMessage = firebaseMessage
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (submitting) "Creating..." else "Sign Up",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Already have an account? Log In",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
