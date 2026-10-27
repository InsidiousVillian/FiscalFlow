package com.example.fiscalflow

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseIdentity {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun firebaseEmail(username: String): String {
        val trimmed = username.trim()
        if (trimmed.contains("@")) {
            return trimmed.lowercase(Locale.ROOT)
        }
        val normalized = trimmed.lowercase(Locale.ROOT)
        return "$normalized@fiscalflow.com"
    }

    suspend fun registerOrSignIn(username: String, password: String): String {
        val email = firebaseEmail(username)
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("Firebase did not return a user UID.")
            Log.i("FiscalFlowAuth", "Firebase createUserWithEmailAndPassword succeeded for $email -> UID: $uid")
            uid
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.w("FiscalFlowAuth", "Email $email already exists in Firebase Auth. Attempting sign-in to verify UID...")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw IllegalStateException("Firebase did not return a user UID.")
            Log.i("FiscalFlowAuth", "Firebase signInWithEmailAndPassword succeeded for $email -> UID: $uid")
            uid
        } catch (e: Exception) {
            Log.e("FiscalFlowAuth", "Firebase Auth registration error for $email: ${e.message}", e)
            throw e
        }
    }

    suspend fun signIn(username: String, password: String): String {
        val email = firebaseEmail(username)
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid
            ?: throw IllegalStateException("Firebase did not return a user ID.")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun currentUid(): String? = auth.currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    suspend fun ensureSignedIn(): String {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            return currentUser.uid
        }

        return suspendCancellableCoroutine { continuation ->
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener

                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid

                        if (uid != null) {
                            continuation.resume(uid)
                        } else {
                            continuation.resumeWithException(
                                IllegalStateException(
                                    "Firebase did not return a user ID."
                                )
                            )
                        }
                    } else {
                        continuation.resumeWithException(
                            task.exception
                                ?: IllegalStateException(
                                    "Anonymous Firebase sign-in failed."
                                )
                        )
                    }
                }
        }
    }
}
