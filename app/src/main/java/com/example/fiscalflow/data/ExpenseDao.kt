package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.fiscalflow.Expense
import kotlinx.coroutines.flow.Flow

// Database queries for managing expense records
@Dao
interface ExpenseDao {


    @Query("SELECT * FROM expenses WHERE ownerUsername = :ownerUsername ORDER BY id DESC")
    fun observeAll(ownerUsername: String): Flow<List<Expense>>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    // Delete a category's expenses only for the selected account
    @Query("DELETE FROM expenses WHERE ownerUsername = :ownerUsername AND category = :categoryName")
    suspend fun deleteByCategory(ownerUsername: String, categoryName: String)

    // Check an expense only within the selected account
    @Query("SELECT EXISTS(SELECT 1 FROM expenses WHERE ownerUsername = :ownerUsername AND id = :expenseId)")
    suspend fun existsById(ownerUsername: String, expenseId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreExpense(expense: Expense)

    // Count expenses for migration checks and account-specific totals
    @Query("SELECT COUNT(*) FROM expenses WHERE ownerUsername = :ownerUsername")
    suspend fun count(ownerUsername: String): Int

    @Query("SELECT * FROM expenses WHERE ownerUsername = :ownerUsername AND id = :expenseId LIMIT 1")
    suspend fun getById(ownerUsername: String, expenseId: Long): Expense?

}
