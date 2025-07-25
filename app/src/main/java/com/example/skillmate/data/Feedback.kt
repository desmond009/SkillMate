package com.example.skillmate.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Feedback(
    val id: String = "",
    val toUserId: String = "",
    val fromUserId: String = "",
    val rating: Int = 0, // 1-5
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object FeedbackRepository {
    private val db = FirebaseFirestore.getInstance()
    private val feedbacks = db.collection("feedbacks")

    suspend fun addFeedback(feedback: Feedback) {
        feedbacks.document(feedback.id).set(feedback).await()
    }

    suspend fun getFeedbackForUser(userId: String): List<Feedback> {
        val result = feedbacks.whereEqualTo("toUserId", userId).get().await()
        return result.documents.mapNotNull { it.toObject(Feedback::class.java) }
    }
} 