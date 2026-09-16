package com.example.fiscalflow

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
