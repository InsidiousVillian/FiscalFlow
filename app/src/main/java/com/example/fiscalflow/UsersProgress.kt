package com.example.fiscalflow

import androidx.room.Entity
import androidx.room.PrimaryKey

// Gamification progress (XP, streak, milestones). Also a single-row table.
// Milestones are joined into one string with "|" as separator because Room's
// default type converters do not know how to persist List<String>.
@Entity(tableName = "user_progress")
data class UsersProgress(
    val xp: Int = 0,
    val streak: Int = 0,
    // Persisted as a "|"-separated string. Use milestonesList / fromList for the List<String> view.
    val milestonesRaw: String = "",
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID
) {
    // Read-only list view of the underlying raw string. Empty raw -> empty list.
    val milestones: List<String>
        get() = if (milestonesRaw.isBlank()) emptyList() else milestonesRaw.split("|")

    companion object {
        const val SINGLE_ROW_ID: Int = 1

        // Helper to build the entity from a list without callers having to know the storage format.
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
