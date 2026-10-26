
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

            //Read the local account supplied when the work was scheduled
            val ownerUsername = inputData.getString("ownerUsername")
                ?: return Result.failure()

            // Process only after the sync function verifies account ownership
            FiscalFlowCloudSync.processPendingChanges(app.database, ownerUsername)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        // Require the active local username when scheduling
        fun schedule(context: Context, ownerUsername: String) {
            val username = ownerUsername.trim()

            // Never queue work without an account
            if (username.isBlank()) return

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<FiscalFlowUploadWorker>()
                // Pass the account to the background worker
                .setInputData(
                    androidx.work.workDataOf("ownerUsername" to username)
                )
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
