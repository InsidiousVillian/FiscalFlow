
package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Only show categories owned by the active account
    @Query("SELECT * FROM categories WHERE ownerUsername = :ownerUsername ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(ownerUsername: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    //Delete a category only from the selected account
    @Query("DELETE FROM categories WHERE ownerUsername = :ownerUsername AND name = :categoryName")
    suspend fun deleteByName(ownerUsername: String, categoryName: String)

    //Count categories for the selected account only
    @Query("SELECT COUNT(*) FROM categories WHERE ownerUsername = :ownerUsername")
    suspend fun count(ownerUsername: String): Int

    @Query("SELECT * FROM categories WHERE ownerUsername = :ownerUsername AND name = :categoryName LIMIT 1")
    suspend fun getByName(ownerUsername: String, categoryName: String): CategoryEntity?
}

