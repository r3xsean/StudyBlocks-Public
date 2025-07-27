package com.example.studyblocks.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry
import com.example.studyblocks.ui.theme.AnimationDurations
import com.example.studyblocks.ui.theme.AnimationEasing

object ScreenTransitions {
    
    // Slide transitions for forward navigation
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    // Slide transitions for back navigation
    val slideInFromLeft = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val slideOutToRight = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    // Fade transitions for tab navigation
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        )
    )
    
    // Scale transitions for modal screens
    val scaleIn = scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val scaleOut = scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        )
    )
    
    // Slide up transitions for bottom sheets
    val slideInFromBottom = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val slideOutToBottom = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        )
    )
    
    // Combined transitions for different navigation patterns
    
    // Forward navigation (e.g., list to detail)
    val forwardEnter = slideInFromRight + fadeIn
    val forwardExit = slideOutToLeft + fadeOut
    
    // Back navigation
    val backEnter = slideInFromLeft + fadeIn
    val backExit = slideOutToRight + fadeOut
    
    // Tab navigation
    val tabEnter = fadeIn
    val tabExit = fadeOut
    
    // Modal navigation (e.g., dialog-like screens)
    val modalEnter = slideInFromBottom + fadeIn + scaleIn
    val modalExit = slideOutToBottom + fadeOut + scaleOut
    
    // Shared element-like transition
    val sharedElementEnter = fadeIn + scaleIn(
        initialScale = 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val sharedElementExit = fadeOut + scaleOut(
        targetScale = 1.2f,
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.FastOutLinearIn
        )
    )
}

// Extension functions for easy use with Navigation Compose
fun AnimatedContentTransitionScope<NavBackStackEntry>.slideHorizontally(
    towards: AnimatedContentTransitionScope.SlideDirection
) = when (towards) {
    AnimatedContentTransitionScope.SlideDirection.Left -> {
        ScreenTransitions.slideInFromLeft to ScreenTransitions.slideOutToLeft
    }
    AnimatedContentTransitionScope.SlideDirection.Right -> {
        ScreenTransitions.slideInFromRight to ScreenTransitions.slideOutToRight
    }
    else -> {
        ScreenTransitions.slideInFromRight to ScreenTransitions.slideOutToLeft
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideVertically(
    towards: AnimatedContentTransitionScope.SlideDirection
) = when (towards) {
    AnimatedContentTransitionScope.SlideDirection.Up -> {
        ScreenTransitions.slideInFromBottom to ScreenTransitions.slideOutToBottom
    }
    else -> {
        ScreenTransitions.slideInFromBottom to ScreenTransitions.slideOutToBottom
    }
}

// Helper to determine navigation pattern based on routes
fun getNavigationPattern(
    initialRoute: String?,
    targetRoute: String?
): NavigationPattern {
    return when {
        isTabNavigation(initialRoute, targetRoute) -> NavigationPattern.TAB
        isModalNavigation(targetRoute) -> NavigationPattern.MODAL
        isBackNavigation(initialRoute, targetRoute) -> NavigationPattern.BACK
        else -> NavigationPattern.FORWARD
    }
}

private fun isTabNavigation(initialRoute: String?, targetRoute: String?): Boolean {
    val tabRoutes = listOf("today", "subjects", "analytics", "timer", "profile")
    return initialRoute in tabRoutes && targetRoute in tabRoutes
}

private fun isModalNavigation(targetRoute: String?): Boolean {
    val modalRoutes = listOf("add_subject", "edit_subject", "schedule_settings")
    return targetRoute in modalRoutes
}

private fun isBackNavigation(initialRoute: String?, targetRoute: String?): Boolean {
    // This is a simplified check - in a real app you'd track the navigation stack
    return false // Implement based on your navigation structure
}

enum class NavigationPattern {
    FORWARD,
    BACK,
    TAB,
    MODAL
}

// Smooth navigation transitions based on pattern
@OptIn(ExperimentalAnimationApi::class)
fun AnimatedContentTransitionScope<NavBackStackEntry>.getEnterTransition(
    pattern: NavigationPattern
): EnterTransition = when (pattern) {
    NavigationPattern.FORWARD -> ScreenTransitions.forwardEnter
    NavigationPattern.BACK -> ScreenTransitions.backEnter
    NavigationPattern.TAB -> ScreenTransitions.tabEnter
    NavigationPattern.MODAL -> ScreenTransitions.modalEnter
}

@OptIn(ExperimentalAnimationApi::class)
fun AnimatedContentTransitionScope<NavBackStackEntry>.getExitTransition(
    pattern: NavigationPattern
): ExitTransition = when (pattern) {
    NavigationPattern.FORWARD -> ScreenTransitions.forwardExit
    NavigationPattern.BACK -> ScreenTransitions.backExit
    NavigationPattern.TAB -> ScreenTransitions.tabExit
    NavigationPattern.MODAL -> ScreenTransitions.modalExit
}