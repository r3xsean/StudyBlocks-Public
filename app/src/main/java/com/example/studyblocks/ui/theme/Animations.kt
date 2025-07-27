package com.example.studyblocks.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin

// Standard animation durations following Material Design guidelines
object AnimationDurations {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 1000
}

// Standard easing curves for Material Design
object AnimationEasing {
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val LinearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val Standard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val Accelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

// Pre-configured animation specs
object StudyBlocksAnimations {
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
    
    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        )
    )
    
    val slideInFromBottom = slideInVertically(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        ),
        initialOffsetY = { it }
    )
    
    val slideOutToBottom = slideOutVertically(
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        ),
        targetOffsetY = { it }
    )
    
    val scaleIn = scaleIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        ),
        initialScale = 0.8f,
        transformOrigin = TransformOrigin.Center
    )
    
    val scaleOut = scaleOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.FAST,
            easing = AnimationEasing.LinearOutSlowIn
        ),
        targetScale = 0.8f,
        transformOrigin = TransformOrigin.Center
    )
    
    // Screen transition animations
    val screenEnter = slideInFromBottom + fadeIn + scaleIn
    val screenExit = slideOutToBottom + fadeOut + scaleOut
    
    // Dialog animations
    val dialogEnter = fadeIn + scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val dialogExit = fadeOut + scaleOut
    
    // Card animations
    val cardEnter = fadeIn + slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.FastOutSlowIn
        )
    )
}

// Composable for animated content transitions
@Composable
fun AnimatedContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = StudyBlocksAnimations.fadeIn,
    exit: ExitTransition = StudyBlocksAnimations.fadeOut,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

// Smooth state transitions for Material 3
@Composable
fun animateColorAsStateMaterial3(
    targetValue: androidx.compose.ui.graphics.Color,
    label: String = "ColorAnimation"
): State<androidx.compose.ui.graphics.Color> = animateColorAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = AnimationDurations.NORMAL,
        easing = AnimationEasing.FastOutSlowIn
    ),
    label = label
)

@Composable
fun animateFloatAsStateMaterial3(
    targetValue: Float,
    label: String = "FloatAnimation"
): State<Float> = animateFloatAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = AnimationDurations.NORMAL,
        easing = AnimationEasing.FastOutSlowIn
    ),
    label = label
)

@Composable
fun animateDpAsStateMaterial3(
    targetValue: androidx.compose.ui.unit.Dp,
    label: String = "DpAnimation"
): State<androidx.compose.ui.unit.Dp> = animateDpAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = AnimationDurations.NORMAL,
        easing = AnimationEasing.FastOutSlowIn
    ),
    label = label
)

// Spring animations for interactive elements
@Composable
fun animateFloatAsStateSpring(
    targetValue: Float,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessLow,
    label: String = "SpringFloatAnimation"
): State<Float> = animateFloatAsState(
    targetValue = targetValue,
    animationSpec = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    ),
    label = label
)

// Staggered animations for lists
@Composable
fun StaggeredListAnimation(
    itemIndex: Int,
    staggerDelay: Int = 50,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((itemIndex * staggerDelay).toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = AnimationEasing.FastOutSlowIn
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = AnimationEasing.FastOutSlowIn
            )
        )
    ) {
        content()
    }
}

// Shared element transition helpers
object SharedElementTransitions {
    fun cardToDialog() = fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.SLOW,
            easing = AnimationEasing.FastOutSlowIn
        )
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.SLOW,
            easing = AnimationEasing.FastOutSlowIn
        ),
        initialScale = 0.8f
    )
    
    fun dialogToCard() = fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.LinearOutSlowIn
        )
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = AnimationEasing.LinearOutSlowIn
        ),
        targetScale = 0.8f
    )
}

// Pulsing animation for attention-grabbing elements
@Composable
fun PulsingAnimation(
    content: @Composable (scale: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    content(scale)
}

// Breathing animation for subtle emphasis
@Composable
fun BreathingAnimation(
    content: @Composable (alpha: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )
    
    content(alpha)
}