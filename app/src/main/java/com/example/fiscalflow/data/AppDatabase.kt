package com.example.fiscalflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        // Migration 2 -> 3 adds ownerUsername to expenses using neutral legacy default
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN ownerUsername TEXT NOT NULL DEFAULT 'unassigned_legacy'")
            }
        }

        // Migration 3 -> 4 adds account ownership to categories, budget_goal, and user_progress
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Categories: allow same category name for different users
                db.execSQL(
                    """
                    CREATE TABLE categories_new (
                        ownerUsername TEXT NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY(ownerUsername, name)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO categories_new (ownerUsername, name)
                    SELECT 'unassigned_legacy', name FROM categories
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE categories")
                db.execSQL("ALTER TABLE categories_new RENAME TO categories")

                // Budgets: give each user an independent budget row
                db.execSQL(
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

                db.execSQL(
                    """
                    INSERT INTO budget_goal_new (
                        minimum, maximum, month, year, id, ownerUsername
                    )
                    SELECT minimum, maximum, month, year, id, 'unassigned_legacy'
                    FROM budget_goal
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE budget_goal")
                db.execSQL("ALTER TABLE budget_goal_new RENAME TO budget_goal")

                // Progress: preserve XP, streak, and milestones for legacy records
                db.execSQL(
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

                db.execSQL(
                    """
                    INSERT INTO user_progress_new (
                        xp, streak, milestonesRaw, id, ownerUsername
                    )
                    SELECT xp, streak, milestonesRaw, id, 'unassigned_legacy'
                    FROM user_progress
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE user_progress")
                db.execSQL("ALTER TABLE user_progress_new RENAME TO user_progress")
            }
        }

        // Migration 4 -> 5 adds firebaseUid column to users table
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
            }
        }
    }
}
