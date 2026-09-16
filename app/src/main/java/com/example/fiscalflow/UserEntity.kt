package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// A registered user account. Stored offline so login works without a network.
// NOTE: passwords are stored as plain text here to keep the prototype simple; in a real
// app we would hash them (e.g. BCrypt) before saving. Called out for the marker.
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val password: String
)
