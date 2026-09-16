package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.UsersProgress
import kotlinx.coroutines.flow.Flow

// Database operations for user gamification stats
@Dao
interface UserProgressDao {

    @Query("SELECT * FROM user_progress WHERE id = ${UsersProgress.SINGLE_ROW_ID} LIMIT 1")
    fun observeCurrent(): Flow<UsersProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UsersProgress)
}
