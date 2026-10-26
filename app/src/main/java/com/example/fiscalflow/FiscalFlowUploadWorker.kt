
package com.example.fiscalflow

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

class FiscalFlowUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!FiscalFlowApp.IS_CLOUD_SYNC_ENABLED) {
            return Result.success()
        }

        return try {
            val app = applicationContext as FiscalFlowApp

            // Read the local account supplied when the work was scheduled
            val ownerUsername = inputData.getString("ownerUsername")
                ?: return Result.failure()

            // Process only after the sync function verifies account ownership
            FiscalFlowCloudSync.processPendingChanges(app.database, ownerUsername)

            Result.success()
        } catch (e: IllegalStateException) {
            // Permanent failure: unlinked user, account missing, or Firebase UID mismatch
            Result.failure()
        } catch (e: IllegalArgumentException) {
            // Permanent failure: blank username or invalid parameters
            Result.failure()
        } catch (e: SecurityException) {
            // Permanent failure: security or permission issue
            Result.failure()
        } catch (e: FirebaseAuthException) {
            // Permanent failure: authentication credentials invalid or expired
            Result.failure()
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.UNAUTHENTICATED,
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> Result.failure()
                else -> Result.retry()
            }
        } catch (e: Exception) {
            // Transient network / I/O error: retry later
            Result.retry()
        }
    }

    companion object {
        // Require the active local username when scheduling
        fun schedule(context: Context, ownerUsername: String) {
            if (!FiscalFlowApp.IS_CLOUD_SYNC_ENABLED) return

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
