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

    // Emits live updates whenever category records change
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE name = :categoryName")
    suspend fun deleteByName(categoryName: String)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
