package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.CategoryEntity
import kotlinx.coroutines.flow.Flow

// Database operations for spending categories
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE ownerUsername = :ownerUsername COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(ownerUsername: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND name = :categoryName")
    suspend fun deleteByName(ownerUsername: String, categoryName: String)

    @Query("SELECT COUNT(*) FROM categories WHERE ownerUsername = :ownerUsername COLLATE NOCASE")
    suspend fun count(ownerUsername: String): Int

    @Query("SELECT * FROM categories WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND name = :categoryName LIMIT 1")
    suspend fun getByName(ownerUsername: String, categoryName: String): CategoryEntity?
}
