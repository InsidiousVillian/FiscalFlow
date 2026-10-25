
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

        val categories = database.categoryDao().observeAll().first()
        val expenses = database.expenseDao().observeAll().first()
        val budget = database.budgetGoalDao().observeCurrent().first()

        for (category in categories)
        {
            val categoryId = Base64.encodeToString(
                category.name.toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            userRef.collection("categories")
                .document(categoryId)
                .set(mapOf("name" to category.name))
                .await()
        }

        for (expense in expenses)
        {
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

        if (budget != null)
        {
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

    suspend fun deleteCloudCategory(categoryName: String)
    {
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

        for (expense in matchingExpenses.documents)
        {
            expense.reference.delete().await()
        }
    }
}
