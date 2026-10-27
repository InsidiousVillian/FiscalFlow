package com.example.fiscalflow

import android.app.Application
import com.example.fiscalflow.data.FiscalFlowRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FiscalFlowApp : Application() {

    val repository: FiscalFlowRepository by lazy {
        FiscalFlowRepository(
            prefs = getSharedPreferences("fiscalflow_prefs", MODE_PRIVATE)
        )
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder().build()
            firestore.firestoreSettings = settings
        } catch (_: Exception) {}
    }

    companion object {
        const val IS_CLOUD_SYNC_ENABLED = true
    }
}
