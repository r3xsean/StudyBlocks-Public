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

@Composable
fun GlobalXPAnimation(
    xpChange: Int,
    tapX: Float,
    tapY: Float,
    onAnimationComplete: () -> Unit
) {
    // Animation phases
    var animationPhase by remember { mutableStateOf(0) } // 0: start, 1: rise, 2: fade
    var hasStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Only start animation if not already started (prevents restart on recomposition)
        if (!hasStarted) {
            hasStarted = true
            
            // Phase 1: Initial bounce (0-200ms)
            animationPhase = 1
            kotlinx.coroutines.delay(200)
            
            // Phase 2: Rise and fade (200-1500ms)  
            animationPhase = 2
            kotlinx.coroutines.delay(1300)
            
            // Complete animation
            onAnimationComplete()
        }
    }

    // Smooth continuous rising animation
    val offsetY by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0f
            1 -> -15f // Initial slight upward movement
            else -> -120f // Final upward position - continues moving until end
        },
        animationSpec = when (animationPhase) {
            1 -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
            else -> tween(
                durationMillis = 1300, // Match the fade duration
                easing = LinearOutSlowInEasing // Smooth continuous movement
            )
        },
        label = "offsetY"
    )
    
    // Scale with initial bounce effect
    val scale by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0.8f
            1 -> 1.3f // Bounce up
            else -> 1.0f // Settle to normal
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )
    
    // Alpha with smooth fade in and out
    val alpha by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 1f
            1 -> 1f // Fully visible during bounce
            else -> 0f // Fade out during rise
        },
        animationSpec = when (animationPhase) {
            2 -> tween(
                durationMillis = 1000,
                easing = LinearOutSlowInEasing
            )
            else -> tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        },
        label = "alpha"
    )

    val density = LocalDensity.current
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = if (xpChange > 0) "+$xpChange XP" else "$xpChange XP",
            color = if (xpChange > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset {
                    IntOffset(
                        tapX.toInt(),
                        (tapY + offsetY).toInt()
                    )
                }
                .alpha(alpha)
                .scale(scale)
        )
    }
}

object GlobalXPAnimator {
    var lastTapPosition: Offset = Offset(0f, 0f)
}