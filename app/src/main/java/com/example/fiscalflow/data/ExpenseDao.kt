package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fiscalflow.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Ordered by id DESC so the newest expense appears first in history lists.
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun observeAll(): Flow<List<Expense>>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)
}
