package com.example.studyblocks.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyblocks.ui.theme.ConfidenceColors
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.theme.XPGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    useGradient: Boolean = false,
    gradient: Brush = StudyGradients.primaryGradient,
    strokeWidth: Dp = 8.dp,
    animationDuration: Int = 1000
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )

    Box(
        modifier = modifier
            .height(strokeWidth)
            .clip(RoundedCornerShape(strokeWidth / 2))
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(trackColor, RoundedCornerShape(strokeWidth / 2))
        )
        
        // Progress
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    if (useGradient) gradient else SolidColor(progressColor),
                    RoundedCornerShape(strokeWidth / 2)
                )
        )
    }
}

@Composable
fun AnimatedCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    useGradient: Boolean = false,
    gradient: Brush = StudyGradients.primaryGradient,
    showPercentage: Boolean = true,
    centerContent: @Composable (() -> Unit)? = null,
    animationDuration: Int = 1500
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "circular_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.toPx()
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (diameter - strokeWidthPx) / 2
            val centerX = diameter / 2
            val centerY = diameter / 2
            
            // Draw track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Draw progress
            val sweepAngle = 360f * animatedProgress
            if (useGradient) {
                drawArc(
                    brush = gradient,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(diameter - strokeWidthPx, diameter - strokeWidthPx),
                    style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(diameter - strokeWidthPx, diameter - strokeWidthPx),
                    style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
        
        if (centerContent != null) {
            centerContent()
        } else if (showPercentage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun XPProgressBar(
    currentXP: Long,
    targetXP: Long,
    level: Int,
    modifier: Modifier = Modifier,
    showXPNumbers: Boolean = true,
    animationDuration: Int = 2000
) {
    val progress = if (targetXP > 0) (currentXP.toFloat() / targetXP.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "xp_progress"
    )
    
    val animatedCurrentXP by animateFloatAsState(
        targetValue = currentXP.toFloat(),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "current_xp"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level $level",
                style = StudyBlocksTypography.levelDisplay,
                color = Color.White
            )
            
            if (showXPNumbers) {
                Text(
                    text = "${animatedCurrentXP.toLong()}/${targetXP} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AnimatedLinearProgressIndicator(
            progress = animatedProgress,
            useGradient = true,
            gradient = StudyGradients.xpGradient,
            strokeWidth = 12.dp,
            animationDuration = 0 // Already animated above
        )
    }
}

@Composable
fun ConfidenceRatingIndicator(
    confidence: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    interactive: Boolean = false,
    onConfidenceChange: ((Int) -> Unit)? = null
) {
    val targetProgress = confidence / 10f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "confidence_progress"
    )
    
    val color = ConfidenceColors.getOrElse(confidence - 1) { ConfidenceColors.last() }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.toPx()
            val strokeWidth = 8.dp.toPx()
            val radius = (diameter - strokeWidth) / 2
            val centerX = diameter / 2
            val centerY = diameter / 2
            
            // Draw track
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw confidence arc
            val sweepAngle = 360f * animatedProgress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(diameter - strokeWidth, diameter - strokeWidth),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = confidence.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "/10",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PulsingProgressDot(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.8f else 1f,
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.6f else 1f,
        targetValue = if (isActive) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = if (isActive) scale else 1f
                scaleY = if (isActive) scale else 1f
                this.alpha = alpha
            }
            .background(
                if (isActive) activeColor else inactiveColor,
                CircleShape
            )
    )
}

@Composable
fun WaveProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 60.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "wave_progress"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
    ) {
        val width = size.width
        val waveHeight = size.height
        val fillHeight = waveHeight * animatedProgress
        
        if (animatedProgress > 0) {
            val path = Path()
            val waveAmplitude = 8.dp.toPx()
            val waveFrequency = 0.02f
            
            path.moveTo(0f, waveHeight)
            path.lineTo(0f, waveHeight - fillHeight + waveAmplitude)
            
            for (x in 0..width.toInt()) {
                val y = waveHeight - fillHeight + 
                        waveAmplitude * sin(x * waveFrequency + waveOffset)
                path.lineTo(x.toFloat(), y)
            }
            
            path.lineTo(width, waveHeight)
            path.close()
            
            drawPath(path, waveColor)
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}