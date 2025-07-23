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
    var animationVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        animationVisible = true
        kotlinx.coroutines.delay(1500)
        animationVisible = false
        onAnimationComplete()
    }

    val alpha by animateFloatAsState(
        targetValue = if (animationVisible) 1f else 0f,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing)
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (animationVisible) -120f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing)
    )
    
    val scale by animateFloatAsState(
        targetValue = if (animationVisible) 1.0f else 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
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