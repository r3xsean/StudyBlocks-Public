package com.example.studyblocks.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studyblocks.data.model.SubjectGrouping
import com.example.studyblocks.repository.SchedulingResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSubjectGroupingScreen(
    navController: NavController,
    initialGrouping: SubjectGrouping = SubjectGrouping.BALANCED,
    onGroupingSelected: (SubjectGrouping) -> Unit,
    onNext: () -> Unit
) {
    var subjectGrouping by remember { mutableStateOf(initialGrouping) }
    val scrollState = rememberScrollState()
    
    // Update parent whenever value changes
    LaunchedEffect(subjectGrouping) {
        onGroupingSelected(subjectGrouping)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ViewModule,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Subject Grouping",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "How should we organize your subjects throughout the week? Different patterns work better for different learning styles.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Grouping Options
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(SubjectGrouping.values()) { grouping ->
                GroupingOptionCard(
                    grouping = grouping,
                    isSelected = grouping == subjectGrouping,
                    onClick = { subjectGrouping = grouping }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Detailed explanation of selected grouping
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = subjectGrouping.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = subjectGrouping.description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Visual pattern example
                Text(
                    text = "Daily Pattern Example:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SubjectPatternVisualization(
                    grouping = subjectGrouping,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Benefits/drawbacks
                when (subjectGrouping) {
                    SubjectGrouping.MOST_GROUPED -> {
                        BenefitsList(
                            benefits = listOf(
                                "✅ Deep focus on one subject at a time",
                                "✅ Easier to get into 'flow state'",
                                "✅ Better for complex, interconnected topics"
                            ),
                            considerations = listOf(
                                "⚠️ Less variety in daily routine",
                                "⚠️ Risk of mental fatigue with same subject"
                            )
                        )
                    }
                    SubjectGrouping.BALANCED -> {
                        BenefitsList(
                            benefits = listOf(
                                "✅ Good variety keeps you engaged",
                                "✅ Balanced cognitive load",
                                "✅ Works well for most learning styles",
                                "✅ Prevents subject-specific burnout"
                            ),
                            considerations = listOf(
                                "⚠️ May require more context switching"
                            )
                        )
                    }
                    SubjectGrouping.LEAST_GROUPED -> {
                        BenefitsList(
                            benefits = listOf(
                                "✅ Maximum variety and stimulation",
                                "✅ Excellent for attention span issues",
                                "✅ Natural spaced repetition effect"
                            ),
                            considerations = listOf(
                                "⚠️ Frequent context switching",
                                "⚠️ May feel scattered for some learners"
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weekly overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📅 Weekly Overview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show 3 days worth of examples
                val subjects = listOf("📚", "🧮", "🧪", "🌍", "🎨")
                val patterns = generateWeeklyPatterns(subjectGrouping, subjects, 3)
                
                patterns.forEachIndexed { dayIndex, dayPattern ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day ${dayIndex + 1}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.width(50.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dayPattern.forEach { subject ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subject,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    if (dayIndex < patterns.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Complete Setup")
            }
        }
        
        // Progress indicator
        Text(
            text = "Step 6 of 7",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun GroupingOptionCard(
    grouping: SubjectGrouping,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (grouping) {
        SubjectGrouping.MOST_GROUPED -> Icons.Default.LinearScale
        SubjectGrouping.BALANCED -> Icons.Default.ViewModule
        SubjectGrouping.LEAST_GROUPED -> Icons.Default.Shuffle
    }
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (grouping) {
                        SubjectGrouping.MOST_GROUPED -> "Most"
                        SubjectGrouping.BALANCED -> "Balanced"
                        SubjectGrouping.LEAST_GROUPED -> "Least"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Grouped",
                    fontSize = 12.sp,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Mini pattern preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val pattern = when (grouping) {
                    SubjectGrouping.MOST_GROUPED -> listOf("📚", "📚", "📚")
                    SubjectGrouping.BALANCED -> listOf("📚", "🧮", "🧪")
                    SubjectGrouping.LEAST_GROUPED -> listOf("📚", "🧮", "🧪")
                }
                
                pattern.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectPatternVisualization(
    grouping: SubjectGrouping,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pattern = when (grouping) {
            SubjectGrouping.MOST_GROUPED -> listOf("📚", "📚", "📚", "🧮", "🧮")
            SubjectGrouping.BALANCED -> listOf("📚", "🧮", "🧪", "📚", "🧮")
            SubjectGrouping.LEAST_GROUPED -> listOf("📚", "🧮", "🧪", "🌍", "🎨")
        }
        
        pattern.forEachIndexed { index, subject ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject,
                    fontSize = 16.sp
                )
            }
            
            if (index < pattern.size - 1) {
                Text(
                    text = "→",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun BenefitsList(
    benefits: List<String>,
    considerations: List<String>
) {
    Column {
        benefits.forEach { benefit ->
            Text(
                text = benefit,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
        
        if (considerations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            considerations.forEach { consideration ->
                Text(
                    text = consideration,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun generateWeeklyPatterns(
    grouping: SubjectGrouping,
    subjects: List<String>,
    days: Int
): List<List<String>> {
    val blocksPerDay = 3
    return (0 until days).map { dayIndex ->
        when (grouping) {
            SubjectGrouping.MOST_GROUPED -> {
                val subjectIndex = dayIndex % subjects.size
                List(blocksPerDay) { subjects[subjectIndex] }
            }
            SubjectGrouping.BALANCED -> {
                (0 until blocksPerDay).map { blockIndex ->
                    subjects[(dayIndex + blockIndex) % subjects.size]
                }
            }
            SubjectGrouping.LEAST_GROUPED -> {
                (0 until blocksPerDay).map { blockIndex ->
                    subjects[(dayIndex * blocksPerDay + blockIndex) % subjects.size]
                }
            }
        }
    }
}

@Composable
fun OnboardingScheduleResultDialog(
    result: SchedulingResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Created! 🎉") },
        text = {
            Column {
                Text("Your personalized study schedule has been generated:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("✅ ${result.totalBlocks} study blocks created")
                Text("📅 Distributed over ${result.scheduleHorizon} days")
                
                if (result.subjectDistribution.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📚 Subject breakdown:",
                        fontWeight = FontWeight.Medium
                    )
                    result.subjectDistribution.forEach { (subject, count) ->
                        Text("• $subject: $count blocks", fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You're ready to start your personalized learning journey! 🚀",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Let's Study!")
            }
        }
    )
}