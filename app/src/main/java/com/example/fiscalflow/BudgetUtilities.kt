package com.example.fiscalflow

fun calculateProgress(
    totalSpent: Double,
    maximum: Double
): Float
{
    if (maximum <= 0) return 0f

    return (totalSpent / maximum)
        .coerceIn(0.0, 1.0)
        .toFloat()
}