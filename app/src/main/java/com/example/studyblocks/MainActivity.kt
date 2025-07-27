package com.example.studyblocks

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
// Firebase Auth disabled for open source version - these imports are commented out
// import com.example.studyblocks.auth.AuthState
// import com.example.studyblocks.auth.AuthViewModel
import com.example.studyblocks.data.model.AppTheme
import com.example.studyblocks.navigation.StudyBlocksBottomNavigation
import com.example.studyblocks.navigation.StudyBlocksNavigation
import com.example.studyblocks.notifications.NotificationService
import com.example.studyblocks.ui.screens.profile.ProfileViewModel
import com.example.studyblocks.ui.theme.StudyBlocksTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var notificationService: NotificationService
    
    // Permission request launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notification permission granted")
            // Test notification after permission is granted
            notificationService.testShowSimpleNotification()
        } else {
            android.util.Log.w("MainActivity", "Notification permission denied")
        }
    }
    
    // Static companion object to hold reference for external access
    companion object {
        private var currentActivity: MainActivity? = null
        
        fun requestNotificationPermission() {
            currentActivity?.requestNotificationPermissionInternal()
        }
    }
    
    private fun requestNotificationPermissionInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationService.hasNotificationPermission()) {
                android.util.Log.d("MainActivity", "Requesting notification permission")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                android.util.Log.d("MainActivity", "Notification permission already granted")
                // Test notification if permission already exists
                notificationService.testShowSimpleNotification()
            }
        } else {
            android.util.Log.d("MainActivity", "Android 12 or below, showing test notification")
            // For Android 12 and below, just show the notification
            notificationService.testShowSimpleNotification()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set current activity reference for permission requests
        currentActivity = this
        
        // Configure window for edge-to-edge experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            // Firebase Auth disabled for open source version - using simplified approach
            // For open source version, skip authentication and go directly to main app
            
            // Get theme preference - using ProfileViewModel directly since auth is disabled
            val profileViewModel: ProfileViewModel = hiltViewModel()
            
            val userPreferences by profileViewModel.userPreferences.collectAsState()
            
            // Determine dark theme based on user preference
            val darkTheme = when (userPreferences.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.AUTO -> isSystemInDarkTheme()
            }
            
            StudyBlocksTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                
                // Firebase Auth disabled for open source version - simplified navigation
                val isAuthenticated = true // Always authenticated in open source version
                
                // Check if onboarding is needed by checking if user has any subjects
                val needsOnboarding by profileViewModel.needsOnboarding.collectAsState()
                
                val showBottomNav = isAuthenticated && !needsOnboarding
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            println("DEBUG MainActivity: Showing bottom navigation")
                            StudyBlocksBottomNavigation(navController = navController)
                        } else {
                            println("DEBUG MainActivity: Hiding bottom navigation (showBottomNav = $showBottomNav)")
                        }
                    }
                ) { paddingValues ->
                    StudyBlocksNavigation(
                        navController = navController,
                        isAuthenticated = isAuthenticated,
                        paddingValues = paddingValues,
                        // authState = authState, // Disabled for open source version
                        needsOnboarding = needsOnboarding
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear activity reference to prevent memory leaks
        if (currentActivity == this) {
            currentActivity = null
        }
    }
}