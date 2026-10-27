package com.example.fiscalflow.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object FiscalFlowInputDefaults {
    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.Black.copy(alpha = 0.6f),
        errorTextColor = Color.Black,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = Color(0xFF7951A8),
        unfocusedLabelColor = Color(0xFF4B5563),
        focusedPlaceholderColor = Color(0xFF666666),
        unfocusedPlaceholderColor = Color(0xFF666666),
        focusedPrefixColor = Color.Black,
        unfocusedPrefixColor = Color.Black,
        focusedSuffixColor = Color.Black,
        unfocusedSuffixColor = Color.Black,
        focusedBorderColor = Color(0xFF7951A8),
        unfocusedBorderColor = Color(0xFF9CA3AF)
    )
}
