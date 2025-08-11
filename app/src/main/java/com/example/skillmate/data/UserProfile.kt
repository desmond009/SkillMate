package com.example.skillmate.data

// User profile data model for Firestore and matching

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val location: String? = null,
    val skillsOffered: List<String> = emptyList(),
    val availability: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val avgRating: Double = 0.0,
    val isOpenToSwap: Boolean = true,
    val aboutMe: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) 