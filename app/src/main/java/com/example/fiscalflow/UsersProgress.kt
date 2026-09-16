package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tracks overall gamification progress including XP points, streaks, and unlocked milestones.
@Entity(tableName = "user_progress")
data class UsersProgress(
    val xp: Int = 0,
    val streak: Int = 0,
    // Joined with pipe delimiter "|" for Room storage
    val milestonesRaw: String = "",
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID
) {
    // Helper property to access milestones as a list
    val milestones: List<String>
        get() = if (milestonesRaw.isBlank()) emptyList() else milestonesRaw.split("|")

    companion object {
        const val SINGLE_ROW_ID: Int = 1

        fun fromList(
            xp: Int = 0,
            streak: Int = 0,
            milestones: List<String> = emptyList()
        ): UsersProgress = UsersProgress(
            xp = xp,
            streak = streak,
            milestonesRaw = milestones.joinToString("|")
        )
    }
}
