package com.example.fiscalflow

import android.app.Application
import androidx.work.WorkManager
import com.example.fiscalflow.data.AppDatabase
import com.example.fiscalflow.data.FiscalFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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

    fun syncLocalDataToCloud(ownerUsername: String) {
        if (!IS_CLOUD_SYNC_ENABLED) return
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
        if (!IS_CLOUD_SYNC_ENABLED) return false
        return FiscalFlowCloudSync.hasCloudDataForAccount(
            database,
            ownerUsername
        )
    }

    suspend fun hasLocalFinancialData(ownerUsername: String): Boolean {
        val username = ownerUsername.trim()
        if (username.isBlank()) return false
        return (database.expenseDao().count(username) > 0) ||
                (database.categoryDao().count(username) > 0) ||
                database.budgetGoalDao().hasGoal(username)
    }

    suspend fun restoreCloudData(ownerUsername: String) {
        if (!IS_CLOUD_SYNC_ENABLED) return
        FiscalFlowCloudSync.restoreCloudData(database, ownerUsername)
    }

    companion object {
        // Explicit, default-disabled feature gate for cloud sync operations
        const val IS_CLOUD_SYNC_ENABLED = false
    }
}
