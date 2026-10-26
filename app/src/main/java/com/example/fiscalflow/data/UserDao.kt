package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.UserEntity

// Data access operations for user accounts
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    // Find whether a Firebase UID is already linked to a local account
    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun findByFirebaseUid(uid: String): UserEntity?

    // Link a Firebase UID only if the account is not linked yet Or if it is already linked to the same UID
    @Query("""
        UPDATE users
        SET firebaseUid = :uid
        WHERE username = :username COLLATE NOCASE
        AND (firebaseUid IS NULL OR firebaseUid = :uid)
    """)
    suspend fun updateFirebaseUid(username: String, uid: String): Int
}
