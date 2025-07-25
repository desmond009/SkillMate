package com.example.skillmate.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.example.skillmate.ui.explore.ExploreScreen
import com.example.skillmate.ui.profile.ProfileScreen
import com.example.skillmate.ui.profile.ProfileDetailScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(modifier, navController = navController) }
        composable("profile") { backStackEntry ->
            ProfileScreen(
                modifier,
                navBackStackEntry = backStackEntry
            )
        }
        composable("profileDetail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileDetailScreen(userId = userId)
        }
        composable("explore") { ExploreScreen(modifier, navController = navController) }
        composable("requests") { RequestsScreen(modifier) }
        composable("feedback") { FeedbackScreen(modifier) }
    }
}