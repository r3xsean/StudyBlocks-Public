package com.example.studyblocks.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studyblocks.ui.theme.StudyGradients

@Composable
fun ModernCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    useGradient: Boolean = false,
    gradientBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation * 0.5f else elevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_elevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            ),
        colors = colors,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = if (useGradient && gradientBrush != null) {
                Modifier
                    .fillMaxWidth()
                    .background(gradientBrush, RoundedCornerShape(cornerRadius))
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            }
        ) {
            content()
        }
    }
}

@Composable
fun StudyBlockCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    isOverdue: Boolean = false,
    isPending: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = when {
        isCompleted -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
        isOverdue -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
        isPending -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        )
        else -> CardDefaults.cardColors()
    }
    
    val gradientBrush = when {
        isCompleted -> StudyGradients.successGradient
        isOverdue -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            )
        )
        isPending -> StudyGradients.warningGradient
        else -> StudyGradients.surfaceGradient
    }
    
    ModernCard(
        onClick = onClick,
        modifier = modifier,
        colors = cardColors,
        elevation = if (isCompleted) 2.dp else 6.dp,
        cornerRadius = 20.dp,
        useGradient = isCompleted || isOverdue || isPending,
        gradientBrush = gradientBrush,
        content = content
    )
}

@Composable
fun SubjectCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    confidenceLevel: Int = 5,
    xp: Long = 0,
    level: Int = 1,
    content: @Composable ColumnScope.() -> Unit
) {
    val confidenceColor = when {
        confidenceLevel <= 3 -> MaterialTheme.colorScheme.error
        confidenceLevel <= 6 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            confidenceColor.copy(alpha = 0.1f)
        )
    )
    
    ModernCard(
        onClick = onClick,
        modifier = modifier,
        elevation = 8.dp,
        cornerRadius = 24.dp,
        useGradient = true,
        gradientBrush = gradientBrush,
        content = content
    )
}

@Composable
fun GlassMorphicCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "glass_card_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.6f)
                    )
                )
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    gradient: Brush = StudyGradients.primaryGradient
) {
    ModernCard(
        modifier = modifier,
        elevation = 6.dp,
        cornerRadius = 20.dp,
        useGradient = true,
        gradientBrush = gradient
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    icon()
                }
            }
        }
    }
}