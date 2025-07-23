package com.example.studyblocks.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun StudyBlocksBottomNavigation(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.title
                    ) 
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Today.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Today",
        icon = Icons.Default.Home,
        route = Screen.Today.route
    ),
    BottomNavItem(
        title = "Subjects",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        route = Screen.Subjects.route
    ),
    BottomNavItem(
        title = "Analytics",
        icon = Icons.Default.Analytics,
        route = Screen.Analytics.route
    ),
    BottomNavItem(
        title = "Timer",
        icon = Icons.Default.Schedule,
        route = Screen.Timer.route
    ),
    BottomNavItem(
        title = "Profile",
        icon = Icons.Default.Person,
        route = Screen.Profile.route
    )
)