package com.example.fiscalflow

import androidx.room.Entity

// Category database record. Name and ownerUsername act as composite primary key.
@Entity(
    tableName = "categories",
    primaryKeys = ["ownerUsername", "name"]
)
data class CategoryEntity(
    val name: String,
    val ownerUsername: String = "unassigned_legacy"
)
