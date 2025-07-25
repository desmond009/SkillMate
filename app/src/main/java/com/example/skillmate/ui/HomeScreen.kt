package com.example.skillmate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skillmate.domain.MatchingEngine
import com.example.skillmate.ui.auth.AuthViewModel
import com.example.skillmate.ui.home.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val userId = authViewModel.currentUser.value?.uid
    val allUsers by homeViewModel.allUsers.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()

    // Fetch all users and current user profile
    LaunchedEffect(userId) {
        if (userId != null) homeViewModel.loadUsers(userId)
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isLoading -> {
                Spacer(Modifier.height(64.dp))
                CircularProgressIndicator()
            }
            error != null -> {
                Spacer(Modifier.height(64.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }
            currentUser != null -> {
                val matches = MatchingEngine.getTopMatches(currentUser!!, allUsers)
                if (matches.isEmpty()) {
                    Spacer(Modifier.height(64.dp))
                    Text("No matches found. Try updating your skills!", style = MaterialTheme.typography.bodyLarge)
                } else {
                    LazyColumn {
                        items(matches) { (user, score) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { navController.navigate("profileDetail/${user.id}") },
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(Modifier.padding(20.dp)) {
                                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Skills Offered: ${user.skillsOffered.joinToString()}", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Score: ${"%.2f".format(score)}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 