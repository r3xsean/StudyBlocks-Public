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
// Firebase Auth disabled for open source version - login/signup screens commented out
// import com.example.studyblocks.ui.screens.auth.LoginScreen
// import com.example.studyblocks.ui.screens.auth.SignUpScreen
import com.example.studyblocks.ui.screens.profile.ProfileScreen
import com.example.studyblocks.ui.screens.subjects.SubjectsScreen
import com.example.studyblocks.ui.screens.subjects.SubjectDetailScreen
import com.example.studyblocks.ui.screens.subjects.ScheduleSettingsScreen
import com.example.studyblocks.ui.screens.timer.TimerScreen
import com.example.studyblocks.ui.screens.today.TodayScreen
import com.example.studyblocks.ui.screens.today.ConfidenceReevaluationScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingWelcomeScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingSubjectsScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingScheduleHorizonScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingDailyBlocksScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingBlockDurationScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingSubjectGroupingScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingNotificationPermissionScreen
import com.example.studyblocks.ui.screens.onboarding.OnboardingScheduleResultDialog
import com.example.studyblocks.ui.screens.summary.DailySummaryScreen
import com.example.studyblocks.repository.SchedulingResult
import com.example.studyblocks.notifications.NotificationService
import javax.inject.Inject

