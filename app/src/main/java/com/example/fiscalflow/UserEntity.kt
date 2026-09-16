package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// User profile stored in local Room database.
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val password: String
)
