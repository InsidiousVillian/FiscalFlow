package com.example.fiscalflow

import android.app.Application
import com.example.fiscalflow.data.AppDatabase
import com.example.fiscalflow.data.FiscalFlowRepository

/**
 * Custom Application subclass. Android instantiates it once per process, before any Activity,
 * which makes it the ideal place to build shared singletons like the Room database and repository.
 *
 * Registered in AndroidManifest.xml via android:name=".FiscalFlowApp".
 */
class FiscalFlowApp : Application() {

    // `by lazy` means the database is only opened the first time the repository is used.
    val database: AppDatabase by lazy { AppDatabase.get(this) }

    val repository: FiscalFlowRepository by lazy {
        FiscalFlowRepository(
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetGoalDao = database.budgetGoalDao(),
            userProgressDao = database.userProgressDao()
        )
    }
}
