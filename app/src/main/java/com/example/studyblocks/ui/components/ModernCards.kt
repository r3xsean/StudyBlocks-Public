package com.example.studyblocks.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.theme.StudyBlocksTypography

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
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    elevation: Dp = 8.dp,
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

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation * 0.7f else elevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "glass_card_elevation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        Color.Transparent
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
    gradient: Brush = StudyGradients.primaryGradient,
    isGlassmorphic: Boolean = false
) {
    if (isGlassmorphic) {
        GlassMorphicCard(
            modifier = modifier,
            cornerRadius = 20.dp
        ) {
            StatisticCardContent(
                title = title,
                value = value,
                subtitle = subtitle,
                icon = icon
            )
        }
    } else {
        ModernCard(
            modifier = modifier,
            elevation = 6.dp,
            cornerRadius = 20.dp,
            useGradient = true,
            gradientBrush = gradient
        ) {
            StatisticCardContent(
                title = title,
                value = value,
                subtitle = subtitle,
                icon = icon
            )
        }
    }
}

@Composable
private fun StatisticCardContent(
    title: String,
    value: String,
    subtitle: String?,
    icon: @Composable (() -> Unit)?
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

// New modern components inspired by screenshots

@Composable
fun PillSegmentedControl(
    selectedIndex: Int,
    options: List<String>,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val interactionSources = remember { options.map { MutableInteractionSource() } }
    
    Row(
        modifier = modifier
            .background(
                backgroundColor,
                RoundedCornerShape(25.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val animatedBackgroundColor by animateColorAsState(
                targetValue = if (isSelected) selectedColor else Color.Transparent,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "pill_background"
            )
            
            val animatedTextColor by animateColorAsState(
                targetValue = if (isSelected) backgroundColor else unselectedColor,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "pill_text"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        animatedBackgroundColor,
                        RoundedCornerShape(21.dp)
                    )
                    .clickable(
                        interactionSource = interactionSources[index],
                        indication = null,
                        onClick = { onSelectionChange(index) }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = StudyBlocksTypography.pillButtonText,
                    color = animatedTextColor
                )
            }
        }
    }
}

@Composable
fun FloatingStudyCard(
    title: String,
    duration: String,
    subject: String,
    subjectIcon: String? = null, // Add subject icon parameter
    isCompleted: Boolean = false,
    isPending: Boolean = false,
    isOverdue: Boolean = false,
    onClick: (androidx.compose.ui.geometry.Offset) -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardColor = when {
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        isOverdue -> MaterialTheme.colorScheme.errorContainer
        isPending -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    var cardCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    
    // Subtle animation states for professional, calm feel
    val animatedScale by animateFloatAsState(
        targetValue = if (isCompleted) 0.98f else 1f, // Less dramatic scale change
        animationSpec = tween(
            durationMillis = 300, // Slower, more gentle animation
            easing = FastOutSlowInEasing
        ),
        label = "card_scale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.85f else 1f, // Less dramatic alpha change
        animationSpec = tween(
            durationMillis = 300, // Consistent timing with scale
            easing = FastOutSlowInEasing
        ),
        label = "card_alpha"
    )
    
    GlassMorphicCard(
        onClick = null, // Remove simple click from GlassMorphicCard
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
            .onGloballyPositioned { cardCoords = it }
            .pointerInput(isCompleted, isPending, isOverdue) { // Add deps to ensure recomposition
                detectTapGestures(
                    onTap = { tapOffset ->
                        val rootPos = cardCoords?.positionInRoot() ?: androidx.compose.ui.geometry.Offset.Zero
                        val globalTapPos = rootPos + tapOffset
                        onClick(globalTapPos)
                    },
                    onLongPress = { onLongPress?.invoke() }
                )
            },
        backgroundColor = cardColor.copy(alpha = 0.9f),
        cornerRadius = 16.dp,
        elevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject icon with animated background
            if (subjectIcon != null) {
                val iconBackgroundColor by animateColorAsState(
                    targetValue = when {
                        isCompleted -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        isPending -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    ),
                    label = "icon_background_color"
                )
                
                val iconScale by animateFloatAsState(
                    targetValue = if (isCompleted) 1.05f else 1f, // Less dramatic scale
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    ),
                    label = "icon_scale"
                )
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale)
                        .background(iconBackgroundColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subjectIcon,
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subject,
                    style = StudyBlocksTypography.cardSubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = StudyBlocksTypography.cardTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = duration,
                    style = StudyBlocksTypography.microLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Enhanced status indicator with faster animations
            val statusColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> MaterialTheme.colorScheme.secondary
                    isOverdue -> MaterialTheme.colorScheme.error
                    isPending -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                },
                animationSpec = tween(
                    durationMillis = 200, // Reduced from 300ms for snappier response
                    easing = FastOutSlowInEasing
                ),
                label = "status_color"
            )
            
            val statusSize by animateDpAsState(
                targetValue = if (isCompleted) 14.dp else 12.dp, // Less dramatic size change
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "status_size"
            )
            
            Box(
                modifier = Modifier
                    .size(statusSize) // Use animated size for smooth transition
                    .background(statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        modifier = Modifier.size(10.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRingCard(
    title: String,
    progress: Float,
    progressText: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    ringSize: Dp = 80.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    isFullWidth: Boolean = false,
    animationDuration: Int = 1000
) {
    // Smooth animated progress with subtle, professional feel
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 600, // Reduced from bounce to simple tween with shorter duration
            easing = FastOutSlowInEasing
        ),
        label = "progress_animation"
    )
    
    // Animated progress text for smooth number changes
    val animatedProgressInt by animateIntAsState(
        targetValue = (progress * 100).toInt(),
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "progress_text_animation"
    )
    GlassMorphicCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        elevation = 8.dp
    ) {
        if (isFullWidth) {
            // Full width layout for analytics global progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = StudyBlocksTypography.cardTitle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = StudyBlocksTypography.cardSubtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Background ring
                    Canvas(
                        modifier = Modifier.size(ringSize)
                    ) {
                        drawCircle(
                            color = backgroundColor,
                            radius = size.minDimension / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                        )
                        // Animated progress ring
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                    
                    Text(
                        text = if (progressText.contains("%")) "${animatedProgressInt}%" else progressText,
                        style = StudyBlocksTypography.statisticsNumber.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // Compact layout for other uses
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = StudyBlocksTypography.cardTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Background ring
                    Canvas(
                        modifier = Modifier.size(ringSize)
                    ) {
                        drawCircle(
                            color = backgroundColor,
                            radius = size.minDimension / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                        )
                        // Animated progress ring
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                    
                    Text(
                        text = if (progressText.contains("%")) "${animatedProgressInt}%" else progressText,
                        style = StudyBlocksTypography.statisticsNumber.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = StudyBlocksTypography.cardSubtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMetricCard(
    value: String,
    label: String,
    trend: String? = null,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    gradient: Brush = StudyGradients.glassPrimaryGradient
) {
    GlassMorphicCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        elevation = 6.dp
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    style = StudyBlocksTypography.statisticsLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                gradient,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(4.dp)
                    ) {
                        icon()
                    }
                }
            }
            
            Text(
                text = value,
                style = StudyBlocksTypography.statisticsNumberLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (trend != null) {
                Text(
                    text = trend,
                    style = StudyBlocksTypography.microLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CelebrationCard(
    title: String,
    subtitle: String,
    celebrationText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudyGradients.celebrationGradient)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = celebrationText,
                    style = StudyBlocksTypography.celebrationTitle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = title,
                    style = StudyBlocksTypography.welcomeTitle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = subtitle,
                    style = StudyBlocksTypography.cardSubtitle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = StudyBlocksTypography.pillButtonText
                    )
                }
            }
        }
    }
}