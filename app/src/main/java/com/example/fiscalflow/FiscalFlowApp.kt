
package com.example.fiscalflow

import android.app.Application
//import android.util.Log
import androidx.work.WorkManager
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
            userProgressDao = database.userProgressDao(),
            pendingSyncDao = database.pendingSyncDao()
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
    fun syncLocalDataToCloud(ownerUsername: String) {
        FiscalFlowUploadWorker.schedule(this, ownerUsername)
    }

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this)
            .cancelUniqueWork("fiscal_flow_periodic_cloud_sync")

        WorkManager.getInstance(this)
            .cancelUniqueWork("fiscal_flow_cloud_upload")
    }
    suspend fun hasCloudBackup(ownerUsername: String): Boolean {
        return FiscalFlowCloudSync.hasCloudDataForAccount(
            database,
            ownerUsername
        )
    }

    suspend fun hasLocalFinancialData(ownerUsername: String = "Belinda"): Boolean {
        return (database.expenseDao().count(ownerUsername) > 0) ||
                (database.categoryDao().count(ownerUsername) > 0) ||
                database.budgetGoalDao().hasGoal(ownerUsername)
    }
    suspend fun restoreCloudData(ownerUsername: String) {
        FiscalFlowCloudSync.restoreCloudData(database, ownerUsername)
    }
}
