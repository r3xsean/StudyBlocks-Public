package com.example.studyblocks.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Weekend
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
import com.example.studyblocks.data.model.OnboardingSchedulePreferences
import com.example.studyblocks.repository.SchedulingResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPreferencesScreen(
    navController: NavController,
    onPreferencesSet: (OnboardingSchedulePreferences) -> Unit,
    onComplete: () -> Unit
) {
    var scheduleHorizonWeeks by remember { mutableStateOf(3) }
    var blocksPerWeekday by remember { mutableStateOf(3) }
    var blocksPerWeekend by remember { mutableStateOf(2) }
    var blockDuration by remember { mutableStateOf(60) }
    
    val scrollState = rememberScrollState()
    
    // Calculate total study time
    val weekdayStudyTime = blocksPerWeekday * blockDuration
    val weekendStudyTime = blocksPerWeekend * blockDuration
    
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
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Your Preferences",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Customize your study schedule to fit your lifestyle and goals.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Schedule Horizon
        PreferenceCard(
            icon = Icons.Default.CalendarMonth,
            title = "Schedule Horizon",
            description = "How many weeks ahead to plan your study schedule",
            value = "$scheduleHorizonWeeks weeks (${scheduleHorizonWeeks * 7} days)",
            onClick = { }
        ) {
            Column {
                Text(
                    text = "Weeks: $scheduleHorizonWeeks (${scheduleHorizonWeeks * 7} days)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = scheduleHorizonWeeks.toFloat(),
                    onValueChange = { scheduleHorizonWeeks = it.toInt() },
                    valueRange = 1f..4f,
                    steps = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Recommended: 3 weeks for optimal spaced repetition",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Blocks per Weekday
        PreferenceCard(
            icon = Icons.Default.Schedule,
            title = "Weekday Study Blocks",
            description = "Number of study blocks for Monday-Friday",
            value = "$blocksPerWeekday blocks (${formatTime(weekdayStudyTime)} daily)",
            onClick = { }
        ) {
            Column {
                Text(
                    text = "Blocks: $blocksPerWeekday (Total: ${formatTime(weekdayStudyTime)} per day)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = blocksPerWeekday.toFloat(),
                    onValueChange = { blocksPerWeekday = it.toInt() },
                    valueRange = 1f..8f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Recommended: 3-4 blocks for busy weekdays",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Blocks per Weekend
        PreferenceCard(
            icon = Icons.Default.Weekend,
            title = "Weekend Study Blocks",
            description = "Number of study blocks for Saturday-Sunday",
            value = "$blocksPerWeekend blocks (${formatTime(weekendStudyTime)} daily)",
            onClick = { }
        ) {
            Column {
                Text(
                    text = "Blocks: $blocksPerWeekend (Total: ${formatTime(weekendStudyTime)} per day)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = blocksPerWeekend.toFloat(),
                    onValueChange = { blocksPerWeekend = it.toInt() },
                    valueRange = 0f..6f,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "You can set to 0 if you prefer weekends off",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Block Duration
        PreferenceCard(
            icon = Icons.Default.Timer,
            title = "Block Duration",
            description = "Length of each study session",
            value = "${blockDuration} minutes",
            onClick = { }
        ) {
            Column {
                Text(
                    text = "Duration: ${blockDuration} minutes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = blockDuration.toFloat(),
                    onValueChange = { blockDuration = (it.toInt() / 15) * 15 }, // Round to 15-minute intervals
                    valueRange = 15f..180f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    text = "📊 Your Study Schedule Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "• Planning ${scheduleHorizonWeeks} weeks (${scheduleHorizonWeeks * 7} days) ahead",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "• ${formatTime(weekdayStudyTime)} per weekday (${blocksPerWeekday} blocks)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (blocksPerWeekend > 0) {
                    Text(
                        text = "• ${formatTime(weekendStudyTime)} per weekend day (${blocksPerWeekend} blocks)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = "• Weekend breaks (no study blocks)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = "• ${blockDuration}-minute study sessions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                onClick = {
                    println("DEBUG PreferencesScreen: Complete Setup button clicked")
                    val preferences = OnboardingSchedulePreferences(
                        scheduleHorizonDays = scheduleHorizonWeeks * 7,
                        blocksPerWeekday = blocksPerWeekday,
                        blocksPerWeekend = blocksPerWeekend,
                        defaultBlockDurationMinutes = blockDuration
                    )
                    println("DEBUG PreferencesScreen: Created preferences: $preferences")
                    onPreferencesSet(preferences)
                    println("DEBUG PreferencesScreen: Called onPreferencesSet")
                    onComplete()
                    println("DEBUG PreferencesScreen: Called onComplete")
                },
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
            text = "Step 3 of 3",
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
private fun PreferenceCard(
    icon: ImageVector,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
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