package com.example.fiscalflow

import android.app.Application
import android.content.Context
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
            prefs = getSharedPreferences("fiscalflow_prefs", MODE_PRIVATE)
        )
    }

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncLocalDataToCloud() {
        if (!IS_CLOUD_SYNC_ENABLED) return
        FiscalFlowUploadWorker.schedule(this)
    }

    override fun onCreate() {
        super.onCreate()
    }

    suspend fun hasCloudBackup(): Boolean {
        if (!IS_CLOUD_SYNC_ENABLED) return false
        return FiscalFlowCloudSync.hasCloudData()
    }

    suspend fun hasLocalFinancialData(ownerUsername: String = ""): Boolean {
        val activeOwner = ownerUsername.ifBlank { repository.activeUsername.value.orEmpty() }.trim()
        val queryOwner = if (activeOwner.isBlank()) "unassigned_legacy" else activeOwner
        return (database.expenseDao().count(queryOwner) > 0) ||
                (database.categoryDao().count(queryOwner) > 0) ||
                database.budgetGoalDao().hasGoal(queryOwner)
    }

    suspend fun restoreCloudData() {
        if (!IS_CLOUD_SYNC_ENABLED) return
        FiscalFlowCloudSync.restoreCloudData(database)
    }

    companion object {
        const val IS_CLOUD_SYNC_ENABLED = false
    }
}
