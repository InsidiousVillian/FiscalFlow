package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.Expense
import kotlinx.coroutines.flow.Flow

// Database queries for managing expense records
@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE ownerUsername = :ownerUsername COLLATE NOCASE ORDER BY id DESC")
    fun observeAll(ownerUsername: String): Flow<List<Expense>>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND category = :categoryName")
    suspend fun deleteByCategory(ownerUsername: String, categoryName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM expenses WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND id = :expenseId)")
    suspend fun existsById(ownerUsername: String, expenseId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreExpense(expense: Expense)

    @Query("SELECT COUNT(*) FROM expenses WHERE ownerUsername = :ownerUsername COLLATE NOCASE")
    suspend fun count(ownerUsername: String): Int

    @Query("SELECT * FROM expenses WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND id = :expenseId LIMIT 1")
    suspend fun getById(ownerUsername: String, expenseId: Long): Expense?
}
