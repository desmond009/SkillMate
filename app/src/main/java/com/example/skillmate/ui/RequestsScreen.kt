package com.example.skillmate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.skillmate.data.SwapRequestRepository
import com.example.skillmate.data.SwapRequest
import com.example.skillmate.ui.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skillmate.data.Feedback
import com.example.skillmate.data.FeedbackRepository

@Composable
fun RequestsScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel = hiltViewModel()) {
    val userId = authViewModel.currentUser.value?.uid ?: return
    var sentRequests by remember { mutableStateOf<List<SwapRequest>>(emptyList()) }
    var receivedRequests by remember { mutableStateOf<List<SwapRequest>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Received, 1: Sent
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        isLoading = true
        error = null
        try {
            receivedRequests = SwapRequestRepository.getRequestsForUser(userId)
            sentRequests = SwapRequestRepository.getSentRequests(userId)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Swap Requests", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Received") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Sent") })
        }
        Spacer(Modifier.height(8.dp))
        when {
            isLoading -> {
                Spacer(Modifier.height(64.dp))
                CircularProgressIndicator()
            }
            error != null -> {
                Spacer(Modifier.height(64.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }
            (selectedTab == 0 && receivedRequests.isEmpty()) || (selectedTab == 1 && sentRequests.isEmpty()) -> {
                Spacer(Modifier.height(64.dp))
                Text("No swap requests yet.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> {
                val requestsToShow = if (selectedTab == 0) receivedRequests else sentRequests
                LazyColumn {
                    items(requestsToShow, key = { it.id }) { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("From User: ${req.fromUserId}", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text("Skills Matched: ${req.skillMatched.joinToString()}", style = MaterialTheme.typography.bodySmall)
                                Text("Availability: ${req.availabilityMatch.joinToString()}", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Text("Status: ${req.status}", style = MaterialTheme.typography.labelSmall)
                                if (selectedTab == 0 && req.status == "pending") {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            scope.launch {
                                                SwapRequestRepository.updateRequestStatus(req.id, "accepted")
                                                receivedRequests = SwapRequestRepository.getRequestsForUser(userId)
                                                // Add to feedback section
                                                val feedback = Feedback(
                                                    id = java.util.UUID.randomUUID().toString(),
                                                    toUserId = req.fromUserId,
                                                    fromUserId = userId,
                                                    rating = 0,
                                                    comment = "",
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                FeedbackRepository.addFeedback(feedback)
                                            }
                                        }) { Text("Accept") }
                                        TextButton(onClick = {
                                            scope.launch {
                                                SwapRequestRepository.updateRequestStatus(req.id, "rejected")
                                                receivedRequests = SwapRequestRepository.getRequestsForUser(userId)
                                            }
                                        }) { Text("Reject") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 