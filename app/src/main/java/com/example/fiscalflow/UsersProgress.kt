package com.example.fiscalflow

// Tracks overall gamification progress including XP points, streaks, and unlocked milestones per user.
data class UsersProgress(
    val xp: Int = 0,
    val streak: Int = 0,
    val milestonesRaw: String = "",
    val id: Int = SINGLE_ROW_ID,
    val ownerUsername: String = "unassigned_legacy"
) {
    val milestones: List<String>
        get() = if (milestonesRaw.isBlank()) emptyList() else milestonesRaw.split("|")

    companion object {
        const val SINGLE_ROW_ID: Int = 1

        fun fromList(
            xp: Int = 0,
            streak: Int = 0,
            milestones: List<String> = emptyList(),
            ownerUsername: String = "unassigned_legacy"
        ): UsersProgress = UsersProgress(
            xp = xp,
            streak = streak,
            milestonesRaw = milestones.joinToString("|"),
            ownerUsername = ownerUsername
        )
    }
}
