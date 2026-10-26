package com.example.fiscalflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fiscalflow.BudgetingGoal
import com.example.fiscalflow.CategoryEntity
import com.example.fiscalflow.Expense
import com.example.fiscalflow.PendingSyncEntity
import com.example.fiscalflow.UserEntity
import com.example.fiscalflow.UsersProgress
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Room database initialization and singleton instance provider.
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        Expense::class,
        BudgetingGoal::class,
        UsersProgress::class,
        PendingSyncEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun pendingSyncDao(): PendingSyncDao

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

                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }


        // Migration adds account ownership without deleting financial records
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // Expenses: keep all existing values and assign them to Belinda
                database.execSQL(
                    """
                    CREATE TABLE expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount REAL NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        photoUri TEXT,
                        isIncome INTEGER NOT NULL,
                        ownerUsername TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO expenses_new (
                        id, amount, startDate, endDate, description,
                        category, photoUri, isIncome, ownerUsername
                    )
                    SELECT
                        id, amount, startDate, endDate, description,
                        category, photoUri, isIncome, 'Belinda'
                    FROM expenses
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE expenses")
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")

                // Categories: allow the same category name for different users.
                database.execSQL(
                    """
                    CREATE TABLE categories_new (
                        ownerUsername TEXT NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY(ownerUsername, name)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO categories_new (ownerUsername, name)
                    SELECT 'Belinda', name FROM categories
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE categories")
                database.execSQL("ALTER TABLE categories_new RENAME TO categories")

                // Budgets: give each user an independent budget row
                database.execSQL(
                    """
                    CREATE TABLE budget_goal_new (
                        minimum REAL NOT NULL,
                        maximum REAL NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        id INTEGER NOT NULL,
                        ownerUsername TEXT NOT NULL,
                        PRIMARY KEY(ownerUsername, id)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO budget_goal_new (
                        minimum, maximum, month, year, id, ownerUsername
                    )
                    SELECT minimum, maximum, month, year, id, 'Belinda'
                    FROM budget_goal
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE budget_goal")
                database.execSQL(
                    "ALTER TABLE budget_goal_new RENAME TO budget_goal"
                )

                // Progress: preserve Belinda's XP, streak, and milestones
                database.execSQL(
                    """
                    CREATE TABLE user_progress_new (
                        xp INTEGER NOT NULL,
                        streak INTEGER NOT NULL,
                        milestonesRaw TEXT NOT NULL,
                        id INTEGER NOT NULL,
                        ownerUsername TEXT NOT NULL,
                        PRIMARY KEY(ownerUsername, id)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO user_progress_new (
                        xp, streak, milestonesRaw, id, ownerUsername
                    )
                    SELECT xp, streak, milestonesRaw, id, 'Belinda'
                    FROM user_progress
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE user_progress")
                database.execSQL(
                    "ALTER TABLE user_progress_new RENAME TO user_progress"
                )

                // Rebuild the queue to match the current entity schema
                // Existing queued changes are assigned to Belinda
                // Any old createdAt column is intentionally omitted because
                // the current PendingSyncEntity does not declare that field
                database.execSQL("DROP TABLE IF EXISTS pending_sync_new")

                database.execSQL(
                    """
                    CREATE TABLE pending_sync_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        recordId TEXT NOT NULL,
                        ownerUsername TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO pending_sync_new (
                        id, entityType, operation, recordId, ownerUsername
                    )
                    SELECT id, entityType, operation, recordId, 'Belinda'
                    FROM pending_sync
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE pending_sync")
                database.execSQL(
                    "ALTER TABLE pending_sync_new RENAME TO pending_sync"
                )
            }



        }
        //Migration 4 -> 5 adds Firebase UID without deleting existing accounts.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN firebaseUid TEXT"
                )
            }
        }

    }
}