package com.example.fiscalflow

import android.util.Base64
import com.example.fiscalflow.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object FiscalFlowCloudSync {

    private val firestore = FirebaseFirestore.getInstance()

    // Reject cloud access unless the signed-in Firebase UID matches the UID saved for the requested local account
    private suspend fun requireVerifiedUid(
        database: AppDatabase,
        ownerUsername: String
    ): String {
        val username = ownerUsername.trim()

        require(username.isNotBlank()) {
            "A local account is required for cloud sync."
        }

        val firebaseUid = FirebaseIdentity.requireCurrentUid()
        val localUser = database.userDao().findByUsername(username)
            ?: throw IllegalStateException("Local account not found.")

        check(
            !localUser.firebaseUid.isNullOrBlank() &&
                    localUser.firebaseUid == firebaseUid
        ) {
            "Firebase identity does not match the local account. Cloud sync blocked."
        }

        return firebaseUid
    }

    suspend fun uploadLocalData(database: AppDatabase, ownerUsername: String){

        val uid = requireVerifiedUid(database, ownerUsername)
        val userRef = firestore.collection("users").document(uid)

        val categories = database.categoryDao().observeAll(ownerUsername).first()
        val expenses = database.expenseDao().observeAll(ownerUsername).first()
        val budget = database.budgetGoalDao().observeCurrent(ownerUsername).first()

        for (category in categories) {
            val categoryId = Base64.encodeToString(
                category.name.toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            userRef.collection("categories")
                .document(categoryId)
                .set(mapOf("name" to category.name))
                .await()
        }

        for (expense in expenses) {
            userRef.collection("expenses")
                .document(expense.id.toString())
                .set(
                    mapOf(
                        "id" to expense.id,
                        "amount" to expense.amount,
                        "startDate" to expense.startDate,
                        "endDate" to expense.endDate,
                        "description" to expense.description,
                        "category" to expense.category,
                        "photoUri" to expense.photoUri,
                        "isIncome" to expense.isIncome
                    )
                )
                .await()
        }

        if (budget != null) {
            userRef.collection("budget")
                .document("current")
                .set(
                    mapOf(
                        "minimum" to budget.minimum,
                        "maximum" to budget.maximum,
                        "month" to budget.month,
                        "year" to budget.year
                    )
                )
                .await()
        }
    }

    suspend fun deleteCloudExpense(expenseId: Long, uid: String) {

        firestore.collection("users")
            .document(uid)
            .collection("expenses")
            .document(expenseId.toString())
            .delete()
            .await()
    }

    //Processes queued local changes in order
    suspend fun processPendingChanges(
        database: AppDatabase,
        ownerUsername: String
    ) {
        // Verify once and keep the UID for all queued operations
        val uid = requireVerifiedUid(database, ownerUsername)
        // Keep queued changes belonging to other accounts untouched
        val pendingChanges = database.pendingSyncDao()
            .getPendingChanges()
            .filter { it.ownerUsername == ownerUsername.trim() }
        for (change in pendingChanges) {
            // Read the latest local state when processing an upsert
            // Keep unsupported operations in the queue for investigation.
            val supportedOperation = when (change.entityType) {
                "expense", "category" ->
                    change.operation == "upsert" || change.operation == "delete"

                "budget", "progress" ->
                    change.operation == "upsert"

                else -> false
            }

            if (!supportedOperation) {
                // Do not remove an unsupported queue entry.
                continue
            }
            // A deleted local record must not be uploaded again
            when (change.entityType) {
                "expense" -> {
                    val expenseId = change.recordId.toLongOrNull()
                        ?: continue

                    if (change.operation == "delete") {
                        deleteCloudExpense(expenseId, uid)
                    } else{
                        val expense = database.expenseDao().getById(change.ownerUsername, expenseId)

                        if (expense == null) {
                            // The local expense no longer exists, so do not attempt to upload it again
                            deleteCloudExpense(expenseId, uid)
                        } else {
                            // Upload only while the expense still exists locally
                            uploadExpense(expense, uid)
                        }
                    }
                }

                "category" -> {
                    if (change.operation == "delete") {
                        deleteCloudCategory(change.recordId, uid)
                    } else if (change.operation == "upsert") {
                        val category = database.categoryDao().getByName(change.ownerUsername, change.recordId)

                        if (category == null) {
                            deleteCloudCategory(change.recordId, uid)
                        } else {
                            uploadCategory(category.name, uid)
                        }
                    }
                }

                "budget" -> {
                    if (change.operation == "upsert") {
                        val budget = database.budgetGoalDao().observeCurrent(change.ownerUsername).first()
                        if (budget != null) {
                            uploadBudget(budget, uid)
                        }
                    }
                }

                "progress" -> {
                    if (change.operation == "upsert") {
                        val progress = database.userProgressDao().getCurrent(change.ownerUsername)
                        if (progress != null) {
                            uploadProgress(progress, uid)
                        }
                    }
                }

                else -> {
                    // Unknown queue entries are retained for investigation.
                    continue
                }
            }

            // Reached only if the corresponding cloud operation succeeded.
            database.pendingSyncDao().removeChange(change.id)
        }
    }

    private suspend fun uploadCategory(categoryName: String, uid: String) {
        val categoryId = Base64.encodeToString(
            categoryName.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        firestore.collection("users").document(uid)
            .collection("categories").document(categoryId)
            .set(mapOf("name" to categoryName))
            .await()
    }

    private suspend fun uploadExpense(expense: Expense, uid: String) {
        firestore.collection("users").document(uid)
            .collection("expenses").document(expense.id.toString())
            .set(
                mapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "startDate" to expense.startDate,
                    "endDate" to expense.endDate,
                    "description" to expense.description,
                    "category" to expense.category,
                    "photoUri" to expense.photoUri,
                    "isIncome" to expense.isIncome
                )
            )
            .await()
    }

    private suspend fun uploadBudget(budget: BudgetingGoal, uid: String) {

        firestore.collection("users").document(uid)
            .collection("budget").document("current")
            .set(
                mapOf(
                    "minimum" to budget.minimum,
                    "maximum" to budget.maximum,
                    "month" to budget.month,
                    "year" to budget.year
                )
            )
            .await()
    }

    private suspend fun uploadProgress(progress: UsersProgress, uid: String) {

        firestore.collection("users").document(uid)
            .collection("progress").document("current")
            .set(
                mapOf(
                    "xp" to progress.xp,
                    "streak" to progress.streak,
                    "milestonesRaw" to progress.milestonesRaw
                )
            )
            .await()
    }

    suspend fun deleteCloudCategory(categoryName: String, uid: String) {
        val categoryId = Base64.encodeToString(
            categoryName.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        val userRef = firestore.collection("users").document(uid)
        userRef.collection("categories")
            .document(categoryId)
            .delete()
            .await()
    }

    suspend fun hasCloudData(uid: String): Boolean {
        val userRef = firestore.collection("users").document(uid)

        val categories = userRef.collection("categories")
            .limit(1)
            .get()
            .await()

        if (!categories.isEmpty) return true

        val expenses = userRef.collection("expenses")
            .limit(1)
            .get()
            .await()

        if (!expenses.isEmpty) return true

        val budget = userRef.collection("budget")
            .document("current")
            .get()
            .await()

        if (budget.exists()) return true

        // Check whether a cloud progress backup exists.
        val progress = userRef.collection("progress")
            .document("current")
            .get()
            .await()

        return progress.exists()
    }
    // Verify the local account before checking its cloud backup.
    suspend fun hasCloudDataForAccount(
        database: AppDatabase,
        ownerUsername: String
    ): Boolean {
        val uid = requireVerifiedUid(database, ownerUsername)
        return hasCloudData(uid)
    }

    suspend fun restoreCloudData(database: AppDatabase, ownerUsername: String) {
        val uid = requireVerifiedUid(database, ownerUsername)

        // Stop restore if this account already has local data
        val localExpenseCount = database.expenseDao().count(ownerUsername)
        val localCategoryCount = database.categoryDao().count(ownerUsername)
        val hasLocalBudget = database.budgetGoalDao().hasGoal(ownerUsername)
        val hasLocalProgress = database.userProgressDao().getCurrent(ownerUsername) != null

        // Check for unsent changes belonging to this account
        val pendingChanges = database.pendingSyncDao().countForOwner(ownerUsername)

        // Prevent cloud data from overwriting existing account data
        if (
            localExpenseCount > 0 ||
            localCategoryCount > 0 ||
            hasLocalBudget ||
            hasLocalProgress ||
            pendingChanges > 0
        ) {
            return
        }

        // Declare the cloud account reference only once
        val userRef = firestore.collection("users").document(uid)

        val hasBackup = hasCloudData(uid)
        if (!hasBackup) return

        val categories = userRef.collection("categories").get().await()
        for (document in categories.documents) {
            val name = document.getString("name") ?: continue

            database.categoryDao().insert(
                CategoryEntity(
                    name = name,
                    ownerUsername = ownerUsername
                )
            )
        }

        val expenses = userRef.collection("expenses").get().await()
        for (document in expenses.documents) {
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

        val budgetDocument = userRef.collection("budget")
            .document("current")
            .get()
            .await()

        if (budgetDocument.exists()) {
            val minimum = budgetDocument.getDouble("minimum")
            val maximum = budgetDocument.getDouble("maximum")
            val month = budgetDocument.getLong("month")
            val year = budgetDocument.getLong("year")

            if (
                minimum != null &&
                maximum != null &&
                month != null &&
                year != null
            ) {
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

        // Restore XP, streak and milestones from the cloud backup.
        val progressDocument = userRef.collection("progress")
            .document("current")
            .get()
            .await()

        if (progressDocument.exists()) {
            val xp = progressDocument.getLong("xp")?.toInt() ?: 0
            val streak = progressDocument.getLong("streak")?.toInt() ?: 0
            val milestonesRaw =
                progressDocument.getString("milestonesRaw") ?: ""

            database.userProgressDao().upsert(
                UsersProgress(
                    xp = xp,
                    streak = streak,
                    milestonesRaw = milestonesRaw,
                    ownerUsername = ownerUsername
                )
            )
        }
    }
    }
