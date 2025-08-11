package com.example.skillmate.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val successMessage by profileViewModel.successMessage.collectAsState()
    val userId = authViewModel.currentUser.value?.uid
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skillsOffered by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(setOf<String>()) }
    var isPublic by remember { mutableStateOf(true) }
    var avgRating by remember { mutableStateOf(0.0) }
    var isOpenToSwap by remember { mutableStateOf(true) }
    var aboutMe by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    val allSlots = listOf("Weekdays", "Weekends", "Mornings", "Evenings")
    val scrollState = rememberScrollState()

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
    
    // Clear success message after 3 seconds and reset saving state
    LaunchedEffect(successMessage, error) {
        if (successMessage != null) {
            isSaving = false
            kotlinx.coroutines.delay(3000)
            profileViewModel.clearSuccessMessage()
        }
        if (error != null) {
            isSaving = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Enhanced Profile Header
        Card(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = "Profile", 
                    modifier = Modifier.size(80.dp), 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Profile Name with better styling
        Text(
            text = name.ifEmpty { "Your Name" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        
        // User email display
        authViewModel.currentUser.value?.email?.let { email ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Last updated timestamp
        profile?.lastUpdated?.let { timestamp ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Last updated: ${java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Rating display with stars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < avgRating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star",
                    tint = if (index < avgRating.toInt()) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${"%.1f".format(avgRating)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(32.dp))

        if (isLoading && profile == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    "Loading profile...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        
        if (!isLoading && profile == null && error == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "No Profile",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No Profile Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Fill in the form below to create your profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        if (successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        successMessage ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Changes indicator
        if (profile != null && (
            name != profile!!.name ||
            location != (profile!!.location ?: "") ||
            skillsOffered != profile!!.skillsOffered.joinToString(", ") ||
            availability != profile!!.availability.toSet() ||
            isPublic != profile!!.isPublic ||
            isOpenToSwap != profile!!.isOpenToSwap ||
            aboutMe != profile!!.aboutMe
        )) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Changes",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "You have unsaved changes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // Form Fields
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, "Name") },
            isError = name.isBlank()
        )
        
        if (name.isBlank()) {
            Text(
                "Name is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, "Location") },
            placeholder = { Text("City, Country") }
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = skillsOffered,
            onValueChange = { skillsOffered = it },
            label = { Text("Skills Offered *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Psychology, "Skills") },
            placeholder = { Text("e.g., Kotlin, Java, UI/UX Design") },
            isError = skillsOffered.isBlank()
        )
        
        if (skillsOffered.isBlank()) {
            Text(
                "At least one skill is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        } else {
            val parsedOffered = skillsOffered.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (parsedOffered.isNotEmpty()) {
                Text(
                    "Skills (${parsedOffered.size}): ${parsedOffered.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = aboutMe,
            onValueChange = { aboutMe = it.take(500) },
            label = { Text("About Me") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            leadingIcon = { Icon(Icons.Default.Info, "About") },
            placeholder = { Text("Tell others about yourself, your experience, and what you're looking for...") }
        )
        
        // Character count for About Me
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${aboutMe.length}/500",
                style = MaterialTheme.typography.bodySmall,
                color = if (aboutMe.length > 450) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Availability Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Availability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (availability.isNotEmpty()) {
                Text(
                    "${availability.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allSlots.forEach { slot ->
                FilterChip(
                    selected = availability.contains(slot),
                    onClick = {
                        availability = if (availability.contains(slot)) {
                            availability - slot
                        } else {
                            availability + slot
                        }
                    },
                    label = { Text(slot) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Settings Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Profile Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Public Profile",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Public Profile")
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Open to Swap",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Open to Swap")
                    }
                    Switch(
                        checked = isOpenToSwap,
                        onCheckedChange = { isOpenToSwap = it }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Profile Preview Section
        if (name.isNotBlank() || skillsOffered.isNotBlank() || aboutMe.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Profile Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    if (name.isNotBlank()) {
                        Text(
                            "Name: $name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (location.isNotBlank()) {
                        Text(
                            "Location: $location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (skillsOffered.isNotBlank()) {
                        val parsedSkills = skillsOffered.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (parsedSkills.isNotEmpty()) {
                            Text(
                                "Skills: ${parsedSkills.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    if (availability.isNotEmpty()) {
                        Text(
                            "Availability: ${availability.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (aboutMe.isNotBlank()) {
                        Text(
                            "About: ${aboutMe.take(100)}${if (aboutMe.length > 100) "..." else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, "Delete")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Profile")
                }
                
                Button(
                    onClick = {
                        if (name.isNotBlank() && skillsOffered.isNotBlank()) {
                            isSaving = true
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
                                    aboutMe = aboutMe,
                                    lastUpdated = System.currentTimeMillis()
                                )
                                profileViewModel.saveProfile(profileObj)
                            }
                        } else {
                            showValidationDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Saving..." else if (profile != null) "Update Profile" else "Create Profile")
                }
            }
            
            // Reset form button
            if (profile != null) {
                OutlinedButton(
                    onClick = {
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Refresh, "Reset")
                    Spacer(Modifier.width(8.dp))
                    Text("Reset to Saved Profile")
                }
            }
        }
    }
    
    // Delete Profile Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete your profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userId?.let { profileViewModel.deleteProfile(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Validation Dialog
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            title = { Text("Missing Information") },
            text = { 
                Text(
                    when {
                        name.isBlank() -> "Please enter your full name."
                        skillsOffered.isBlank() -> "Please enter at least one skill."
                        else -> "Please fill in all required fields."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = false }) {
                    Text("OK")
                }
            }
        )
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