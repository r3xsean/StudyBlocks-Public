package com.example.studyblocks.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Refresh
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    navController: NavController,
    currentPreferredBlocks: Int,
    onGenerate: (blocksPerWeekday: Int, blocksPerWeekend: Int, horizon: Int, blockDuration: Int) -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    var selectedBlocksWeekday by remember { mutableStateOf(currentPreferredBlocks) }
    var selectedBlocksWeekend by remember { mutableStateOf(2) } // Default 2 blocks on weekends
    var selectedHorizonWeeks by remember { mutableStateOf(2) } // Default 2 weeks
    var selectedBlockDuration by remember { mutableStateOf(60) } // Default 1 hour
    
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            1 -> "One week - Quick sprint"
                            2 -> "Two weeks - Balanced planning"
                            3 -> "Three weeks - Comprehensive schedule"
                            4 -> "Four weeks - Extended planning"
                            else -> "$selectedHorizonWeeks weeks"
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
                        items((1..6).toList()) { blocks ->
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
                            1 -> "Light weekday schedule - Minimal daily commitment"
                            2 -> "Relaxed weekday schedule - Good for busy days"
                            3 -> "Balanced weekday schedule - Steady progress"
                            4 -> "Active weekday schedule - Focused learning"
                            5 -> "Intensive weekday schedule - Exam preparation"
                            else -> "Very intensive weekdays - Maximum learning"
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
                        items((0..4).toList()) { blocks ->
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
                            0 -> "Weekend breaks - No study blocks"
                            1 -> "Light weekend schedule - Keep momentum"
                            2 -> "Balanced weekend schedule - Steady progress"
                            3 -> "Active weekend schedule - Intensive learning"
                            else -> "Very intensive weekends - Maximum focus"
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
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(30, 45, 60, 90, 120)) { duration ->
                            FilterChip(
                                onClick = { selectedBlockDuration = duration },
                                label = { 
                                    Text(
                                        text = when (duration) {
                                            30 -> "30m"
                                            45 -> "45m"
                                            60 -> "1h"
                                            90 -> "1.5h"
                                            120 -> "2h"
                                            else -> "${duration}m"
                                        }
                                    )
                                },
                                selected = duration == selectedBlockDuration,
                                modifier = Modifier.widthIn(min = 56.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Recommended: 45-60 minutes for focused learning",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                    onGenerate(selectedBlocksWeekday, selectedBlocksWeekend, selectedHorizonWeeks * 7, selectedBlockDuration)
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