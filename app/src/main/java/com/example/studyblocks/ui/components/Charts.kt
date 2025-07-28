@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyblocks.ui.theme.StudyGradients
import kotlin.math.*

@Composable
fun WeeklyCompletionChart(
    weeklyData: List<Triple<String, Int, Int>>, // Day name, total blocks, completion percentage
    modifier: Modifier = Modifier,
    animationDuration: Int = 1500
) {
    val maxPercentage = 100 // Always scale to 100%
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Use wrap content for better responsive behavior
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp) // Further reduced padding for narrow screens
    ) {
        Column {
            Text(
                text = "Weekly Completion Rate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Identify your most and least productive days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete study blocks to see your weekly patterns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp), // Add horizontal padding for better spacing
                    horizontalArrangement = Arrangement.SpaceEvenly, // Use SpaceEvenly for equal spacing
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { (day, totalBlocks, completionPercentage) ->
                        CompletionPercentageColumn(
                            label = day,
                            totalBlocks = totalBlocks,
                            completionPercentage = completionPercentage,
                            maxPercentage = maxPercentage,
                            animationDelay = weeklyData.indexOf(Triple(day, totalBlocks, completionPercentage)) * 100,
                            animationDuration = animationDuration,
                            modifier = Modifier.weight(1f, fill = false) // Add weight for responsive sizing
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyMinutesChart(
    dailyData: List<Triple<String, Int, Int>>, // Day name, scheduled minutes, completed minutes
    modifier: Modifier = Modifier,
    animationDuration: Int = 1500
) {
    val maxMinutes = dailyData.maxOfOrNull { maxOf(it.second, it.third) } ?: 1
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Scheduled vs Completed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Track your adherence to the study plan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dailyData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scheduled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyData.forEach { (day, scheduledMinutes, completedMinutes) ->
                        ComparisonBarColumn(
                            label = day,
                            scheduledValue = scheduledMinutes,
                            completedValue = completedMinutes,
                            maxValue = maxMinutes,
                            animationDelay = dailyData.indexOf(Triple(day, scheduledMinutes, completedMinutes)) * 100,
                            animationDuration = animationDuration
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompletionPercentageColumn(
    label: String,
    totalBlocks: Int,
    completionPercentage: Int,
    maxPercentage: Int,
    animationDelay: Int = 0,
    animationDuration: Int = 1000,
    modifier: Modifier = Modifier
) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (maxPercentage > 0) (completionPercentage.toFloat() / maxPercentage) else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "completion_height"
    )
    
    val animatedPercentage by animateIntAsState(
        targetValue = completionPercentage,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "completion_percentage"
    )
    
    // Determine color based on completion percentage
    val barColor = when {
        completionPercentage >= 80 -> Color(0xFF4CAF50) // Green for high completion
        completionPercentage >= 50 -> Color(0xFFFF9800) // Orange for medium completion
        completionPercentage > 0 -> Color(0xFFF44336) // Red for low completion
        else -> MaterialTheme.colorScheme.surfaceVariant // Gray for no data
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.widthIn(min = 32.dp, max = 48.dp) // Responsive width based on available space
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f) // Use percentage of column width for responsiveness
                .height(120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
            )
            
            // Completion percentage bar
            if (totalBlocks > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeight)
                        .background(
                            barColor,
                            RoundedCornerShape(6.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show completion percentage
        if (totalBlocks > 0) {
            Text(
                text = "${animatedPercentage}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
            Text(
                text = "${totalBlocks} block${if (totalBlocks != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        } else {
            Text(
                text = "0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Day label - make it more prominent but smaller to fit
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp, // Reduced font size
            maxLines = 1
        )
    }
}

@Composable
private fun ComparisonBarColumn(
    label: String,
    scheduledValue: Int,
    completedValue: Int,
    maxValue: Int,
    animationDelay: Int = 0,
    animationDuration: Int = 1000
) {
    val animatedScheduledHeight by animateFloatAsState(
        targetValue = if (maxValue > 0) (scheduledValue.toFloat() / maxValue) else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "scheduled_height"
    )
    
    val animatedCompletedHeight by animateFloatAsState(
        targetValue = if (maxValue > 0) (completedValue.toFloat() / maxValue) else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay + 200,
            easing = FastOutSlowInEasing
        ),
        label = "completed_height"
    )
    
    val animatedScheduledValue by animateIntAsState(
        targetValue = scheduledValue,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "scheduled_value"
    )
    
    val animatedCompletedValue by animateIntAsState(
        targetValue = completedValue,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay + 200,
            easing = FastOutSlowInEasing
        ),
        label = "completed_value"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(45.dp) // Reduced width to fit all 7 days
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f) // Use percentage of column width for responsiveness
                .height(120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Scheduled bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(animatedScheduledHeight)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 0.dp)
                        )
                )
                
                // Completed bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(animatedCompletedHeight)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(topStart = 0.dp, topEnd = 4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show completion percentage if scheduled > 0
        if (scheduledValue > 0) {
            val completionPercentage = (completedValue * 100) / scheduledValue
            Text(
                text = "${completionPercentage}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    completionPercentage >= 100 -> MaterialTheme.colorScheme.secondary
                    completionPercentage >= 70 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        } else {
            Text(
                text = "${animatedCompletedValue}m",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp, // Smaller font for better fit
            maxLines = 1
        )
    }
}

@Composable
private fun AnimatedBarColumn(
    label: String,
    value: Int,
    maxValue: Int,
    animationDelay: Int = 0,
    animationDuration: Int = 1000
) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (maxValue > 0) (value.toFloat() / maxValue) else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "bar_height"
    )
    
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "bar_value"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
            
            // Animated value bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${animatedValue}m",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp, // Smaller font for better fit
            maxLines = 1
        )
    }
}

@Composable
fun ProductivityHeatmap(
    hourlyData: Map<Int, Double>, // Hour to productivity score
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Use wrap content for better responsive behavior
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp) // Further reduced padding for narrow screens
    ) {
        Column {
            Text(
                text = "Productivity Heatmap",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "All hours (0-23) showing productivity patterns",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (hourlyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete study blocks to see your productivity patterns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                HeatmapGrid(hourlyData)
            }
        }
    }
}

@Composable
private fun HeatmapGrid(hourlyData: Map<Int, Double>) {
    val maxProductivity = hourlyData.values.maxOrNull() ?: 1.0
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Time periods covering complete 24-hour day (all hours 0-23 are displayed)
        val timePeriods = listOf(
            "Early Morning" to (0..5),   // Hours: 0, 1, 2, 3, 4, 5
            "Morning" to (6..11),        // Hours: 6, 7, 8, 9, 10, 11
            "Afternoon" to (12..17),     // Hours: 12, 13, 14, 15, 16, 17
            "Evening" to (18..23)        // Hours: 18, 19, 20, 21, 22, 23
        )
        
        timePeriods.forEach { (period, hours) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp), // Reduced spacing for narrow screens
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelSmall, // Smaller text for responsiveness
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp) // Fixed width to prevent squishing
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp), // Reduced spacing between cells
                    modifier = Modifier.weight(1f) // Take up remaining space
                ) {
                    hours.forEach { hour ->
                        val productivity = hourlyData[hour] ?: 0.0
                        val intensity = if (maxProductivity > 0) productivity / maxProductivity else 0.0
                        
                        HeatmapCell(
                            hour = hour,
                            intensity = intensity.toFloat(),
                            productivity = productivity,
                            modifier = Modifier.weight(1f) // Make cells fill available space evenly
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Legend
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.2f + (index * 0.2f)
                                ),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeatmapCell(
    hour: Int,
    intensity: Float,
    productivity: Double,
    modifier: Modifier = Modifier
) {
    val timeString = when {
        hour == 0 -> "12 AM"
        hour < 12 -> "${hour} AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
    
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text("$timeString: ${productivity.toInt()} blocks")
            }
        },
        state = rememberTooltipState()
    ) {
        Box(
            modifier = modifier
                .aspectRatio(1f) // Maintain square aspect ratio
                .sizeIn(minWidth = 20.dp, minHeight = 20.dp, maxWidth = 36.dp, maxHeight = 36.dp) // Responsive sizing
                .background(
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f + (intensity * 0.7f)
                    ),
                    RoundedCornerShape(4.dp) // Smaller corner radius for smaller cells
                )
                .padding(1.dp), // Reduced padding
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hour.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp, // Slightly larger for better readability
                color = if (intensity > 0.4f) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium // Consistent medium weight for clarity
            )
        }
    }
}

