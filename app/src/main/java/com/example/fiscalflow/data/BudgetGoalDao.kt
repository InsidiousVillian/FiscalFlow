package com.example.fiscalflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fiscalflow.BudgetingGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {

    // Only ever one row (id = SINGLE_ROW_ID). observeCurrent emits null until the user saves a goal.
    @Query("SELECT * FROM budget_goal WHERE id = ${BudgetingGoal.SINGLE_ROW_ID} LIMIT 1")
    fun observeCurrent(): Flow<BudgetingGoal?>

    // REPLACE means "save" is really an upsert — insert first time, update thereafter.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: BudgetingGoal)
}