@Composable
fun StudyBlocksNavigation(
    navController: NavHostController,
    isAuthenticated: Boolean,
    paddingValues: PaddingValues,
    // authState: com.example.studyblocks.auth.AuthState, // Disabled for open source version
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
    
    // Handle navigation when onboarding status changes
    LaunchedEffect(needsOnboarding, isAuthenticated) {
        if (isAuthenticated && !needsOnboarding) {
            // User has completed onboarding, navigate to main app
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            println("DEBUG: Navigation - Current route: $currentRoute, needsOnboarding: $needsOnboarding")
            if (currentRoute?.startsWith("onboarding") == true) {
                println("DEBUG: Navigation - Onboarding completed, navigating to Today screen from route: $currentRoute")
                navController.navigate(Screen.Today.route) {
                    // Clear the entire backstack to prevent going back to onboarding
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    // Additional effect to handle redo onboarding completion from any onboarding screen
    LaunchedEffect(needsOnboarding) {
        // Only navigate if authenticated and no longer needs onboarding
        if (isAuthenticated && !needsOnboarding) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            println("DEBUG: Navigation - Checking for onboarding completion navigation: currentRoute=$currentRoute")
            // If we're currently on any onboarding screen, navigate away
            if (currentRoute?.startsWith("onboarding") == true) {
                println("DEBUG: Navigation - Force navigating from onboarding to Today screen")
                // Small delay to ensure UI state is ready
                kotlinx.coroutines.delay(100)
                navController.navigate(Screen.Today.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Auth screens - disabled for open source version
        // composable(Screen.Login.route) {
        //     LoginScreen(navController = navController)
        // }
        
        // composable(Screen.SignUp.route) {
        //     SignUpScreen(navController = navController)
        // }
        
        // Main app screens
        composable(Screen.Today.route) {
            TodayScreen(navController = navController)
        }
        
        composable(Screen.ConfidenceReevaluation.route) {
            ConfidenceReevaluationScreen(navController = navController)
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
                onGenerate = { blocksPerWeekday, blocksPerWeekend, horizon, blockDuration, grouping ->
                    subjectsViewModel.generateNewSchedule(blocksPerWeekday, blocksPerWeekend, horizon, blockDuration, grouping)
                },
                viewModel = subjectsViewModel
            )
        }
        
        composable(Screen.DailySummary.route) {
            DailySummaryScreen(
                navController = navController
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
                    navController.navigate(Screen.OnboardingScheduleHorizon.route)
                }
            )
        }
        
        composable(Screen.OnboardingScheduleHorizon.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            
            OnboardingScheduleHorizonScreen(
                navController = navController,
                initialWeeks = 3,
                onWeeksSelected = { weeks ->
                    onboardingViewModel.setScheduleHorizonWeeks(weeks)
                },
                onNext = {
                    navController.navigate(Screen.OnboardingBlockDuration.route)
                }
            )
        }
        
        composable(Screen.OnboardingDailyBlocks.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            val currentBlockDuration by onboardingViewModel.currentBlockDuration.collectAsState()
            
            OnboardingDailyBlocksScreen(
                navController = navController,
                initialWeekdayBlocks = 3,
                initialWeekendBlocks = 2,
                blockDurationMinutes = currentBlockDuration,
                onBlocksSelected = { weekday, weekend ->
                    onboardingViewModel.setDailyBlocks(weekday, weekend)
                },
                onNext = {
                    navController.navigate(Screen.OnboardingSubjectGrouping.route)
                }
            )
        }
        
        composable(Screen.OnboardingBlockDuration.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            
            OnboardingBlockDurationScreen(
                navController = navController,
                initialDuration = 60,
                onDurationSelected = { duration ->
                    onboardingViewModel.setBlockDuration(duration)
                },
                onNext = {
                    navController.navigate(Screen.OnboardingDailyBlocks.route)
                }
            )
        }
        
        composable(Screen.OnboardingSubjectGrouping.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            // Firebase Auth disabled for open source version
            // val authViewModel: com.example.studyblocks.auth.AuthViewModel = 
            //     androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            
            OnboardingSubjectGroupingScreen(
                navController = navController,
                onGroupingSelected = { grouping ->
                    onboardingViewModel.setSubjectGrouping(grouping)
                },
                onNext = {
                    navController.navigate(Screen.OnboardingNotificationPermission.route)
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
                        // For open source version, use the study repository to mark onboarding complete
                        println("DEBUG Navigation: Marking onboarding as completed")
                        
                        // Force navigation to Today screen immediately after marking onboarding complete
                        println("DEBUG Navigation: Force navigating to Today screen after onboarding completion")
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
                        
                        // Mark onboarding as completed and force navigation
                        // For open source version, onboarding is considered complete when schedule is generated
                        onboardingViewModel.resetOnboardingState()
                        println("DEBUG Navigation: Fallback - forcing navigation to Today screen")
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
        
        composable(Screen.OnboardingNotificationPermission.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val onboardingViewModel: com.example.studyblocks.ui.screens.onboarding.OnboardingViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            // Firebase Auth disabled for open source version
            // val authViewModel: com.example.studyblocks.auth.AuthViewModel = 
            //     androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            // Get notification service through TodayViewModel which has it injected
            val todayViewModel: com.example.studyblocks.ui.screens.today.TodayViewModel = 
                androidx.hilt.navigation.compose.hiltViewModel(context as androidx.activity.ComponentActivity)
            val notificationService = todayViewModel.notificationService
            
            OnboardingNotificationPermissionScreen(
                navController = navController,
                notificationService = notificationService,
                onNext = {
                    // Firebase Auth disabled for open source version - using simplified approach
                    println("DEBUG Navigation: Notification permission onNext triggered")
                    
                    // For open source version, use a default user ID or create one
                    val defaultUserId = "offline_user_001" // Simple offline user ID
                    
                    // Complete onboarding process - this will generate schedule and show dialog
                    onboardingViewModel.completeOnboarding(defaultUserId)
                    
                    // DON'T call markOnboardingCompleted() here - wait for dialog dismissal
                    println("DEBUG Navigation: Onboarding process started, waiting for dialog dismissal")
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
                    kotlinx.coroutines.delay(100) // Brief delay for state stabilization
                    
                    // Clear all onboarding state completely
                    onboardingViewModel.resetOnboardingState()
                    println("DEBUG Navigation: Cleared onboarding state")
                }
            }
            
            // Show schedule result dialog if we have results
            if (showOnboardingResultDialog && savedScheduleResult != null) {
                OnboardingScheduleResultDialog(
                    result = savedScheduleResult!!,
                    onDismiss = {
                        println("DEBUG Navigation: Onboarding result dialog dismissed by user")
                        showOnboardingResultDialog = false
                        dialogDismissed = true
                        
                        // NOW mark onboarding as completed - this will trigger navigation
                        // For open source version, use the study repository to mark onboarding complete
                        println("DEBUG Navigation: Marking onboarding as completed")
                        
                        // Force navigation to Today screen immediately after marking onboarding complete
                        println("DEBUG Navigation: Force navigating to Today screen after onboarding completion")
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
                        
                        // Mark onboarding as completed and force navigation
                        // For open source version, onboarding is considered complete when schedule is generated
                        onboardingViewModel.resetOnboardingState()
                        println("DEBUG Navigation: Fallback - forcing navigation to Today screen")
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
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
    object OnboardingScheduleHorizon : Screen("onboarding_schedule_horizon")
    object OnboardingDailyBlocks : Screen("onboarding_daily_blocks")
    object OnboardingBlockDuration : Screen("onboarding_block_duration")
    object OnboardingSubjectGrouping : Screen("onboarding_subject_grouping")
    object OnboardingNotificationPermission : Screen("onboarding_notification_permission")
    object DailySummary : Screen("daily_summary")
    object ConfidenceReevaluation : Screen("confidence_reevaluation")
}