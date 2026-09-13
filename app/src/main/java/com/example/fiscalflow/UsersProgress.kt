package com.example.fiscalflow

data class UsersProgress (
    val xp: Int = 0,
    val streak: Int = 0,
    val milestones: List<String> = emptyList()
)