@Composable
fun SubjectDistributionChart(
    subjectData: List<Pair<String, Int>>, // Subject name to study time in minutes
    modifier: Modifier = Modifier
) {
    val totalMinutes = subjectData.sumOf { it.second }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp) // Reduced padding for better space utilization
    ) {
        Column {
            Text(
                text = "Subject Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (subjectData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Donut chart
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            data = subjectData,
                            totalValue = totalMinutes
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${totalMinutes / 60}h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjectData.forEach { (subject, minutes) ->
                            val percentage = if (totalMinutes > 0) (minutes * 100) / totalMinutes else 0
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            getSubjectColor(subjectData.indexOf(subject to minutes)),
                                            CircleShape
                                        )
                                )
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${minutes}m ($percentage%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<Pair<String, Int>>,
    totalValue: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        val strokeWidth = 24.dp.toPx()
        
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, value) ->
            val sweepAngle = if (totalValue > 0) {
                (value.toFloat() / totalValue) * 360f * animatedProgress
            } else 0f
            
            drawArc(
                color = getSubjectColor(index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
            
            startAngle += sweepAngle
        }
    }
}

private fun getSubjectColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF00BCD4), // Cyan
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[index % colors.size]
}

@Composable
fun InsightCard(
    title: String,
    description: String,
    icon: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onActionClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}