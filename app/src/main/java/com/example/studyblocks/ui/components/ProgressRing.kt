package com.example.studyblocks.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    strokeWidth: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 1000,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )
    
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.width.coerceAtMost(this.size.height) / 2f) - strokeWidthPx / 2f
            
            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
            }
        }
        
        content()
    }
}

@Composable
fun SubjectProgressRing(
    completedBlocks: Int,
    totalBlocks: Int,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    strokeWidth: Dp = 5.dp,
    showText: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val progress = if (totalBlocks > 0) completedBlocks.toFloat() / totalBlocks.toFloat() else 0f
    
    val progressColor = when {
        progress >= 1f -> Color(0xFF4CAF50) // Green when complete
        progress >= 0.7f -> Color(0xFF2196F3) // Blue when mostly done
        progress >= 0.4f -> Color(0xFFFF9800) // Orange when in progress
        else -> Color(0xFFE0E0E0) // Gray when just started
    }
    
    ProgressRing(
        progress = progress,
        modifier = modifier,
        size = size,
        strokeWidth = strokeWidth,
        progressColor = progressColor,
        animationDuration = 1200
    ) {
        if (showText && totalBlocks > 0) {
            Text(
                text = "$completedBlocks/$totalBlocks",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    glowEffect: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (progress >= 1f) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "completion_scale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val strokeWidthPx = with(density) { strokeWidth.toPx() }
        
        // Get colors outside Canvas context
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.width.coerceAtMost(this.size.height) / 2f) - strokeWidthPx / 2f
            
            // Glow effect background
            if (glowEffect && progress > 0f) {
                drawCircle(
                    color = primaryColor.copy(alpha = glowAlpha * 0.3f),
                    radius = radius + strokeWidthPx,
                    center = center
                )
            }
            
            // Background ring
            drawCircle(
                color = surfaceVariantColor.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Progress arc with gradient
            if (progress > 0f) {
                val gradient = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        tertiaryColor,
                        primaryColor
                    ),
                    center = center
                )
                
                drawArc(
                    brush = gradient,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
            }
        }
        
        content()
    }
}

@Composable
fun PulsingProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isActive: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val currentScale = if (isActive && progress < 1f) pulseScale else 1f
    
    Box(
        modifier = modifier
            .size(size)
            .scale(currentScale),
        contentAlignment = Alignment.Center
    ) {
        ProgressRing(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            progressColor = if (progress >= 1f) 
                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            animationDuration = 1500
        ) {
            content()
        }
    }
}