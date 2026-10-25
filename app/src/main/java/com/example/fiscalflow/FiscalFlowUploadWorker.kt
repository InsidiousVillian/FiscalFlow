
package com.example.fiscalflow

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class FiscalFlowUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as FiscalFlowApp
            FiscalFlowCloudSync.uploadLocalData(app.database)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<FiscalFlowUploadWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "fiscal_flow_cloud_upload",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
