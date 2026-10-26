
package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Examples: expense, category, budget, progress
    val entityType: String,

    // Examples: upsert, delete
    val operation: String,

    // ID or name identifying the affected record
    val recordId: String,

    // Existing queued changes will be assigned to Belinda in the migration
    val ownerUsername: String = "Belinda"
)
