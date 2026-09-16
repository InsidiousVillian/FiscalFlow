package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Category database record. Name acts as primary key for uniqueness.
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String
)
