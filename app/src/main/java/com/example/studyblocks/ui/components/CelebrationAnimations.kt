package com.example.studyblocks.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.XPGold
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun LevelUpCelebration(
    newLevel: Int,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    // Trigger haptic feedback
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(3000)
        onAnimationComplete()
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Particle effects background
        ParticleEffect(
            particleCount = 50,
            colors = listOf(XPGold, Color.Yellow, Color(0xFFFFA500))
        )
        
        // Main celebration card
        Card(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = XPGold.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    fontSize = 48.sp,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotation
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "LEVEL UP!",
                    style = StudyBlocksTypography.statisticsNumber,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Level $newLevel",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun XPGainAnimation(
    xpGain: Int,
    startPosition: Offset,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetY by animateFloatAsState(
        targetValue = -100f,
        animationSpec = tween(1500, easing = EaseOutCubic),
        finishedListener = { onAnimationComplete() },
        label = "xp_offset"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "xp_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "xp_scale"
    )
    
    Box(
        modifier = modifier
            .offset(
                x = startPosition.x.dp,
                y = (startPosition.y + offsetY).dp
            )
            .scale(scale)
            .alpha(alpha)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = XPGold.copy(alpha = 0.9f)
            ),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "+$xpGain XP",
                style = StudyBlocksTypography.xpGain,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun CompletionBurst(
    center: Offset,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        finishedListener = { onAnimationComplete() },
        label = "burst_progress"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawCompletionBurst(
            center = center,
            progress = animationProgress,
            size = size
        )
    }
}

@Composable
fun ParticleEffect(
    particleCount: Int = 30,
    colors: List<Color> = listOf(XPGold, Color.Yellow, Color(0xFFFFA500)),
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(particleCount) { 
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                velocityX = (Random.nextFloat() - 0.5f) * 2f,
                velocityY = (Random.nextFloat() - 0.5f) * 2f,
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f,
                life = Random.nextFloat() * 2f + 1f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        particles.forEach { particle ->
            val currentLife = particle.life * time
            if (currentLife < particle.life) {
                val alpha = 1f - (currentLife / particle.life)
                val currentX = particle.x + particle.velocityX * time
                val currentY = particle.y + particle.velocityY * time
                
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size * (1f - currentLife / particle.life),
                    center = Offset(
                        currentX * size.width,
                        currentY * size.height
                    )
                )
            }
        }
    }
}

@Composable
fun PulsatingIcon(
    content: @Composable () -> Unit,
    isPulsating: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isPulsating) 0.9f else 1f,
        targetValue = if (isPulsating) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier.scale(if (isPulsating) scale else 1f),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ShakeAnimation(
    isShaking: Boolean,
    intensity: Float = 10f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isShaking) intensity else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_offset"
    )
    
    Box(
        modifier = Modifier.offset(x = offsetX.dp)
    ) {
        content()
    }
}

@Composable
fun CounterAnimation(
    targetValue: Int,
    animationDuration: Int = 1000,
    style: androidx.compose.ui.text.TextStyle = StudyBlocksTypography.statisticsNumber,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var currentValue by remember { mutableIntStateOf(0) }
    
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = EaseOutCubic
        ),
        label = "counter"
    ) { value ->
        currentValue = value
    }
    
    Text(
        text = animatedValue.toString(),
        style = style,
        color = color
    )
}

// Helper functions and data classes
private data class Particle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val life: Float
)

private fun DrawScope.drawCompletionBurst(
    center: Offset,
    progress: Float,
    size: androidx.compose.ui.geometry.Size
) {
    val rayCount = 12
    val maxLength = minOf(size.width, size.height) * 0.3f
    
    for (i in 0 until rayCount) {
        val angle = (i * 2 * PI / rayCount).toFloat()
        val length = maxLength * progress
        val startRadius = 20f
        
        val startX = center.x + cos(angle) * startRadius
        val startY = center.y + sin(angle) * startRadius
        val endX = center.x + cos(angle) * (startRadius + length)
        val endY = center.y + sin(angle) * (startRadius + length)
        
        drawLine(
            color = XPGold.copy(alpha = 1f - progress),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 4f * (1f - progress)
        )
    }
}