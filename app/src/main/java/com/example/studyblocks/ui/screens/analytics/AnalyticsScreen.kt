package com.example.studyblocks.ui.screens.analytics

import android.graphics.Color
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.StudyStreak
import com.example.studyblocks.data.model.XPDataPoint
import com.example.studyblocks.repository.LevelPrediction
import com.example.studyblocks.ui.components.ModernCard
import com.example.studyblocks.ui.components.StatisticCard
import com.example.studyblocks.ui.components.XPProgressBar
import com.example.studyblocks.ui.components.StudyMinutesChart
import com.example.studyblocks.ui.components.WeeklyCompletionChart
import com.example.studyblocks.ui.components.ProductivityHeatmap
import com.example.studyblocks.ui.components.SubjectDistributionChart
import com.example.studyblocks.ui.components.InsightCard
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.theme.XPGold
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val xpProgression by viewModel.xpProgression.collectAsState()
    val detailedXPData by viewModel.detailedXPData.collectAsState()
    val xpPerSubject by viewModel.xpPerSubject.collectAsState()
    val studyOverview by viewModel.studyOverview.collectAsState()
    val globalStats by viewModel.globalStats.collectAsState()
    val subjectAnalytics by viewModel.subjectAnalytics.collectAsState()
    val confidencePerformance by viewModel.confidencePerformance.collectAsState()
    val productiveHours by viewModel.productiveHours.collectAsState()
    val studyStreak by viewModel.studyStreak.collectAsState()
    val levelPredictions by viewModel.levelPredictions.collectAsState()
    val weeklyCompletionData by viewModel.weeklyCompletionData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient Top App Bar matching other screens
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = StudyGradients.primaryGradient
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Analytics",
                            style = StudyBlocksTypography.screenTitle.copy(fontSize = 28.sp),
                            color = androidx.compose.ui.graphics.Color.White,
                            maxLines = 2
                        )
                        Text(
                            text = "Your learning journey",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }

            // Body content with modern spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Global XP Summary with integrated stats
                    item {
                        ModernGlobalXPCard(globalStats, studyOverview)
                    }

                    // Weekly Completion Chart
                    item {
                        WeeklyCompletionChart(
                            weeklyData = weeklyCompletionData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Productivity Heatmap
                    item {
                        ProductivityHeatmap(
                            hourlyData = productiveHours,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Subject Distribution Chart
                    item {
                        val subjectData = subjectAnalytics.map { subject ->
                            subject.name to subject.totalMinutesStudied
                        }
                        SubjectDistributionChart(
                            subjectData = subjectData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Study Streak
                    item {
                        StudyStreakCard(studyStreak)
                    }

                    
                    // Level Predictions
                    item {
                        LevelPredictionsCard(levelPredictions)
                    }
                }
            }
        }
    }
}

// Removed old GlobalXPCard - using ModernGlobalXPCard instead


@Composable
private fun SubjectAnalyticsSection(subjectAnalytics: List<SubjectAnalyticsData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Subject Progress",
                style = StudyBlocksTypography.subjectTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (subjectAnalytics.isEmpty()) {
                Text(
                    text = "No subject data available yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    subjectAnalytics.forEach { subject ->
                        SubjectAnalyticsCard(subject)
                    }
                }
            }
        }
    }
}

// Removed old SubjectXPDistribution - using SubjectAnalyticsSection instead

// Removed old SubjectXPItem - using SubjectAnalyticsCard instead

