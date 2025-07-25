package com.example.skillmate.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val users = db.collection("users")

    suspend fun getUser(userId: String): Result<UserProfile?> = runCatching {
        val doc = users.document(userId).get().await()
        doc.toObject(UserProfile::class.java)
    }

    suspend fun createOrUpdateUser(profile: UserProfile): Result<Unit> = runCatching {
        users.document(profile.id).set(profile).await()
    }

    fun observeUser(userId: String): Flow<UserProfile?> = callbackFlow {
        val registration: ListenerRegistration = users.document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { registration.remove() }
    }

    suspend fun getAllPublicUsers(excludeUserId: String): Result<List<UserProfile>> = runCatching {
        val query = users.whereEqualTo("public", true).get().await()
        query.documents.mapNotNull { it.toObject(UserProfile::class.java) }
            .filter { it.id != excludeUserId }
    }
} 