package com.example.skillmate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skillmate.data.Feedback
import com.example.skillmate.data.FeedbackRepository
import com.example.skillmate.ui.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel = hiltViewModel()) {
    val userId = authViewModel.currentUser.value?.uid ?: return
    var feedbackList by remember { mutableStateOf<List<Feedback>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        isLoading = true
        error = null
        try {
            feedbackList = FeedbackRepository.getFeedbackForUser(userId)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Feedback & Ratings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        when {
            isLoading -> {
                Spacer(Modifier.height(64.dp))
                CircularProgressIndicator()
            }
            error != null -> {
                Spacer(Modifier.height(64.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }
            feedbackList.isEmpty() -> {
                Spacer(Modifier.height(64.dp))
                Text("No feedback yet.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> {
                LazyColumn {
                    items(feedbackList) { fb ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("From User: ${fb.fromUserId}", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text("Rating: ${fb.rating} \u2b50", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Text(fb.comment, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
} 