package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// A user-defined budget category (e.g. "Groceries").
// `name` is the primary key so we get automatic "unique category name" enforcement
// straight from SQLite instead of hand-rolling the check in Kotlin.
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String
)
