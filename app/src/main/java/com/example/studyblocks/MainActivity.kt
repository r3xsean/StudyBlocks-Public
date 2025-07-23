package com.example.studyblocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.studyblocks.auth.AuthState
import com.example.studyblocks.auth.AuthViewModel
import com.example.studyblocks.data.model.AppTheme
import com.example.studyblocks.navigation.StudyBlocksBottomNavigation
import com.example.studyblocks.navigation.StudyBlocksNavigation
import com.example.studyblocks.ui.screens.profile.ProfileViewModel
import com.example.studyblocks.ui.theme.StudyBlocksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configure window for edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            
            // Get theme preference if user is authenticated
            val profileViewModel: ProfileViewModel? = if (authState is AuthState.Authenticated) {
                hiltViewModel()
            } else null
            
            val userPreferences by (profileViewModel?.userPreferences?.collectAsState() 
                ?: remember { androidx.compose.runtime.mutableStateOf(
                    com.example.studyblocks.data.model.UserPreferences("")
                ) })
            
            // Determine dark theme based on user preference
            val darkTheme = when (userPreferences.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.AUTO -> isSystemInDarkTheme()
            }
            
            StudyBlocksTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                
                val isAuthenticated = authState is AuthState.Authenticated
                val showBottomNav = isAuthenticated
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            StudyBlocksBottomNavigation(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    StudyBlocksNavigation(
                        navController = navController,
                        isAuthenticated = isAuthenticated,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }
}