package com.example.fiscalflow

import android.util.Base64
import android.util.Log
import com.example.fiscalflow.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object FiscalFlowCloudSync {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private suspend fun requireVerifiedUid(
        database: AppDatabase,
        ownerUsername: String
    ): String {
        val username = ownerUsername.trim()

        require(username.isNotBlank()) {
            "A local account is required for cloud sync."
        }

        val firebaseUid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("No authenticated Firebase account is signed in.")

        val localUser = database.userDao().findByUsername(username)
            ?: throw IllegalStateException("Local account not found for $username.")

        check(
            !localUser.firebaseUid.isNullOrBlank() &&
                    localUser.firebaseUid == firebaseUid
        ) {
            "Firebase identity does not match the local account. Cloud sync blocked."
        }

        return firebaseUid
    }

    suspend fun uploadExpense(database: AppDatabase, ownerUsername: String, expense: Expense): Boolean {
        return try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val docRef = firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .document(expense.id.toString())

            val expenseData = mapOf(
                "id" to expense.id,
                "amount" to expense.amount,
                "startDate" to expense.startDate,
                "endDate" to expense.endDate,
                "description" to expense.description,
                "category" to expense.category,
                "photoUri" to expense.photoUri,
                "isIncome" to expense.isIncome,
                "ownerUsername" to expense.ownerUsername
            )
            docRef.set(expenseData).await()
            Log.i("FiscalFlowCloudSync", "SUCCESS: Firestore write for expense ID ${expense.id} at users/$uid/expenses/${expense.id}")
            true
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud upload deferred for expense ID ${expense.id}: ${e.message}")
            false
        }
    }

    suspend fun deleteCloudExpense(database: AppDatabase, ownerUsername: String, expenseId: Long) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .document(expenseId.toString())
                .delete()
                .await()
            Log.i("FiscalFlowCloudSync", "SUCCESS: Firestore delete for expense ID $expenseId at users/$uid/expenses/$expenseId")
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud delete deferred for expense ID $expenseId: ${e.message}")
        }
    }

    suspend fun uploadCategory(database: AppDatabase, ownerUsername: String, categoryName: String) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val categoryId = Base64.encodeToString(
                categoryName.toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            firestore.collection("users").document(uid)
                .collection("categories").document(categoryId)
                .set(mapOf("name" to categoryName, "ownerUsername" to ownerUsername))
                .await()
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud category upload deferred for $categoryName: ${e.message}")
        }
    }

    suspend fun deleteCloudCategory(database: AppDatabase, ownerUsername: String, categoryName: String) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val categoryId = Base64.encodeToString(
                categoryName.toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            val userRef = firestore.collection("users").document(uid)
            userRef.collection("categories")
                .document(categoryId)
                .delete()
                .await()

            val matchingExpenses = userRef.collection("expenses")
                .whereEqualTo("category", categoryName)
                .get()
                .await()

            for (expense in matchingExpenses.documents) {
                expense.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud category delete deferred for $categoryName: ${e.message}")
        }
    }

    suspend fun uploadBudget(database: AppDatabase, ownerUsername: String, budget: BudgetingGoal) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            firestore.collection("users").document(uid)
                .collection("budget").document("current")
                .set(
                    mapOf(
                        "minimum" to budget.minimum,
                        "maximum" to budget.maximum,
                        "month" to budget.month,
                        "year" to budget.year,
                        "ownerUsername" to ownerUsername
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud budget upload deferred: ${e.message}")
        }
    }

    suspend fun uploadProgress(database: AppDatabase, ownerUsername: String, progress: UsersProgress) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            firestore.collection("users").document(uid)
                .collection("progress").document("current")
                .set(
                    mapOf(
                        "xp" to progress.xp,
                        "streak" to progress.streak,
                        "milestonesRaw" to progress.milestonesRaw,
                        "ownerUsername" to ownerUsername
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud progress upload deferred: ${e.message}")
        }
    }

    suspend fun uploadLocalData(database: AppDatabase, ownerUsername: String) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val userRef = firestore.collection("users").document(uid)

            val categories = database.categoryDao().observeAll(ownerUsername).first()
            val expenses = database.expenseDao().observeAll(ownerUsername).first()
            val budget = database.budgetGoalDao().observeCurrent(ownerUsername).first()
            val progress = database.userProgressDao().getCurrent(ownerUsername)

            for (category in categories) {
                uploadCategory(database, ownerUsername, category.name)
            }

            for (expense in expenses) {
                uploadExpense(database, ownerUsername, expense)
            }

            if (budget != null) {
                uploadBudget(database, ownerUsername, budget)
            }

            if (progress != null) {
                uploadProgress(database, ownerUsername, progress)
            }
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud upload local data deferred for $ownerUsername: ${e.message}")
        }
    }

    suspend fun hasCloudDataForAccount(
        database: AppDatabase,
        ownerUsername: String
    ): Boolean {
        return try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val userRef = firestore.collection("users").document(uid)

            val categories = userRef.collection("categories").limit(1).get().await()
            if (!categories.isEmpty) return true

            val expenses = userRef.collection("expenses").limit(1).get().await()
            if (!expenses.isEmpty) return true

            val budget = userRef.collection("budget").document("current").get().await()
            if (budget.exists()) return true

            val progress = userRef.collection("progress").document("current").get().await()
            progress.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreCloudData(database: AppDatabase, ownerUsername: String) {
        try {
            val uid = requireVerifiedUid(database, ownerUsername)
            val userRef = firestore.collection("users").document(uid)

            val categoriesSnapshot = userRef.collection("categories").get().await()
            for (document in categoriesSnapshot.documents) {
                val name = document.getString("name") ?: continue
                database.categoryDao().insert(
                    CategoryEntity(name = name, ownerUsername = ownerUsername)
                )
            }

            val expensesSnapshot = userRef.collection("expenses").get().await()
            for (document in expensesSnapshot.documents) {
                val id = document.getLong("id") ?: continue
                val amount = document.getDouble("amount") ?: continue
                val startDate = document.getString("startDate") ?: continue
                val endDate = document.getString("endDate") ?: continue
                val description = document.getString("description") ?: ""
                val category = document.getString("category") ?: continue
                val photoUri = document.getString("photoUri")
                val isIncome = document.getBoolean("isIncome") ?: false

                database.expenseDao().restoreExpense(
                    Expense(
                        id = id,
                        amount = amount,
                        startDate = startDate,
                        endDate = endDate,
                        description = description,
                        category = category,
                        photoUri = photoUri,
                        isIncome = isIncome,
                        ownerUsername = ownerUsername
                    )
                )
            }

            val budgetDocument = userRef.collection("budget").document("current").get().await()
            if (budgetDocument.exists()) {
                val minimum = budgetDocument.getDouble("minimum")
                val maximum = budgetDocument.getDouble("maximum")
                val month = budgetDocument.getLong("month")
                val year = budgetDocument.getLong("year")

                if (minimum != null && maximum != null && month != null && year != null) {
                    database.budgetGoalDao().upsert(
                        BudgetingGoal(
                            minimum = minimum,
                            maximum = maximum,
                            month = month.toInt(),
                            year = year.toInt(),
                            ownerUsername = ownerUsername
                        )
                    )
                }
            }

            val progressDocument = userRef.collection("progress").document("current").get().await()
            if (progressDocument.exists()) {
                val xp = progressDocument.getLong("xp")?.toInt() ?: 0
                val streak = progressDocument.getLong("streak")?.toInt() ?: 0
                val milestonesRaw = progressDocument.getString("milestonesRaw") ?: ""

                database.userProgressDao().upsert(
                    UsersProgress(
                        xp = xp,
                        streak = streak,
                        milestonesRaw = milestonesRaw,
                        ownerUsername = ownerUsername
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("FiscalFlowCloudSync", "Cloud data restore deferred for $ownerUsername: ${e.message}")
        }
    }
}
