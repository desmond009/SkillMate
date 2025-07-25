package com.example.skillmate.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user
    }

    fun signOut() {
        auth.signOut()
    }
} 