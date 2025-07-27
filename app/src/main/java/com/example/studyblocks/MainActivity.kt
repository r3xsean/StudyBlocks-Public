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
import com.example.studyblocks.auth.AuthState
import com.example.studyblocks.auth.AuthViewModel
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
            val authViewModel: AuthViewModel = hiltViewModel(this@MainActivity)
            val authState by authViewModel.authState.collectAsState()
            val isFirstTimeLogin by authViewModel.isFirstTimeLogin.collectAsState()
            
            println("DEBUG MainActivity: setContent recomposed - authState=$authState, isFirstTimeLogin=$isFirstTimeLogin")
            println("DEBUG MainActivity: ViewModel instance: ${authViewModel.hashCode()}")
            
            // Force recomposition tracking
            LaunchedEffect(authState) {
                println("DEBUG MainActivity: authState changed to $authState")
                println("DEBUG MainActivity: After authState change - isAuthenticated=${authState is AuthState.Authenticated}, needsOnboarding=${(authState is AuthState.Authenticated) && isFirstTimeLogin}, showBottomNav=${(authState is AuthState.Authenticated) && !isFirstTimeLogin}")
            }
            
            LaunchedEffect(isFirstTimeLogin) {
                println("DEBUG MainActivity: isFirstTimeLogin changed to $isFirstTimeLogin")
                println("DEBUG MainActivity: After isFirstTimeLogin change - isAuthenticated=${authState is AuthState.Authenticated}, needsOnboarding=${(authState is AuthState.Authenticated) && isFirstTimeLogin}, showBottomNav=${(authState is AuthState.Authenticated) && !isFirstTimeLogin}")
            }
            
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
                
                // Don't evaluate these values until auth state is not loading
                val isAuthenticated = authState is AuthState.Authenticated
                val needsOnboarding = isAuthenticated && isFirstTimeLogin
                val showBottomNav = isAuthenticated && !needsOnboarding
                
                // Debug logging (remove in production)
                LaunchedEffect(authState, isFirstTimeLogin) {
                    println("DEBUG MainActivity: authState = $authState")
                    println("DEBUG MainActivity: isFirstTimeLogin = $isFirstTimeLogin")
                    println("DEBUG MainActivity: isAuthenticated = $isAuthenticated")
                    println("DEBUG MainActivity: needsOnboarding = $needsOnboarding")
                    println("DEBUG MainActivity: showBottomNav = $showBottomNav")
                }
                
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
                        authState = authState,
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