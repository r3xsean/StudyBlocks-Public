package com.example.studyblocks.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.theme.animateColorAsStateMaterial3
import com.example.studyblocks.ui.theme.animateFloatAsStateSpring

@Composable
fun EnhancedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    border: androidx.compose.foundation.BorderStroke? = null,
    animatePress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsStateSpring(
        targetValue = if (isPressed && animatePress) 0.98f else 1f,
        label = "card_press_scale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled
                    ) {
                        isPressed = true
                        onClick()
                    }
                } else Modifier
            ),
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        content = content
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = StudyGradients.primaryGradient,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(gradient, shape)
            .clip(shape),
        content = content
    )
}

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsStateSpring(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_press_scale"
    )
    
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = StudyGradients.primaryGradient,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsStateSpring(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "gradient_button_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.6f,
        label = "button_alpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                shape = shape
            )
            .clip(shape)
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun FloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 56.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsStateSpring(
        targetValue = if (isPressed) 0.9f else 1f,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        label = "fab_scale"
    )
    
    val elevation by animateFloatAsStateSpring(
        targetValue = if (isPressed) 4f else 8f,
        label = "fab_elevation"
    )
    
    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .size(size)
            .scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = elevation.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val scale by animateFloatAsStateSpring(
        targetValue = if (selected) 1.05f else 1f,
        label = "chip_scale"
    )
    
    FilterChip(
        onClick = onClick ?: {},
        label = { Text(text) },
        selected = selected,
        modifier = modifier.scale(scale),
        colors = colors,
        leadingIcon = leadingIcon
    )
}

@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    icon: ImageVector,
    contentDescription: String? = null,
    rotateOnClick: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0f) }
    
    val scale by animateFloatAsStateSpring(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "icon_button_scale"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_rotation"
    )
    
    IconButton(
        onClick = {
            isPressed = true
            if (rotateOnClick) {
                rotation += 180f
            }
            onClick()
        },
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = if (rotateOnClick) animatedRotation else 0f
                }
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun ProgressCard(
    progress: Float,
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = progressColor,
                    trackColor = backgroundColor
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsStateSpring(
        targetValue = 1f,
        label = "glass_card_scale"
    )
    
    Card(
        onClick = onClick ?: {},
        modifier = modifier.scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        content = content
    )
}

