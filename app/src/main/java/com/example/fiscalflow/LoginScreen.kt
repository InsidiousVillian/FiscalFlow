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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Login screen. Instead of holding an in-memory `Map<username, password>`, this now delegates
 * credential checking to `onLogin`, which asks the Room-backed ViewModel to look up the user.
 *
 * `onLogin` is asynchronous (Room DAOs are suspend), so the callback returns its result via
 * an inner lambda instead of a plain Boolean.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: (username: String, password: String, result: (Boolean) -> Unit) -> Unit,
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Disables the button while we're waiting on the DB so users can't double-submit.
    var loggingIn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !loggingIn,
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter both username and password"
                    return@Button
                }
                loggingIn = true
                onLogin(username.trim(), password) { success ->
                    loggingIn = false
                    if (success) {
                        errorMessage = null
                        onLoginSuccess(username.trim())
                    } else {
                        errorMessage = "Invalid username or password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loggingIn) "Signing in…" else "Log In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text("Don't have an account? Sign Up")
        }
    }
}
