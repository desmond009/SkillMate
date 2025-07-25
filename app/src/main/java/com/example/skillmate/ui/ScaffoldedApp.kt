    package com.example.skillmate.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skillmate.ui.auth.AuthScreen
import com.example.skillmate.ui.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout

val screenTitles = mapOf(
    "home" to "SkillSwap Suggestions",
    "explore" to "Explore Skills",
    "requests" to "Swap Requests",
    "profile" to "Profile",
    "feedback" to "Feedback & Ratings"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldedApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    if (currentUser == null) {
        AuthScreen(authViewModel)
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "home"

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screenTitles[currentRoute] ?: "SkillSwap") },
                    actions = {
                        if (currentRoute == "profile") {
                            IconButton(onClick = { authViewModel.signOut() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            bottomBar = {
                BottomNavBar(selectedRoute = currentRoute) { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
} 