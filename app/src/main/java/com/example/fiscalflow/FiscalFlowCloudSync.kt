package com.example.fiscalflow

import android.util.Base64
import com.example.fiscalflow.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object FiscalFlowCloudSync {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadLocalData(database: AppDatabase) {
        val uid = FirebaseIdentity.ensureSignedIn()
        val userRef = firestore.collection("users").document(uid)

        val categories = database.categoryDao().observeAll("unassigned_legacy").first()
        val expenses = database.expenseDao().observeAll("unassigned_legacy").first()
        val budget = database.budgetGoalDao().observeCurrent("unassigned_legacy").first()

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

    suspend fun deleteCloudExpense(expenseId: Long) {
        val uid = FirebaseIdentity.ensureSignedIn()

        firestore.collection("users")
            .document(uid)
            .collection("expenses")
            .document(expenseId.toString())
            .delete()
            .await()
    }

    suspend fun deleteCloudCategory(categoryName: String) {
        val uid = FirebaseIdentity.ensureSignedIn()

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
    }

    suspend fun hasCloudData(): Boolean {
        val uid = FirebaseIdentity.ensureSignedIn()
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

        return budget.exists()
    }

    suspend fun restoreCloudData(database: AppDatabase) {
        val uid = FirebaseIdentity.ensureSignedIn()
        val userRef = firestore.collection("users").document(uid)

        val hasBackup = hasCloudData()
        if (!hasBackup) return

        val categories = userRef.collection("categories").get().await()
        for (document in categories.documents) {
            val name = document.getString("name") ?: continue
            database.categoryDao().insert(
                CategoryEntity(name)
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
                    isIncome = isIncome
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

            if (minimum != null && maximum != null &&
                month != null && year != null
            ) {
                database.budgetGoalDao().upsert(
                    BudgetingGoal(
                        minimum = minimum,
                        maximum = maximum,
                        month = month.toInt(),
                        year = year.toInt()
                    )
                )
            }
        }
    }
}
