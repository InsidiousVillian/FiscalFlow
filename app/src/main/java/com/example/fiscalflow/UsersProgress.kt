package com.example.fiscalflow

//store data to impliment gamified features
data class UsersProgress (
    val xp: Int = 0,
    val streak: Int = 0,
    val milestones: List<String> = emptyList()
)