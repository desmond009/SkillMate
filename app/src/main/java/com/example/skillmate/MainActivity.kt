// SkillSwap: A DSA-focused Jetpack Compose app for portfolio use
// Built by a solo developer to showcase Data Structures & Algorithms in real-world app logic
package com.example.skillmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.skillmate.ui.ScaffoldedApp
import com.example.skillmate.ui.theme.SkillMateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillMateTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScaffoldedApp()
                }
            }
        }
    }
}

