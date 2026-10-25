
package com.example.fiscalflow

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseIdentity {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
