package com.example.studyblocks.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.studyblocks.ui.theme.StudyGradients

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.5f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        ),
        start = Offset.Zero,
        end = Offset(1000f, 1000f)
    )
    
    Box(
        modifier = modifier.background(shimmerBrush)
    ) {
        content()
    }
}

@Composable
fun StudyBlockCardSkeleton(
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon skeleton
            ShimmerEffect(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content skeleton
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Status indicator skeleton
            ShimmerEffect(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun SubjectCardSkeleton(
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 8.dp,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon skeleton
            ShimmerEffect(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content skeleton
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Duration skeleton
                    ShimmerEffect(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    
                    // Level skeleton
                    ShimmerEffect(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Confidence indicator skeleton
            ShimmerEffect(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun StatisticCardSkeleton(
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        elevation = 6.dp,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Value skeleton  
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Subtitle skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            // Icon skeleton
            ShimmerEffect(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun TodayScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date picker skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(7) { index ->
                ShimmerEffect(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats row skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                StatisticCardSkeleton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Study blocks skeleton
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(5) {
                StudyBlockCardSkeleton()
            }
        }
    }
}

@Composable
fun SubjectsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Schedule generation card skeleton
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 8.dp,
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
                
                ShimmerEffect(
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Subjects list skeleton
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) {
                SubjectCardSkeleton()
            }
        }
    }
}

@Composable
fun AnalyticsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Global XP card skeleton
        ModernCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 12.dp,
            cornerRadius = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerEffect(
                            modifier = Modifier
                                .width(80.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        ShimmerEffect(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats row skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                StatisticCardSkeleton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Chart skeleton
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = 6.dp,
            cornerRadius = 16.dp
        ) {
            ShimmerEffect(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun PulsingDot(
    color: Color = MaterialTheme.colorScheme.primary,
    size: androidx.compose.ui.unit.Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
            .scale(scale)
    )
}

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )
    
    CircularProgressIndicator(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation
        },
        color = color,
        strokeWidth = 3.dp
    )
}