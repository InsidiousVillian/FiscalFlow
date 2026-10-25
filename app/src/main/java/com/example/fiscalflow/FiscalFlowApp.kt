
package com.example.fiscalflow

import android.app.Application
//import android.util.Log
import com.example.fiscalflow.data.AppDatabase
import com.example.fiscalflow.data.FiscalFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch

class FiscalFlowApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.get(this)
    }

    val repository: FiscalFlowRepository by lazy {
        FiscalFlowRepository(
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetGoalDao = database.budgetGoalDao(),
            userProgressDao = database.userProgressDao()
        )
    }

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /*fun syncLocalDataToCloud() {
        syncScope.launch {
            try {
                FiscalFlowCloudSync.uploadLocalData(database)
                Log.d("FiscalFlowCloudSync", "Upload completed")
            } catch (e: Exception) {
                Log.w("FiscalFlowCloudSync", "Upload failed; local data remains available", e)
            }
        }
    }*/
    fun syncLocalDataToCloud() {
        FiscalFlowUploadWorker.schedule(this)
    }
}
