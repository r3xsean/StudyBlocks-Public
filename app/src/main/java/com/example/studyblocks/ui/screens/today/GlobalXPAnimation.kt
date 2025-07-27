package com.example.studyblocks.ui.screens.today

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.CompositingStrategy

@Composable
fun GlobalXPAnimation(
    xpChange: Int,
    tapX: Float,
    tapY: Float,
    onAnimationComplete: () -> Unit
) {
    // Animation phases with more refined timing
    var animationPhase by remember { mutableStateOf(0) } // 0: start, 1: bounce, 2: rise & fade
    var hasStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = xpChange, key2 = tapX, key3 = tapY) {
        if (!hasStarted) {
            hasStarted = true
            
            // Phase 1: Initial impact bounce (0-250ms)
            animationPhase = 1
            kotlinx.coroutines.delay(250)
            
            // Phase 2: Rise and fade (250-2000ms) - extended further to prevent early disappearing
            animationPhase = 2
            kotlinx.coroutines.delay(1750) // Increased from 1550ms to ensure animation completes
            
            onAnimationComplete()
        }
    }

    // Enhanced vertical movement with better easing
    val offsetY by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0f
            1 -> -20f // More pronounced initial bounce
            else -> -150f // Higher final position for better visibility
        },
        animationSpec = when (animationPhase) {
            1 -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
            else -> tween(
                durationMillis = 1750, // Match the new delay duration
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f) // Custom smooth easing
            )
        },
        label = "offsetY"
    )
    
    // Enhanced scale animation with more dramatic effect
    val scale by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0.7f // Start smaller
            1 -> 1.4f // More dramatic bounce
            else -> 1.1f // Slightly larger than normal at end
        },
        animationSpec = when (animationPhase) {
            1 -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
            else -> spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "scale"
    )
    
    // Refined alpha with smoother transitions
    val alpha by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0f // Start invisible
            1 -> 1f // Fully visible during bounce
            else -> 0f // Smooth fade out
        },
        animationSpec = when (animationPhase) {
            1 -> tween(
                durationMillis = 150,
                easing = FastOutSlowInEasing
            )
            2 -> tween(
                durationMillis = 1550, // Increased to match the new total duration
                delayMillis = 200, // Small delay before fade starts
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
            )
            else -> tween(durationMillis = 100)
        },
        label = "alpha"
    )

    // Add subtle rotation for dynamic feel
    val rotation by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0f
            1 -> if (xpChange > 0) 5f else -5f // Slight tilt based on positive/negative
            else -> if (xpChange > 0) -3f else 3f // Counter-rotate during rise
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Disable clipping at the container level
                clip = false
            },
        contentAlignment = Alignment.TopStart
    ) {
        // Container Box with generous padding to accommodate scaling
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        // Center the container around tap position with room for scaling
                        (tapX - 50f).coerceAtLeast(10f).coerceAtMost(600f).toInt(),
                        (tapY + offsetY - 20f).coerceAtLeast(10f).toInt()
                    )
                }
                .size(100.dp, 50.dp) // Fixed size container larger than scaled text
                .alpha(alpha)
                .scale(scale)
                .graphicsLayer {
                    rotationZ = rotation
                    clip = false // Disable clipping
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (xpChange > 0) "+$xpChange XP" else "$xpChange XP",
                color = if (xpChange > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        // Particle effect overlay - DISABLED to remove circular effect
        // if (animationPhase == 1 && alpha > 0.8f) {
        //     ParticleEffect(
        //         centerX = tapX.coerceAtLeast(60f).coerceAtMost(650f), // Match container center position
        //         centerY = (tapY + offsetY - 5f).coerceAtLeast(30f),
        //         isPositive = xpChange > 0,
        //         alpha = alpha,
        //         scale = scale * 0.8f
        //     )
        // }
    }
}

@Composable
private fun ParticleEffect(
    centerX: Float,
    centerY: Float,
    isPositive: Boolean,
    alpha: Float,
    scale: Float
) {
    val particlePositions = remember {
        listOf(
            Pair(-25f, -15f), Pair(25f, -15f), Pair(-15f, -25f), Pair(15f, -25f),
            Pair(-30f, 10f), Pair(30f, 10f), Pair(0f, -30f), Pair(-20f, 20f), Pair(20f, 20f)
        )
    }
    
    val particleColor = if (isPositive) Color(0xFF81C784) else Color(0xFFEF5350)
    
    particlePositions.forEachIndexed { index, (offsetX, offsetY) ->
        val delay = index * 20 // Stagger particles
        
        val particleAlpha by animateFloatAsState(
            targetValue = alpha * 0.7f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delay,
                easing = LinearOutSlowInEasing
            ),
            label = "particle_alpha_$index"
        )
        
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (centerX + offsetX * scale).toInt(),
                        (centerY + offsetY * scale).toInt()
                    )
                }
                .size(4.dp)
                .alpha(particleAlpha)
                .scale(scale)
                .background(
                    color = particleColor,
                    shape = CircleShape
                )
        )
    }
}

object GlobalXPAnimator {
    var lastTapPosition: Offset = Offset(0f, 0f)
}