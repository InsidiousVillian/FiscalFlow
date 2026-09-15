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

/**
 * Room database for FiscalFlow. This is the offline SQLite database required by the brief.
 *
 * `version = 1` — bump this and provide a Migration whenever an @Entity schema changes.
 * All persistent state (users, categories, expenses, goal, progress) lives here, so the
 * app works fully offline and survives process death / device reboots.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        Expense::class,
        BudgetingGoal::class,
        UsersProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        // Volatile so writes are visible across threads without needing extra locking on reads.
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Standard Room singleton. We only ever want one connection to the SQLite file per process,
         * otherwise Room throws warnings and we can end up with stale data between instances.
         *
         * The double-checked lock (synchronized + Volatile) is the canonical Android/Kotlin pattern.
         */
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiscalflow.db"
                )
                    // For a student prototype we allow destructive migration: bumping the DB version
                    // wipes local data instead of crashing. For production, write real Migrations.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
