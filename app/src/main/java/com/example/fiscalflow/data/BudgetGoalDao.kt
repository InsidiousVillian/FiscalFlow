package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.BudgetingGoal
import kotlinx.coroutines.flow.Flow

// Database operations for monthly budget goal
@Dao
interface BudgetGoalDao {

    @Query("SELECT * FROM budget_goal WHERE ownerUsername = :ownerUsername COLLATE NOCASE AND id = ${BudgetingGoal.SINGLE_ROW_ID} LIMIT 1")
    fun observeCurrent(ownerUsername: String): Flow<BudgetingGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: BudgetingGoal)

    @Query("SELECT EXISTS(SELECT 1 FROM budget_goal WHERE ownerUsername = :ownerUsername COLLATE NOCASE)")
    suspend fun hasGoal(ownerUsername: String): Boolean
}
