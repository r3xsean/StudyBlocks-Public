package com.example.studyblocks.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()
    val subjectBreakdown by viewModel.subjectBreakdown.collectAsState()
    val overallStats by viewModel.overallStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showPeriodSelector by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Analytics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            actions = {
                FilterChip(
                    onClick = { showPeriodSelector = true },
                    label = { Text(selectedPeriod.displayName) },
                    selected = false,
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                )
                
                DropdownMenu(
                    expanded = showPeriodSelector,
                    onDismissRequest = { showPeriodSelector = false }
                ) {
                    AnalyticsPeriod.values().forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period.displayName) },
                            onClick = {
                                viewModel.selectPeriod(period)
                                showPeriodSelector = false
                            },
                            leadingIcon = {
                                if (selectedPeriod == period) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.height(64.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Statistics
            item {
                OverallStatsCard(overallStats)
            }
            
            // Weekly Progress Chart
            if (weeklyProgress.isNotEmpty()) {
                item {
                    WeeklyProgressCard(
                        weeklyProgress = weeklyProgress,
                        period = selectedPeriod
                    )
                }
            }
            
            // Subject Breakdown
            if (subjectBreakdown.isNotEmpty()) {
                item {
                    SubjectBreakdownCard(subjectBreakdown)
                }
            }
            
            // Streak Information
            item {
                StreakCard(
                    currentStreak = overallStats.currentStreak,
                    longestStreak = overallStats.longestStreak
                )
            }
        }
    }
}

@Composable
fun OverallStatsCard(stats: OverallStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = String.format("%.1f", stats.hoursStudied),
                    label = "Hours Studied",
                    icon = Icons.Default.AccessTime
                )
                
                StatItem(
                    value = stats.totalBlocksCompleted.toString(),
                    label = "Blocks Completed",
                    icon = Icons.Default.CheckCircle
                )
                
                StatItem(
                    value = String.format("%.0f", stats.averageSessionDuration),
                    label = "Avg Session (min)",
                    icon = Icons.Default.Assessment
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyProgressCard(
    weeklyProgress: List<DailyProgress>,
    period: AnalyticsPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${period.displayName} Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                val totalCompleted = weeklyProgress.sumOf { it.completedBlocks }
                val totalBlocks = weeklyProgress.sumOf { it.totalBlocks }
                val overallRate = if (totalBlocks > 0) (totalCompleted * 100 / totalBlocks) else 0
                
                Text(
                    text = "$overallRate% completed",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart
            SimpleBarChart(
                data = weeklyProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxValue = data.maxOfOrNull { it.completedBlocks }?.toFloat() ?: 1f
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / data.size * 0.7f
        val spacing = size.width / data.size * 0.3f
        
        data.forEachIndexed { index, progress ->
            val barHeight = if (maxValue > 0) {
                (progress.completedBlocks / maxValue) * size.height * 0.8f
            } else 0f
            
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            drawRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            
            // Day label - simplified without native canvas
            // Note: For production use, consider using androidx.compose.ui.text.drawText or similar
        }
    }
}

@Composable
fun SubjectBreakdownCard(subjectBreakdown: List<SubjectProgress>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Subject Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            subjectBreakdown.forEach { progress ->
                SubjectProgressItem(progress)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SubjectProgressItem(progress: SubjectProgress) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.completionRate / 100f
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progress.subject.icon,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Column {
                    Text(
                        text = progress.subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${progress.completedBlocks}/${progress.totalBlocks} blocks • ${progress.totalMinutes}min",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "${progress.completionRate}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StreakItem(
                value = currentStreak,
                label = "Current Streak",
                icon = "🔥"
            )
            
            StreakItem(
                value = longestStreak,
                label = "Best Streak",
                icon = "🏆"
            )
        }
    }
}

@Composable
fun StreakItem(
    value: Int,
    label: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (value == 1) "day" else "days",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}