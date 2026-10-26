
package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.BudgetingGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {

    // Observe the budget belonging to the selected account
    @Query("SELECT * FROM budget_goal WHERE ownerUsername = :ownerUsername AND id = ${BudgetingGoal.SINGLE_ROW_ID} LIMIT 1")
    fun observeCurrent(ownerUsername: String): Flow<BudgetingGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: BudgetingGoal)

    // Check whether this account has saved a budget
    @Query("SELECT EXISTS(SELECT 1 FROM budget_goal WHERE ownerUsername = :ownerUsername)")
    suspend fun hasGoal(ownerUsername: String): Boolean
}
