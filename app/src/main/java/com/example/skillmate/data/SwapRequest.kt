package com.example.skillmate.data

// Swap request data model for Firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class SwapRequest(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val skillMatched: List<String> = emptyList(),
    val availabilityMatch: List<String> = emptyList(),
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis()
)

object SwapRequestRepository {
    private val db = FirebaseFirestore.getInstance()
    private val requests = db.collection("swap_requests")

    suspend fun sendRequest(request: SwapRequest) {
        requests.document(request.id).set(request).await()
    }

    suspend fun getRequestsForUser(userId: String): List<SwapRequest> {
        val result = requests.whereEqualTo("toUserId", userId).get().await()
        return result.documents.mapNotNull { it.toObject(SwapRequest::class.java) }
    }

    suspend fun updateRequestStatus(requestId: String, status: String) {
        requests.document(requestId).update("status", status).await()
    }

    suspend fun getSentRequests(userId: String): List<SwapRequest> {
        val result = requests.whereEqualTo("fromUserId", userId).get().await()
        return result.documents.mapNotNull { it.toObject(SwapRequest::class.java) }
    }
} 