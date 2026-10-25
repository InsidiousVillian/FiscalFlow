
package com.example.fiscalflow

import com.example.fiscalflow.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first

object FiscalFlowCloudSync {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadLocalData(database: AppDatabase) {
        val uid = FirebaseIdentity.ensureSignedIn()
        val userRef = firestore.collection("users").document(uid)

        val categories = database.categoryDao().observeAll().first()
        val expenses = database.expenseDao().observeAll().first()
        val budget = database.budgetGoalDao().observeCurrent().first()

        for (category in categories) {
            val categoryId = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(category.name.toByteArray(Charsets.UTF_8))

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
                        "photoUri" to null,
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
}
