
package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    // Observe XP, streaks, and milestones for the selected account
    @Query("SELECT * FROM user_progress WHERE ownerUsername = :ownerUsername AND id = ${UsersProgress.SINGLE_ROW_ID} LIMIT 1")
    fun observeCurrent(ownerUsername: String): Flow<UsersProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UsersProgress)

    // Retrieve only this account's saved progress
    @Query("SELECT * FROM user_progress WHERE ownerUsername = :ownerUsername AND id = ${UsersProgress.SINGLE_ROW_ID} LIMIT 1")
    suspend fun getCurrent(ownerUsername: String): UsersProgress?
}
