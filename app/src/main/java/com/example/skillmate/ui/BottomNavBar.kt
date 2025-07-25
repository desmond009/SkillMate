package com.example.skillmate.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star

@Composable
fun BottomNavBar(selectedRoute: String, onTabSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedRoute == "home",
            onClick = { onTabSelected("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
            label = { Text("Explore") },
            selected = selectedRoute == "explore",
            onClick = { onTabSelected("explore") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Requests") },
            label = { Text("Requests") },
            selected = selectedRoute == "requests",
            onClick = { onTabSelected("requests") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = selectedRoute == "profile",
            onClick = { onTabSelected("profile") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Feedback") },
            label = { Text("Feedback") },
            selected = selectedRoute == "feedback",
            onClick = { onTabSelected("feedback") }
        )
    }
} 