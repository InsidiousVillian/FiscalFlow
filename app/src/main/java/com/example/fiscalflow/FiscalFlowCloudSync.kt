package com.example.fiscalflow

import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FiscalFlowCloudSync {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun observeCloudExpenses(ownerUsername: String): Flow<List<Expense>?> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firestoreRegistration?.remove()

            if (user == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            val uid = user.uid
            firestoreRegistration = firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FiscalFlowCloudSync", "Expense listener error for $ownerUsername ($uid): ${error.message}", error)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getLong("id") ?: return@mapNotNull null
                        val amount = doc.getDouble("amount") ?: return@mapNotNull null
                        val startDate = doc.getString("startDate") ?: return@mapNotNull null
                        val endDate = doc.getString("endDate") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val category = doc.getString("category") ?: return@mapNotNull null
                        val photoUri = doc.getString("photoUri")
                        val isIncome = doc.getBoolean("isIncome") ?: false
                        val owner = doc.getString("ownerUsername") ?: ownerUsername

                        Expense(
                            id = id,
                            amount = amount,
                            startDate = startDate,
                            endDate = endDate,
                            description = description,
                            category = category,
                            photoUri = photoUri,
                            isIncome = isIncome,
                            ownerUsername = owner
                        )
                    }
                    Log.d("FiscalFlowCloudSync", "Expenses received for $ownerUsername ($uid): count=${list.size}")
                    trySend(list)
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    fun observeCloudCategories(ownerUsername: String): Flow<List<String>?> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firestoreRegistration?.remove()

            if (user == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            val uid = user.uid
            firestoreRegistration = firestore.collection("users")
                .document(uid)
                .collection("categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FiscalFlowCloudSync", "Category listener error for $ownerUsername ($uid): ${error.message}", error)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.getString("name")
                    }
                    Log.d("FiscalFlowCloudSync", "Categories received for $ownerUsername ($uid): $list")
                    trySend(list)
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    fun observeCloudIncomeCategories(ownerUsername: String): Flow<List<String>?> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firestoreRegistration?.remove()

            if (user == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            val uid = user.uid
            firestoreRegistration = firestore.collection("users")
                .document(uid)
                .collection("income_categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FiscalFlowCloudSync", "Income category listener error for $ownerUsername ($uid): ${error.message}", error)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.getString("name")
                    }
                    Log.d("FiscalFlowCloudSync", "Income categories received for $ownerUsername ($uid): $list")
                    trySend(list)
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    fun observeCloudBudget(ownerUsername: String): Flow<BudgetingGoal?> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firestoreRegistration?.remove()

            if (user == null) {
                trySend(null)
                return@AuthStateListener
            }

            val uid = user.uid
            firestoreRegistration = firestore.collection("users")
                .document(uid)
                .collection("budget")
                .document("current")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val minimum = snapshot.getDouble("minimum")
                    val maximum = snapshot.getDouble("maximum")
                    val month = snapshot.getLong("month")
                    val year = snapshot.getLong("year")

                    if (minimum != null && maximum != null && month != null && year != null) {
                        trySend(
                            BudgetingGoal(
                                minimum = minimum,
                                maximum = maximum,
                                month = month.toInt(),
                                year = year.toInt(),
                                ownerUsername = ownerUsername
                            )
                        )
                    } else {
                        trySend(null)
                    }
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    fun observeCloudProgress(ownerUsername: String): Flow<UsersProgress?> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firestoreRegistration?.remove()

            if (user == null) {
                trySend(null)
                return@AuthStateListener
            }

            val uid = user.uid
            firestoreRegistration = firestore.collection("users")
                .document(uid)
                .collection("progress")
                .document("current")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val xp = snapshot.getLong("xp")?.toInt() ?: 0
                    val streak = snapshot.getLong("streak")?.toInt() ?: 0
                    val milestonesRaw = snapshot.getString("milestonesRaw") ?: ""

                    trySend(
                        UsersProgress(
                            xp = xp,
                            streak = streak,
                            milestonesRaw = milestonesRaw,
                            ownerUsername = ownerUsername
                        )
                    )
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    suspend fun uploadExpenseDirect(ownerUsername: String, expense: Expense): Boolean {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to save expense.")

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
            "ownerUsername" to ownerUsername
        )
        docRef.set(expenseData).await()
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server write for expense ID ${expense.id} at users/$uid/expenses/${expense.id}")
        return true
    }

    suspend fun deleteCloudExpenseDirect(ownerUsername: String, expenseId: Long) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to delete expense.")

        firestore.collection("users")
            .document(uid)
            .collection("expenses")
            .document(expenseId.toString())
            .delete()
            .await()
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server delete for expense ID $expenseId at users/$uid/expenses/$expenseId")
    }

    suspend fun uploadCategoryDirect(ownerUsername: String, categoryName: String) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to create category.")

        val categoryId = Base64.encodeToString(
            categoryName.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        firestore.collection("users").document(uid)
            .collection("categories").document(categoryId)
            .set(mapOf("name" to categoryName, "ownerUsername" to ownerUsername))
            .await()
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server category write for '$categoryName' at users/$uid/categories/$categoryId")
    }

    suspend fun deleteCloudCategoryDirect(ownerUsername: String, categoryName: String) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to delete category.")

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
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server category delete for '$categoryName' at users/$uid/categories/$categoryId")
    }

    suspend fun uploadIncomeCategoryDirect(ownerUsername: String, categoryName: String) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to create income category.")

        val categoryId = Base64.encodeToString(
            categoryName.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        firestore.collection("users").document(uid)
            .collection("income_categories").document(categoryId)
            .set(mapOf("name" to categoryName, "ownerUsername" to ownerUsername))
            .await()
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server income category write for '$categoryName' at users/$uid/income_categories/$categoryId")
    }

    suspend fun deleteCloudIncomeCategoryDirect(ownerUsername: String, categoryName: String) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to delete income category.")

        val categoryId = Base64.encodeToString(
            categoryName.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        val userRef = firestore.collection("users").document(uid)
        userRef.collection("income_categories")
            .document(categoryId)
            .delete()
            .await()

        val matchingExpenses = userRef.collection("expenses")
            .whereEqualTo("category", categoryName)
            .whereEqualTo("isIncome", true)
            .get()
            .await()

        for (expense in matchingExpenses.documents) {
            expense.reference.delete().await()
        }
        Log.i("FiscalFlowCloudSync", "CONFIRMED: Server income category delete for '$categoryName' at users/$uid/income_categories/$categoryId")
    }

    suspend fun uploadBudgetDirect(ownerUsername: String, budget: BudgetingGoal) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to save budget goal.")

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
    }

    suspend fun uploadProgressDirect(ownerUsername: String, progress: UsersProgress) {
        val uid = FirebaseIdentity.currentUid()
            ?: throw IllegalStateException("Internet connection required to update user progress.")

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
    }
}
