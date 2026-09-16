package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // Flow<T> from Room emits a new list every time the underlying table changes.
    // The UI collects this Flow so category cards update automatically after inserts/deletes.
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    // IGNORE means "if this category name already exists, do nothing" — SQLite enforces uniqueness
    // via the primary key on `name`, so this doubles as the "no duplicate categories" rule.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteByName(name: String)
}
