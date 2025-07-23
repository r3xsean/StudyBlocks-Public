package com.example.studyblocks.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

// Animation durations
object AnimationDurations {
    const val SHORT = 150
    const val MEDIUM = 300
    const val LONG = 500
}

// Easing curves
object AnimationEasing {
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

// Card entrance animation
@Composable
fun EnterAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                easing = AnimationEasing.StandardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                easing = LinearEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(
                durationMillis = AnimationDurations.SHORT,
                easing = AnimationEasing.FastOutSlowIn
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.SHORT,
                easing = LinearEasing
            )
        ),
        content = content
    )
}

// Study block completion animation
@Composable
fun CompletionAnimation(
    isCompleted: Boolean,
    content: @Composable () -> Unit
) {
    val scale = animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "completion_scale"
    )

    val alpha = animateFloatAsState(
        targetValue = if (isCompleted) 0.7f else 1f,
        animationSpec = tween(
            durationMillis = AnimationDurations.MEDIUM,
            easing = AnimationEasing.StandardEasing
        ),
        label = "completion_alpha"
    )

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
    ) {
        content()
    }
}

// Progress animation for timer
@Composable
fun animateProgressAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = AnimationDurations.MEDIUM,
        easing = AnimationEasing.StandardEasing
    )
) = animateFloatAsState(
    targetValue = targetValue,
    animationSpec = animationSpec,
    label = "progress_animation"
)

// Shake animation for errors
@Composable
fun rememberShakeController(): ShakeController {
    return androidx.compose.runtime.remember { ShakeController() }
}

class ShakeController {
    private val animatable = Animatable(0f)
    
    suspend fun shake() {
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 300
                val easing = FastOutLinearInEasing
                
                for (i in 1..8) {
                    val fraction = i / 8f
                    val value = if (i % 2 == 0) 10f else -10f
                    (fraction at (37 * i)).using(easing)
                }
            }
        )
    }
    
    val offset: Float
        get() = animatable.value
}

@Composable
fun Modifier.shake(controller: ShakeController): Modifier {
    return this.graphicsLayer {
        translationX = controller.offset
    }
}

// Staggered list animation
@Composable
fun StaggeredAnimation(
    itemIndex: Int,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val delay = itemIndex * 50 // 50ms stagger per item
    
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                delayMillis = delay,
                easing = AnimationEasing.EmphasizedEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                delayMillis = delay,
                easing = LinearEasing
            )
        ),
        content = content
    )
}

// Page transition for navigation
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.pagerTransition(
    pagerState: PagerState,
    page: Int
): Modifier {
    return graphicsLayer {
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
        
        // Parallax effect
        translationX = pageOffset * size.width * 0.3f
        
        // Scale and alpha based on distance from current page
        val scale = 1f - (pageOffset.absoluteValue * 0.1f).coerceAtMost(0.1f)
        scaleX = scale
        scaleY = scale
        
        alpha = (1f - pageOffset.absoluteValue).coerceAtLeast(0.3f)
    }
}

// Loading animation
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        label = "loading_rotation"
    )
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation.value
        }
    )
}

// Bounce animation for buttons
@Composable
fun Modifier.bounceClick(onClick: (() -> Unit)? = null): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    
    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounce_scale"
    )
    
    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .let { modifier ->
            if (onClick != null) {
                modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                modifier
            }
        }
}