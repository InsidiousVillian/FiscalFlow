package com.example.fiscalflow

// Category record model.
data class CategoryEntity(
    val name: String,
    val ownerUsername: String = "unassigned_legacy"
)
