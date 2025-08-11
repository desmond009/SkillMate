package com.example.skillmate.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.skillmate.data.UserProfile
import com.example.skillmate.ui.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.example.skillmate.data.SwapRequest
import com.example.skillmate.data.SwapRequestRepository
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navBackStackEntry: NavBackStackEntry? = null
) {
    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val userId = authViewModel.currentUser.value?.uid
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skillsOffered by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(setOf<String>()) }
    var isPublic by remember { mutableStateOf(true) }
    var avgRating by remember { mutableStateOf(0.0) }
    var isOpenToSwap by remember { mutableStateOf(true) }
    var aboutMe by remember { mutableStateOf("") }
    val allSlots = listOf("Weekdays", "Weekends", "Mornings", "Evenings")

    // Load profile on entry and when navigating to this screen
    LaunchedEffect(userId, navBackStackEntry) { userId?.let { profileViewModel.loadProfile(it) } }
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            location = it.location ?: ""
            skillsOffered = it.skillsOffered.joinToString(", ")
            availability = it.availability.toSet()
            isPublic = it.isPublic
            avgRating = it.avgRating
            isOpenToSwap = it.isOpenToSwap
            aboutMe = it.aboutMe
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(90.dp),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(60.dp), tint = Color.Gray)
            }
        }
        Spacer(Modifier.height(20.dp))
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }
        if (error != null) {
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxSize()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = skillsOffered,
            onValueChange = { skillsOffered = it },
            label = { Text("Skills Offered (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        // Helper: Show parsed skillsOffered
        val parsedOffered = skillsOffered.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (skillsOffered.isNotBlank() && parsedOffered.size <= 1) {
            Text("\u26a0\ufe0f Separate skills with commas, e.g. 'kotlin, java'", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (parsedOffered.isNotEmpty()) {
            Text("Parsed: ${parsedOffered.joinToString()}", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        Text("Average Rating: ${"%.2f".format(avgRating)}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = isOpenToSwap, onCheckedChange = { isOpenToSwap = it })
            Spacer(Modifier.width(8.dp))
            Text(if (isOpenToSwap) "Open to Swap" else "Not Open to Swap")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = aboutMe,
            onValueChange = { aboutMe = it },
            label = { Text("About Me") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )
        Spacer(Modifier.height(10.dp))
        Text("Availability:", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            allSlots.forEach { slot ->
                FilterChip(
                    selected = availability.contains(slot),
                    onClick = {
                        availability = if (availability.contains(slot)) availability - slot else availability + slot
                    },
                    label = { Text(slot) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = isPublic, onCheckedChange = { isPublic = it })
            Spacer(Modifier.width(8.dp))
            Text(if (isPublic) "Public Profile" else "Private Profile")
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                userId?.let {
                    val profileObj = UserProfile(
                        id = it,
                        name = name,
                        location = location.takeIf { it.isNotBlank() },
                        skillsOffered = skillsOffered.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        availability = availability.toList(),
                        isPublic = isPublic,
                        avgRating = avgRating,
                        isOpenToSwap = isOpenToSwap,
                        aboutMe = aboutMe
                    )
                    profileViewModel.saveProfile(profileObj)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Save Profile")
        }
    }
}

@Composable
fun ProfileDetailScreen(userId: String) {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var requestSent by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        isLoading = true
        error = null
        try {
            // Fetch user profile from Firestore
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(userId).get().await()
            profile = doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        } else if (error != null) {
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
        } else if (profile != null) {
            Card(
                modifier = Modifier.size(90.dp),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(60.dp), tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(profile!!.name, style = MaterialTheme.typography.titleLarge)
            profile!!.location?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(10.dp))
            Text("Skills Offered: ${profile!!.skillsOffered.joinToString()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Text("Availability: ${profile!!.availability.joinToString()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            if (!requestSent) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = {
                    scope.launch {
                        val req = SwapRequest(
                            id = java.util.UUID.randomUUID().toString(),
                            fromUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            toUserId = userId,
                            skillMatched = profile!!.skillsOffered,
                            availabilityMatch = profile!!.availability,
                            status = "pending",
                            timestamp = System.currentTimeMillis()
                        )
                        SwapRequestRepository.sendRequest(req.copy(skillMatched = profile!!.skillsOffered, availabilityMatch = profile!!.availability))
                        requestSent = true
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send Request" + if (message.isBlank()) " (Default Message)" else "")
                }
            } else {
                Text("Request sent!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
} 