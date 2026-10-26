
package com.example.fiscalflow

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.Locale

object FirebaseIdentity {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Create a consistent internal identifier for Firebase
    fun firebaseEmail(username: String): String {
        val trimmed = username.trim()
        if (trimmed.contains("@")) {
            return trimmed.lowercase(Locale.ROOT)
        }
        val normalized = trimmed.lowercase(Locale.ROOT)
        return "$normalized@fiscalflow.com"
    }

    // Return the current Firebase UID without creating or switching accounts.
    fun requireCurrentUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException(
                "No Firebase account is signed in. Cloud sync is unavailable."
            )
    }


    // Sign in an existing Firebase account for this username
    suspend fun signIn(username: String, password: String): String {
        val email = firebaseEmail(username)
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid
            ?: throw IllegalStateException("Firebase did not return a user ID.")
    }

    // Create a separate Firebase identity for a newly registered username
    suspend fun register(username: String, password: String): String {
        require(password.length >= 8) {
            "Password must be at least 8 characters."
        }

        val email = firebaseEmail(username)
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid
            ?: throw IllegalStateException("Firebase did not return a user ID.")
    }

    // Return the currently signed-in Firebase identity, if any.
    fun currentUid(): String? = auth.currentUser?.uid

    // Sign out the current Firebase identity when the local user logs out.
    fun signOut() {
        auth.signOut()
    }
}
