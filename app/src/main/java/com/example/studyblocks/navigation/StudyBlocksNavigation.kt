package com.example.studyblocks.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.example.studyblocks.ui.screens.analytics.AnalyticsScreen
import com.example.studyblocks.ui.screens.auth.LoginScreen
import com.example.studyblocks.ui.screens.auth.SignUpScreen
import com.example.studyblocks.ui.screens.profile.ProfileScreen
import com.example.studyblocks.ui.screens.subjects.SubjectsScreen
import com.example.studyblocks.ui.screens.subjects.SubjectDetailScreen
import com.example.studyblocks.ui.screens.subjects.ScheduleSettingsScreen
import com.example.studyblocks.ui.screens.timer.TimerScreen
import com.example.studyblocks.ui.screens.today.TodayScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingWelcomeScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingSubjectsScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingPreferencesScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingScheduleResultDialog
import com.example.studyblocks.repository.SchedulingResult

@Composable
fun StudyBlocksNavigation(
    navController: NavHostController,
    isAuthenticated: Boolean,
    paddingValues: PaddingValues,
    authState: com.example.studyblocks.auth.AuthState,
    needsOnboarding: Boolean = false
) {
    val startDestination = when {
        !isAuthenticated -> Screen.Login.route
        needsOnboarding -> Screen.OnboardingWelcome.route
        else -> Screen.Today.route
    }
    
    println("DEBUG: StudyBlocksNavigation - isAuthenticated = $isAuthenticated")
    println("DEBUG: StudyBlocksNavigation - needsOnboarding = $needsOnboarding")
    println("DEBUG: StudyBlocksNavigation - startDestination = $startDestination")
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        
        // Main app screens
        composable(Screen.Today.route) {
            TodayScreen(navController = navController)
        }
        
        composable(Screen.Subjects.route) {
            SubjectsScreen(navController = navController)
        }
        
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }
        
        composable(Screen.Timer.route) {
            TimerScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.SubjectDetail.route) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            SubjectDetailScreen(
                subjectId = subjectId,
                navController = navController
            )
        }
        
        composable(Screen.ScheduleSettings.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val subjectsViewModel: com.example.studyblocks.ui.screens.subjects.SubjectsViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            ScheduleSettingsScreen(
                navController = navController,
                currentPreferredBlocks = subjectsViewModel.preferredBlocksPerDay.collectAsState().value,
                onGenerate = { blocksPerWeekday, blocksPerWeekend, horizon, blockDuration ->
                    subjectsViewModel.generateNewSchedule(blocksPerWeekday, blocksPerWeekend, horizon, blockDuration)
                },
                viewModel = subjectsViewModel
            )
        }
        
        // Onboarding screens
        composable(Screen.OnboardingWelcome.route) {
            OnboardingWelcomeScreen(navController = navController)
        }
        
        composable(Screen.OnboardingSubjects.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            OnboardingSubjectsScreen(
                navController = navController,
                onSubjectsCreated = { subjects ->
                    onboardingViewModel.setSubjects(subjects)
                }
            )
        }
        
        composable(Screen.OnboardingPreferences.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            val authViewModel: com.example.studyblocks.auth.AuthViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            
            OnboardingPreferencesScreen(
                navController = navController,
                onPreferencesSet = { preferences ->
                    onboardingViewModel.setSchedulePreferences(preferences)
                },
                onComplete = {
                    println("DEBUG Navigation: onComplete callback triggered")
                    if (authState is com.example.studyblocks.auth.AuthState.Authenticated) {
                        println("DEBUG Navigation: User is authenticated, starting onboarding completion")
                        println("DEBUG Navigation: User ID: ${authState.user.uid}")
                        
                        // Complete onboarding process - this will generate schedule and show dialog
                        onboardingViewModel.completeOnboarding(authState.user.uid)
                        
                        // DON'T call markOnboardingCompleted() here - wait for dialog dismissal
                        println("DEBUG Navigation: Onboarding process started, waiting for dialog dismissal")
                    } else {
                        println("DEBUG Navigation: User not authenticated, cannot complete onboarding")
                    }
                }
            )
            
            // Handle onboarding completion and schedule result dialog
            val scheduleResult by onboardingViewModel.scheduleResult.collectAsState()
            val onboardingComplete by onboardingViewModel.onboardingComplete.collectAsState()
            
            // Completely local state management for dialog
            var showOnboardingResultDialog by remember { mutableStateOf(false) }
            var savedScheduleResult by remember { mutableStateOf<SchedulingResult?>(null) }
            var dialogDismissed by remember { mutableStateOf(false) }
            
            // Capture schedule result exactly once when it becomes available
            LaunchedEffect(scheduleResult) {
                if (scheduleResult != null && savedScheduleResult == null && !dialogDismissed) {
                    println("DEBUG Navigation: Capturing schedule result for dialog")
                    savedScheduleResult = scheduleResult
                    showOnboardingResultDialog = true
                }
            }
            
            // Handle cleanup after dialog dismissal
            LaunchedEffect(dialogDismissed) {
                if (dialogDismissed) {
                    println("DEBUG Navigation: Starting cleanup after dialog dismissal...")
                    kotlinx.coroutines.delay(1000) // Allow navigation to complete
                    onboardingViewModel.resetOnboardingState()
                    println("DEBUG Navigation: Cleanup completed")
                }
            }
            
            // Show dialog - completely independent of ViewModel state
            if (showOnboardingResultDialog && savedScheduleResult != null && !dialogDismissed) {
                println("DEBUG Navigation: Showing OnboardingScheduleResultDialog")
                OnboardingScheduleResultDialog(
                    result = savedScheduleResult!!,
                    onDismiss = {
                        println("DEBUG Navigation: User dismissed dialog")
                        showOnboardingResultDialog = false
                        dialogDismissed = true
                        
                        // NOW mark onboarding as completed - this will trigger navigation
                        authViewModel.markOnboardingCompleted()
                        println("DEBUG Navigation: Called markOnboardingCompleted() after dialog dismissal")
                        
                        // Navigation will happen automatically due to needsOnboarding becoming false
                    }
                )
            }
            
            // Fallback navigation if no schedule result after reasonable time
            LaunchedEffect(onboardingComplete) {
                if (onboardingComplete && !dialogDismissed) {
                    println("DEBUG Navigation: Onboarding complete, waiting for schedule result...")
                    kotlinx.coroutines.delay(4000) // Wait longer for schedule generation
                    
                    // Only navigate if dialog hasn't been shown or dismissed
                    if (!showOnboardingResultDialog && !dialogDismissed) {
                        println("DEBUG Navigation: No schedule result, using fallback navigation")
                        
                        // Mark onboarding as completed - this will trigger automatic navigation
                        authViewModel.markOnboardingCompleted()
                        onboardingViewModel.resetOnboardingState()
                        println("DEBUG Navigation: Fallback navigation completed")
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Today : Screen("today")
    object Subjects : Screen("subjects")
    object Analytics : Screen("analytics")
    object Timer : Screen("timer")
    object Profile : Screen("profile")
    object SubjectDetail : Screen("subject_detail/{subjectId}")
    object ScheduleSettings : Screen("schedule_settings")
    object OnboardingWelcome : Screen("onboarding_welcome")
    object OnboardingSubjects : Screen("onboarding_subjects")
    object OnboardingPreferences : Screen("onboarding_preferences")
}