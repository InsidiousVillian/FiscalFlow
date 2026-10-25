package com.example.fiscalflow

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthManager {

    // Connect to Firebase Authentication.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register a new Fiscal Flow user.
    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter your email and password.")
            return
        }

        auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Registration successful!")
            } else {
                onResult(
                    false,
                    task.exception?.localizedMessage
                        ?: "Registration failed."
                )
            }
        }
    }

    // Sign in an existing Fiscal Flow user.
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter your email and password.")
            return
        }

        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Login successful!")
            } else {
                onResult(
                    false,
                    task.exception?.localizedMessage
                        ?: "Login failed."
                )
            }
        }
    }

    // Get the currently signed-in Firebase user.
    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Sign out the current user.
    fun logout() {
        auth.signOut()
    }
}