package com.example.studyblocks.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studyblocks.repository.SchedulingResult
import com.example.studyblocks.data.model.SubjectGrouping

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    navController: NavController,
    currentPreferredBlocks: Int,
    onGenerate: (blocksPerWeekday: Int, blocksPerWeekend: Int, horizon: Int, blockDuration: Int, grouping: SubjectGrouping) -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    // Get current user and schedule preferences
    val currentUser by viewModel.currentUser.collectAsState()
    val schedulePreferences by viewModel.schedulePreferences.collectAsState()
    
    // Use saved preferences as defaults, falling back to hardcoded values if none exist
    var selectedBlocksWeekday by remember { mutableStateOf(currentPreferredBlocks) }
    var selectedBlocksWeekend by remember { mutableStateOf(2) }
    var selectedHorizonWeeks by remember { mutableStateOf(3) }  
    var selectedBlockDuration by remember { mutableStateOf(60) }
    var selectedSubjectGrouping by remember { mutableStateOf(SubjectGrouping.BALANCED) }
    
    // Update state when preferences load
    LaunchedEffect(schedulePreferences) {
        schedulePreferences?.let { prefs ->
            selectedBlocksWeekday = prefs.blocksPerWeekday
            selectedBlocksWeekend = prefs.blocksPerWeekend
            selectedHorizonWeeks = prefs.scheduleHorizonWeeks
            selectedBlockDuration = prefs.defaultBlockDurationMinutes
            selectedSubjectGrouping = prefs.subjectGrouping
        }
    }
    
    val scrollState = rememberScrollState()
    
    // Watch for schedule result
    val scheduleResult by viewModel.scheduleResult.collectAsState()
    val isGeneratingSchedule by viewModel.isGeneratingSchedule.collectAsState()
    var showScheduleDialog by remember { mutableStateOf(false) }
    
    // Handle schedule result
    LaunchedEffect(scheduleResult) {
        if (scheduleResult != null) {
            showScheduleDialog = true
        }
    }
    
    // Calculate total study time
    val weekdayStudyTime = selectedBlocksWeekday * selectedBlockDuration
    val weekendStudyTime = selectedBlocksWeekend * selectedBlockDuration
    
    fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours == 0 -> "${mins}m"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Schedule Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Customize Your Schedule",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Configure your study schedule preferences to match your lifestyle and goals.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Schedule Horizon
            SchedulePreferenceCard(
                icon = Icons.Default.CalendarMonth,
                title = "Schedule Horizon",
                description = "How many weeks ahead to plan your study schedule",
                value = "$selectedHorizonWeeks weeks (${selectedHorizonWeeks * 7} days)"
            ) {
                Column {
                    Text(
                        text = "Weeks: $selectedHorizonWeeks (${selectedHorizonWeeks * 7} days)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(1, 2, 3, 4)) { weeks ->
                            FilterChip(
                                onClick = { selectedHorizonWeeks = weeks },
                                label = { 
                                    Text("${weeks}w")
                                },
                                selected = weeks == selectedHorizonWeeks,
                                modifier = Modifier.widthIn(min = 56.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = when (selectedHorizonWeeks) {
                            1 -> "Short-term planning: Quick starts but requires frequent schedule regeneration"
                            2 -> "Medium-term planning: Good balance between flexibility and convenience"
                            3 -> "Optimal planning: Recommended for spaced repetition effectiveness"
                            4 -> "Long-term planning: Maximum consistency but less adaptable to changes"
                            else -> "Custom planning period"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Block Duration
            SchedulePreferenceCard(
                icon = Icons.Default.Timer,
                title = "Block Duration",
                description = "Length of each study session",
                value = "${selectedBlockDuration} minutes"
            ) {
                Column {
                    Text(
                        text = "Duration: ${selectedBlockDuration} minutes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Slider(
                        value = selectedBlockDuration.toFloat(),
                        onValueChange = { 
                            selectedBlockDuration = (it.toInt() / 15) * 15 // Round to 15-minute intervals
                        },
                        valueRange = 15f..180f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "15 min",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "3 hours",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            selectedBlockDuration <= 30 -> "Quick sessions: Great for reviews and vocabulary"
                            selectedBlockDuration <= 45 -> "Standard sessions: Good for most subjects"
                            selectedBlockDuration <= 75 -> "Focused sessions: Ideal for deep learning"
                            selectedBlockDuration <= 120 -> "Extended sessions: Perfect for complex topics"
                            else -> "Marathon sessions: For intensive study periods"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Blocks per Weekday
            SchedulePreferenceCard(
                icon = Icons.Default.Schedule,
                title = "Weekday Study Blocks",
                description = "Number of study blocks for Monday-Friday",
                value = "$selectedBlocksWeekday blocks (${formatTime(weekdayStudyTime)} daily)"
            ) {
                Column {
                    Text(
                        text = "Blocks: $selectedBlocksWeekday (Total: ${formatTime(weekdayStudyTime)} per day)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..8).toList()) { blocks ->
                            FilterChip(
                                onClick = { selectedBlocksWeekday = blocks },
                                label = { Text("$blocks") },
                                selected = blocks == selectedBlocksWeekday,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = when (selectedBlocksWeekday) {
                            1 -> "Light schedule: Perfect for busy weekdays"
                            2 -> "Moderate schedule: Good work-study balance"
                            3 -> "Recommended: Optimal for consistent progress"
                            4 -> "Intensive schedule: High commitment level"
                            5 -> "Heavy schedule: Requires strong dedication"
                            6 -> "Very intensive weekdays - High commitment"
                            7 -> "Extreme weekday schedule - Maximum dedication"
                            8 -> "Ultimate weekday schedule - Peak performance"
                            else -> "Custom intensive weekdays"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Blocks per Weekend
            SchedulePreferenceCard(
                icon = Icons.Default.Weekend,
                title = "Weekend Study Blocks",
                description = "Number of study blocks for Saturday-Sunday",
                value = "$selectedBlocksWeekend blocks (${formatTime(weekendStudyTime)} daily)"
            ) {
                Column {
                    Text(
                        text = "Blocks: $selectedBlocksWeekend (Total: ${formatTime(weekendStudyTime)} per day)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((0..6).toList()) { blocks ->
                            FilterChip(
                                onClick = { selectedBlocksWeekend = blocks },
                                label = { Text("$blocks") },
                                selected = blocks == selectedBlocksWeekend,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = when (selectedBlocksWeekend) {
                            0 -> "Complete rest: Weekends off for relaxation"
                            1 -> "Light weekend study: Minimal commitment"
                            2 -> "Balanced weekends: Recommended approach"
                            3 -> "Active weekends: Accelerated learning"
                            4 -> "Intensive weekends: Maximum progress"
                            5 -> "Extreme weekend schedule: Maximum dedication"
                            6 -> "Ultimate weekend schedule: Peak performance"
                            else -> "Custom intensive weekends"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subject Grouping
            SchedulePreferenceCard(
                icon = Icons.Default.ViewModule,
                title = "Subject Grouping",
                description = "How to organize subjects throughout your schedule",
                value = selectedSubjectGrouping.displayName
            ) {
                Column {
                    Text(
                        text = "Grouping: ${selectedSubjectGrouping.displayName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SubjectGrouping.values()) { grouping ->
                            FilterChip(
                                onClick = { selectedSubjectGrouping = grouping },
                                label = { 
                                    Text(
                                        text = when (grouping) {
                                            SubjectGrouping.MOST_GROUPED -> "Most"
                                            SubjectGrouping.BALANCED -> "Balanced"
                                            SubjectGrouping.LEAST_GROUPED -> "Least"
                                            else -> "Unknown"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                selected = grouping == selectedSubjectGrouping,
                                modifier = Modifier.widthIn(min = 70.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = selectedSubjectGrouping.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Visual examples
                    Text(
                        text = when (selectedSubjectGrouping) {
                            SubjectGrouping.MOST_GROUPED -> "Example: 📚📚📚 🧮🧮🧮 🧪🧪🧪"
                            SubjectGrouping.BALANCED -> "Example: 📚🧮🧪 📚🧮🧪 📚🧮🧪"
                            SubjectGrouping.LEAST_GROUPED -> "Example: 📚🧮🧪 🧪📚🧮 🧮🧪📚"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 Schedule Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "• $selectedBlocksWeekday blocks per weekday, $selectedBlocksWeekend blocks per weekend",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Total: ${(selectedBlocksWeekday * 5 + selectedBlocksWeekend * 2) * selectedHorizonWeeks} study blocks",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Duration: $selectedHorizonWeeks weeks (${selectedHorizonWeeks * 7} days)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Block length: ${selectedBlockDuration} minutes each",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Subject grouping: ${selectedSubjectGrouping.displayName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Weekday time: ${formatTime(weekdayStudyTime)}/day • Weekend time: ${formatTime(weekendStudyTime)}/day",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Generate Button
            Button(
                onClick = {
                    onGenerate(selectedBlocksWeekday, selectedBlocksWeekend, selectedHorizonWeeks * 7, selectedBlockDuration, selectedSubjectGrouping)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isGeneratingSchedule
            ) {
                if (isGeneratingSchedule) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate New Schedule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Schedule Result Dialog
    if (showScheduleDialog && scheduleResult != null) {
        ScheduleResultDialog(
            result = scheduleResult!!,
            onDismiss = {
                showScheduleDialog = false
                viewModel.clearScheduleResult()
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun SchedulePreferenceCard(
    icon: ImageVector,
    title: String,
    description: String,
    value: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}