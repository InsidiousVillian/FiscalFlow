package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fiscalflow.PendingSyncEntity

// Manages changes waiting to sync with Firebase.
@Dao
interface PendingSyncDao {

    @Insert
    suspend fun insert(change: PendingSyncEntity): Long

    @Query("SELECT * FROM pending_sync ORDER BY id ASC")
    suspend fun getPendingChanges(): List<PendingSyncEntity>

    @Query("DELETE FROM pending_sync WHERE id = :changeId")
    suspend fun removeChange(changeId: Long)

    @Query("SELECT COUNT(*) FROM pending_sync")
    suspend fun count(): Int

    //Count queued changes for a specific account
    @Query("SELECT COUNT(*) FROM pending_sync WHERE ownerUsername = :ownerUsername")
    suspend fun countForOwner(ownerUsername: String): Int

}
