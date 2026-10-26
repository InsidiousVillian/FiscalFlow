package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Category database record. Name acts as primary key for uniqueness.
@Entity(
    tableName = "categories",
    primaryKeys = ["ownerUsername", "name"]
)
data class CategoryEntity(
    val name: String,

    // Belinda remains the owner of existing categories after migration
    val ownerUsername: String = "Belinda"
)