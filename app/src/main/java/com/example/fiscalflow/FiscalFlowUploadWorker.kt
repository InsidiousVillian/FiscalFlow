package com.example.fiscalflow

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FiscalFlowUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return Result.success()
    }

    companion object {
        fun schedule(context: Context, ownerUsername: String = "") {
        }
    }
}