@Composable
private fun ModernLearningInsights(subjectAnalytics: List<SubjectAnalyticsData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Learning Insights",
                style = StudyBlocksTypography.subjectTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (subjectAnalytics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🧠",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add subjects and start studying to see insights!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val highConfidenceSubjects = subjectAnalytics.filter { it.confidence >= 8 }
                val lowConfidenceSubjects = subjectAnalytics.filter { it.confidence <= 3 }
                val averageConfidence = subjectAnalytics.map { it.confidence }.average()
                val mostStudiedSubject = subjectAnalytics.maxByOrNull { it.totalMinutesStudied }
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Confidence Distribution
                    Column {
                        Text(
                            text = "Confidence Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${highConfidenceSubjects.size}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = androidx.compose.ui.graphics.Color(0xFF66BB6A),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "High Confidence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%.1f", averageConfidence),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Average Score",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${lowConfidenceSubjects.size}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = androidx.compose.ui.graphics.Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Need Focus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Key Insights
                    if (mostStudiedSubject != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🏆",
                                    fontSize = 24.sp
                                )
                                Column {
                                    Text(
                                        text = "Most Dedicated: ${mostStudiedSubject.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${mostStudiedSubject.totalMinutesStudied / 60}h ${mostStudiedSubject.totalMinutesStudied % 60}m studied",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Recommendations
                    if (lowConfidenceSubjects.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Color(0xFFEF5350).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🎯",
                                    fontSize = 24.sp
                                )
                                Column {
                                    Text(
                                        text = "Focus Opportunity",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Consider spending more time on ${lowConfidenceSubjects.first().name}",
                                        style = MaterialTheme.typography.bodySmall,
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
private fun InsightItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubjectAnalyticsCard(subject: SubjectAnalyticsData) {
    val confidenceColor = when (subject.confidence) {
        in 1..3 -> androidx.compose.ui.graphics.Color(0xFFEF5350) // Red for low confidence
        in 4..6 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange for medium confidence  
        in 7..8 -> androidx.compose.ui.graphics.Color(0xFF66BB6A) // Green for good confidence
        else -> androidx.compose.ui.graphics.Color(0xFF42A5F5) // Blue for excellent confidence
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                confidenceColor.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        confidenceColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.icon,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Subject Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = subject.name,
                        style = StudyBlocksTypography.subjectTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Level ${subject.level}",
                        style = MaterialTheme.typography.labelMedium,
                        color = confidenceColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // XP Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${subject.totalXP} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Study time Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.totalMinutesStudied / 60}h ${subject.totalMinutesStudied % 60}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Confidence indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(confidenceColor, CircleShape)
                        )
                        Text(
                            text = "${subject.confidence}/10",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun XPSummaryItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Removed period functions - analytics are now all-time focused

@Composable
private fun ModernGlobalXPCard(globalStats: GlobalStats, studyOverview: StudyOverview) {
    val globalXP = globalStats.globalXP.toLong()
    val globalLevel = globalStats.globalLevel
    val totalHours = studyOverview.totalHours
    val subjectCount = studyOverview.subjects
    // Calculate XP for next level based on the exponential formula
    val xpForNextLevel = (200 * ((globalLevel * 1.8).pow(1.3))).toLong()
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        useGradient = true,
        gradientBrush = StudyGradients.xpGradient,
        cornerRadius = 24.dp,
        elevation = 12.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // XP Icon and Level
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐",
                        style = MaterialTheme.typography.headlineLarge,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Level $globalLevel",
                        style = StudyBlocksTypography.levelDisplay,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "Global Rank",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // XP Progress
            XPProgressBar(
                currentXP = globalXP,
                targetXP = xpForNextLevel,
                level = globalLevel,
                showXPNumbers = true,
                animationDuration = 2000
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Study Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${totalHours}h",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Study Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$subjectCount",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Subjects",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${globalStats.streakDays}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Day Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Removed bulky scrollable stats row - integrated into GlobalXPCard

@Composable
private fun MostProductiveHoursCard(productiveHours: Map<Int, Double>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⏰",
                    fontSize = 24.sp
                )
                Text(
                    text = "Most Productive Hours",
                    style = StudyBlocksTypography.subjectTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (productiveHours.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete study blocks to see your most productive hours!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val topHours = productiveHours.entries.sortedByDescending { it.value }.take(3)
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topHours.forEachIndexed { index, (hour, productivity) ->
                        val timeString = when {
                            hour == 0 -> "12:00 AM"
                            hour < 12 -> "${hour}:00 AM"
                            hour == 12 -> "12:00 PM"
                            else -> "${hour - 12}:00 PM"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            when (index) {
                                                0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                                1 -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                                                else -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
                                            }.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (index) {
                                            0 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                            1 -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                                            else -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
                                        }
                                    )
                                }
                                
                                Text(
                                    text = timeString,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Text(
                                text = "${productivity.toInt()} blocks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyStreakCard(studyStreak: StudyStreak) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🔥",
                    fontSize = 24.sp
                )
                Text(
                    text = "Study Streak",
                    style = StudyBlocksTypography.subjectTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${studyStreak.currentStreak}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${studyStreak.longestStreak}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Longest Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (studyStreak.currentStreak > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Color(0xFFFF5722).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🎉 Keep it up! You're on a ${studyStreak.currentStreak}-day streak!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleOptimizationCard(productiveHours: Map<Int, Double>, subjectAnalytics: List<SubjectAnalyticsData>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp
                )
                Text(
                    text = "Schedule Optimization",
                    style = StudyBlocksTypography.subjectTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (productiveHours.isEmpty() || subjectAnalytics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete more study sessions to get AI-powered insights!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val bestHour = productiveHours.maxByOrNull { it.value }?.key ?: 9
                val timeString = when {
                    bestHour == 0 -> "12:00 AM"
                    bestHour < 12 -> "${bestHour}:00 AM" 
                    bestHour == 12 -> "12:00 PM"
                    else -> "${bestHour - 12}:00 PM"
                }
                
                val needsFocusSubject = subjectAnalytics.filter { it.confidence <= 4 }.firstOrNull()
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Your most productive time is around $timeString - aim to study during these hours!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (needsFocusSubject != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "📚 ${needsFocusSubject.name} has low confidence - focus extra attention during study sessions!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelPredictionsCard(levelPredictions: Map<String, LevelPrediction>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp
                )
                Text(
                    text = "Level-Up Predictions",
                    style = StudyBlocksTypography.subjectTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (levelPredictions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Generate a new schedule to see level predictions!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Complete your current schedule to reach these levels:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    levelPredictions.values.sortedByDescending { it.xpGain }.forEach { prediction ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = prediction.subjectName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Level ${prediction.currentLevel} → ${String.format("%.1f", prediction.predictedLevel)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "+${prediction.xpGain} XP",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    )
                                    
                                    val levelGain = prediction.predictedLevel - prediction.currentLevel
                                    if (levelGain >= 1.0f) {
                                        Text(
                                            text = "🎉 +${String.format("%.1f", levelGain)} levels!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
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
}


