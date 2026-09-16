package com.example.fiscalflow

import android.app.Application
import com.example.fiscalflow.data.AppDatabase
import com.example.fiscalflow.data.FiscalFlowRepository

// App application subclass initializing singletons like Room DB and repository.
class FiscalFlowApp : Application() {

    // Lazy load the database instance on first access
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
