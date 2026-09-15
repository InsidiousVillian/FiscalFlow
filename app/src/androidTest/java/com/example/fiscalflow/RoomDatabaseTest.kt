package com.example.fiscalflow

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fiscalflow.data.AppDatabase
import com.example.fiscalflow.data.BudgetGoalDao
import com.example.fiscalflow.data.CategoryDao
import com.example.fiscalflow.data.ExpenseDao
import com.example.fiscalflow.data.UserDao
import com.example.fiscalflow.data.UserProgressDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests that exercise the real Room-generated DAO implementations
 * against an in-memory SQLite instance on the connected device/emulator.
 *
 * Why in-memory?
 *  - Zero file cleanup between tests (DB dies with the JVM the test runner spawns).
 *  - `allowMainThreadQueries()` means we don't need a coroutines dispatcher rule —
 *    suspend DAO calls just block the test thread, which is exactly what we want.
 *
 * At the very end we ALSO open the real on-disk `AppDatabase.get(context)` and confirm
 * the file `fiscalflow.db` is materialised in the app's databases directory, proving
 * the production Room wiring actually creates a database on the device.
 */
@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    // The four DAOs plus the DB reference we tear down after each test.
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var budgetGoalDao: BudgetGoalDao
    private lateinit var userProgressDao: UserProgressDao

    @Before
    fun setUp() {
        // ApplicationProvider gives us the instrumentation context that Room needs.
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Tests are single-threaded blocking code; letting them run on the main thread keeps them simple.
            .allowMainThreadQueries()
            .build()

        userDao = db.userDao()
        categoryDao = db.categoryDao()
        expenseDao = db.expenseDao()
        budgetGoalDao = db.budgetGoalDao()
        userProgressDao = db.userProgressDao()
    }

    @After
    fun tearDown() {
        // Always close the DB so the next test starts from a blank slate.
        db.close()
    }

    // Verifies that inserting a user and looking it back up by username returns the same row.
    // This exercises UserDao.insert (suspend) + UserDao.findByUsername (suspend @Query).
    @Test
    fun userDao_insertAndFindByUsername_returnsInsertedUser() = runBlocking {
        val user = UserEntity(username = "alice", password = "secret")
        userDao.insert(user)

        val loaded = userDao.findByUsername("alice")
        assertNotNull("expected to find the user we just inserted", loaded)
        assertEquals("alice", loaded!!.username)
        assertEquals("secret", loaded.password)

        // Sanity check: an unknown username must return null so authenticate() can reject it.
        assertNull(userDao.findByUsername("does-not-exist"))
    }

    // Verifies that CategoryEntity's PRIMARY KEY on `name` prevents duplicates when
    // combined with OnConflictStrategy.IGNORE. Inserting the same name twice must leave
    // exactly one row in the table (rowId == -1L on the second insert).
    @Test
    fun categoryDao_insertDuplicate_isIgnoredByPrimaryKey() = runBlocking {
        val firstRowId = categoryDao.insert(CategoryEntity("Groceries"))
        val secondRowId = categoryDao.insert(CategoryEntity("Groceries"))

        assertTrue("first insert should succeed with a positive rowId", firstRowId > 0)
        assertEquals("duplicate insert should be ignored (rowId == -1L)", -1L, secondRowId)
        assertEquals(1, categoryDao.count())
    }

    // Verifies CategoryDao.observeAll() Flow emits categories sorted by name (case-insensitive).
    // We insert deliberately out of order to catch a broken ORDER BY.
    @Test
    fun categoryDao_observeAll_emitsSortedCategories() = runBlocking {
        categoryDao.insert(CategoryEntity("utilities"))
        categoryDao.insert(CategoryEntity("Groceries"))
        categoryDao.insert(CategoryEntity("Rent"))

        val emitted = categoryDao.observeAll().first().map { it.name }
        assertEquals(listOf("Groceries", "Rent", "utilities"), emitted)
    }

    // Verifies ExpenseDao autoGenerate primary key + observeAll Flow surfaces the inserted row.
    @Test
    fun expenseDao_insertAndObserveAll_returnsExpense() = runBlocking {
        val expense = Expense(
            amount = 12.50,
            startDate = "5/9/2026",
            endDate = "5/9/2026",
            description = "coffee",
            category = "Groceries",
            photoUri = null
        )
        val newId = expenseDao.insert(expense)
        assertTrue("autoGenerated id should be positive", newId > 0)

        val all = expenseDao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals(12.50, all[0].amount, 0.0001)
        assertEquals("coffee", all[0].description)
    }

    // Verifies that BudgetGoalDao.upsert() (OnConflictStrategy.REPLACE) always keeps a single row.
    // The BudgetingGoal entity uses a fixed SINGLE_ROW_ID so two saves must overwrite each other.
    @Test
    fun budgetGoalDao_upsertReplacesPreviousRow() = runBlocking {
        budgetGoalDao.upsert(BudgetingGoal(minimum = 100.0, maximum = 500.0, month = 9, year = 2026))
        budgetGoalDao.upsert(BudgetingGoal(minimum = 200.0, maximum = 800.0, month = 10, year = 2026))

        val current = budgetGoalDao.observeCurrent().first()
        assertNotNull(current)
        // Only the latest write should survive.
        assertEquals(200.0, current!!.minimum, 0.0001)
        assertEquals(800.0, current.maximum, 0.0001)
        assertEquals(10, current.month)
    }

    // Verifies that UsersProgress round-trips through Room and the milestonesRaw/milestones
    // string split/join helpers work as expected. Room only stores primitive columns, so
    // this doubles as a regression test for the manual serialization strategy.
    @Test
    fun userProgressDao_upsertPersistsMilestonesString() = runBlocking {
        val progress = UsersProgress.fromList(
            xp = 42,
            streak = 3,
            milestones = listOf("first_expense", "first_goal", "week_streak")
        )
        userProgressDao.upsert(progress)

        val loaded = userProgressDao.observeCurrent().first()
        assertNotNull(loaded)
        assertEquals(42, loaded!!.xp)
        assertEquals(3, loaded.streak)
        // Raw string uses "|" separator; the derived `milestones` list rebuilds the original list.
        assertEquals("first_expense|first_goal|week_streak", loaded.milestonesRaw)
        assertEquals(
            listOf("first_expense", "first_goal", "week_streak"),
            loaded.milestones
        )
    }

    // Bonus check: open the REAL (non-in-memory) production database via AppDatabase.get()
    // and confirm the SQLite file `fiscalflow.db` is materialised in the app's databases dir.
    // This proves the production Room wiring actually creates a DB on the device — the whole
    // point of the offline persistence requirement.
    @Test
    fun productionAppDatabase_createsFiscalFlowDbFile() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val real = AppDatabase.get(context)
        // Touching any DAO forces Room to actually open (and therefore create) the SQLite file.
        real.userDao().count()

        val dbFile = context.getDatabasePath("fiscalflow.db")
        assertTrue(
            "expected fiscalflow.db to exist at ${dbFile.absolutePath}",
            dbFile.exists()
        )
    }
}
