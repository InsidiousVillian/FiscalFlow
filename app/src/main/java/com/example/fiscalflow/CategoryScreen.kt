package com.example.fiscalflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val categories = remember { mutableStateListOf("Groceries", "Rent", "Utilities") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Categories",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onLogout) {
                Text("Log Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Input Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it; errorMessage = null },
                label = { Text("New Category") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        errorMessage = "Category name cannot be empty"
                    } else if (categories.any { it.equals(categoryName.trim(), ignoreCase = true) }) {
                        errorMessage = "Category already exists"
                    } else {
                        categories.add(categoryName.trim())
                        categoryName = ""
                        errorMessage = null
                    }
                }
            ) {
                Text("Add")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Categories",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display Category List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}