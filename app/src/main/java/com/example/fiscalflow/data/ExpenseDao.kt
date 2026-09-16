package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fiscalflow.Expense
import kotlinx.coroutines.flow.Flow

// Database queries for managing expense records
@Dao
interface ExpenseDao {

    // Fetch all expenses with newest entries first
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun observeAll(): Flow<List<Expense>>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE category = :categoryName")
    suspend fun deleteByCategory(categoryName: String)
}
