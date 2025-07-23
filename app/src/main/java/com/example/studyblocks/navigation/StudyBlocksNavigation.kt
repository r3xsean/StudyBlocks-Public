package com.example.studyblocks.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studyblocks.ui.screens.analytics.AnalyticsScreen
import com.example.studyblocks.ui.screens.auth.LoginScreen
import com.example.studyblocks.ui.screens.auth.SignUpScreen
import com.example.studyblocks.ui.screens.profile.ProfileScreen
import com.example.studyblocks.ui.screens.subjects.SubjectsScreen
import com.example.studyblocks.ui.screens.subjects.SubjectDetailScreen
import com.example.studyblocks.ui.screens.timer.TimerScreen
import com.example.studyblocks.ui.screens.today.TodayScreen

@Composable
fun StudyBlocksNavigation(
    navController: NavHostController,
    isAuthenticated: Boolean,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.Today.route else Screen.Login.route,
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
}