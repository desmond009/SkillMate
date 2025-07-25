package com.example.skillmate.ui.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skillmate.domain.MatchingEngine
import com.example.skillmate.ui.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.navigation.NavController

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    exploreViewModel: ExploreViewModel = hiltViewModel(),
    navController: NavController
) {
    var query by remember { mutableStateOf("") }
    val userId = authViewModel.currentUser.value?.uid
    val allUsers by exploreViewModel.allUsers.collectAsState()
    val isLoading by exploreViewModel.isLoading.collectAsState()
    val error by exploreViewModel.error.collectAsState()
    val currentUserProfile by exploreViewModel.currentUserProfile.collectAsState()

    // Always reload users when ExploreScreen is shown
    LaunchedEffect(true) {
        if (userId != null) exploreViewModel.loadUsers(userId)
    }

    // Cache for tapped user IDs
    val tappedUserIds = remember { mutableStateListOf<String>() }

    // Fuzzy search: filter users by skill tags using Levenshtein distance
    val results = remember(query, allUsers, currentUserProfile) {
        Log.d("SkillMateDebug", "[Explore] allUsers.size = ${allUsers.size}")
        allUsers.forEach { user ->
            Log.d("SkillMateDebug", "User: ${user.name}, Offered: ${user.skillsOffered}")
        }
        val filtered = if (query.isBlank()) allUsers else allUsers.filter { user ->
            // Only match by offered skills
            val offered = user.skillsOffered.map { it.lowercase() }
            val myOffered = currentUserProfile?.skillsOffered?.map { it.lowercase() } ?: emptyList()
            // Skill match logic
            val swapMatch = myOffered.any { it in offered }
            // Fuzzy search logic
            val fuzzy = user.skillsOffered.any { skill ->
                skill.contains(query, ignoreCase = true) ||
                MatchingEngine.levenshtein(skill.lowercase(), query.lowercase()) <= 2
            }
            swapMatch || fuzzy
        }
        Log.d("SkillMateDebug", "[Explore] results.size = ${filtered.size}")
        filtered
    }

    // Only show tapped cards after a search
    val tappedResults = if (query.isNotBlank()) results.filter { tappedUserIds.contains(it.id) } else emptyList()

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by skill...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        when {
            isLoading -> {
                Spacer(Modifier.height(64.dp))
                CircularProgressIndicator()
                Text("Loading users...", style = MaterialTheme.typography.bodySmall)
            }
            error != null -> {
                Spacer(Modifier.height(64.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }
            tappedResults.isEmpty() -> {
                Spacer(Modifier.height(64.dp))
                Text("No users found for this skill.", style = MaterialTheme.typography.bodyLarge)
                Text("Try updating your skills or check your profile is public.", style = MaterialTheme.typography.bodySmall)
            }
            else -> {
                LazyColumn {
                    items(tappedResults, key = { it.id }) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    if (!tappedUserIds.contains(user.id)) tappedUserIds.add(user.id)
                                    navController.navigate("profileDetail/${user.id}")
                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("Offers: ${user.skillsOffered.joinToString()}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
} 