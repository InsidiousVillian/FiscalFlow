package com.example.fiscalflow

//Calculate how much of the maximum budget has been spent

fun calculateProgress(
    totalSpent: Double,
    maximum: Double
): Float {
    //Prevent any form of division when there is no maximum budget
    if (maximum <= 0) return 0f

    //Calculates the spending percentage
    return (totalSpent / maximum)
        .coerceIn(0.0, 1.0)
        .toFloat()
}
