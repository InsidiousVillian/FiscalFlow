package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.UserEntity

// DAO = Data Access Object. Room generates the SQLite-backed implementation for us at build time.
@Dao
interface UserDao {

    // REPLACE lets us overwrite the password if the same username is inserted again.
    // Used by SignUp; for a stricter "no duplicate usernames" flow you could use ABORT and catch.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    // Returns null if the user does not exist. Login compares the stored password against the input.
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
