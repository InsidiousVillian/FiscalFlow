package com.example.fiscalflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress

// Room database initialization and singleton instance provider.
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        Expense::class,
        BudgetingGoal::class,
        UsersProgress::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Singleton instance provider for Room database connection
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiscalflow.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
