# FiscalFlow — Budget Tracker

![Android CI](https://github.com/InsidiousVillian/FiscalFlow/actions/workflows/android.yml/badge.svg?branch=harrietaleck)

FiscalFlow is a personal budget-tracking Android app that helps users log
expenses against custom categories, attach photos to receipts, set monthly
spending goals (minimum / maximum), and review period-based totals per
category. Everything works offline — the app persists all data locally
with Room so users can add and review expenses without a network
connection.

> **Demo video:** _<add YouTube unlisted link here>_

## Features

- **Account & login** — local user accounts with sign-in / sign-up.
- **Custom categories** — users create their own expense categories.
- **Expenses with photos** — each expense captures amount, date, start
  and end times, description, category and an optional photo of the
  receipt or item.
- **Monthly goals** — set a minimum and maximum monthly spend target and
  track progress against it.
- **Period-based totals** — pick a start and end date to see totals per
  category over that range.
- **Offline persistence** — all data lives in a local Room database, so
  the app is fully usable without connectivity.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Persistence:** Room 2.6.1 with KSP for code generation
- **Images:** Coil for photo loading
- **Architecture:** MVVM — Repository → ViewModel → `StateFlow` → Compose

## Build & run

1. Install [Android Studio](https://developer.android.com/studio)
   (Hedgehog or newer) with the Android 34 SDK.
2. Clone the repo and open the project root in Android Studio.
3. Let Gradle sync — the wrapper will fetch AGP 8.x and JDK 17.
4. Create or start an emulator (API 26+) or plug in a physical device
   with USB debugging enabled.
5. Press **Run ▶** (or `./gradlew installDebug`) to deploy the app.

Command-line build:

```bash
./gradlew assembleDebug          # build debug APK
./gradlew testDebugUnitTest      # run JVM unit tests
```

The debug APK is emitted to `app/build/outputs/apk/debug/app-debug.apk`
and is also published as an artifact on every CI run (see badge above).

## Data persistence (Room)

FiscalFlow's offline database is built on
[Room 2.6.1](https://developer.android.com/training/data-storage/room).
Room is a compile-time SQL abstraction over SQLite: entity classes
describe tables, DAO interfaces describe queries, and a single
`RoomDatabase` subclass ties them together. All expense, category,
goal, progress and user data is stored on-device so the app works
without a network.

### Entities

Each entity is a Kotlin `data class` annotated with `@Entity`, and
becomes a table in the SQLite database.

- **`UserEntity`** — a registered user (username, hashed password,
  primary key). Backs the login / sign-up flow.
- **`CategoryEntity`** — a spending category (name, colour, owning
  user). Users add their own categories from the Categories screen.
- **`Expense`** — a single spending record: amount, date, start &
  end time, description, optional photo URI, and a foreign key to
  the owning category.
- **`BudgetingGoal`** — the user's minimum and maximum monthly spend
  target for a given month.
- **`UsersProgress`** — aggregated progress toward the current goal
  (running totals updated as expenses are added).

### DAOs

DAOs (Data Access Objects) expose the queries used by the app.
Each returns a `Flow` where the UI needs to react to changes, or a
`suspend` function for one-shot inserts / updates.

- **`UserDao`** — `insert`, `getUserByUsername`, credential lookup for
  login.
- **`CategoryDao`** — `insertCategory`, `getCategoriesForUser`,
  `deleteCategory`. Emits a `Flow<List<CategoryEntity>>` so the
  category list refreshes automatically.
- **`ExpenseDao`** — `insertExpense`, `getExpensesForUser`,
  `getExpensesBetween(start, end)` (used for period totals) and
  `getTotalsByCategory(start, end)` for the summary screen.
- **`BudgetingGoalDao`** — read/write the current month's min & max.
- **`UsersProgressDao`** — updates rolling progress totals.

### `AppDatabase` singleton

`AppDatabase` is a `RoomDatabase` subclass that lists every entity
and exposes each DAO via an abstract getter. It's created through
the standard double-checked singleton pattern so the entire process
shares one connection:

```kotlin
@Database(
    entities = [
        UserEntity::class, CategoryEntity::class, Expense::class,
        BudgetingGoal::class, UsersProgress::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetingGoalDao(): BudgetingGoalDao
    abstract fun usersProgressDao(): UsersProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiscalflow.db",
                ).build().also { INSTANCE = it }
            }
    }
}
```

### Reactive UI flow

The app follows a one-way data flow:

```
Compose UI  ⇄  ViewModel  ⇄  Repository  ⇄  DAO  ⇄  Room / SQLite
              (StateFlow)     (suspend /
                              Flow APIs)
```

1. **DAO** methods return `Flow<T>` for observable reads or `suspend`
   for writes.
2. **Repository** classes wrap the DAO calls and expose a clean
   domain-level API to the rest of the app (e.g.
   `ExpenseRepository.getExpensesBetween(start, end)`).
3. **ViewModel** collects the repository flows into `StateFlow`s
   using `stateIn(viewModelScope, ...)`. UI state (loading, empty,
   error, data) is modelled as a sealed class.
4. **Compose** screens observe the `StateFlow` via
   `collectAsStateWithLifecycle()` and recompose automatically when
   the database changes — no manual refresh needed.

### Inspecting the database

To browse the on-device database while debugging:

1. Run the app on an emulator or connected device from Android Studio.
2. Open **View → Tool Windows → App Inspection**.
3. Select the running `com.example.fiscalflow` process, then choose the
   **Database Inspector** tab.
4. Pick `fiscalflow.db` — you can now browse every table, run ad-hoc
   SQL queries, and enable **Live updates** to watch rows change in
   real time as you use the app.

## Continuous integration

Every push and pull request runs the [Android CI](.github/workflows/android.yml)
workflow, which builds the debug APK and runs unit tests on an Ubuntu
runner with JDK 17 (Temurin). The APK is uploaded as a workflow
artifact so reviewers can install it without building locally.
