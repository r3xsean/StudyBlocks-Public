package com.example.studyblocks.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBlockDurationScreen(
    navController: NavController,
    initialDuration: Int = 60,
    onDurationSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    var blockDuration by remember { mutableStateOf(initialDuration) }
    val scrollState = rememberScrollState()
    
    // Update parent whenever value changes
    LaunchedEffect(blockDuration) {
        onDurationSelected(blockDuration)
    }
    
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
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Block Duration",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "How long should each study session be? The right duration balances focus and retention without mental fatigue.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Main Setting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Session Length",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${blockDuration} minutes",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Slider(
                    value = blockDuration.toFloat(),
                    onValueChange = { 
                        blockDuration = (it.toInt() / 15) * 15 // Round to 15-minute intervals
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when {
                        blockDuration <= 30 -> "Quick sessions: Great for reviews and vocabulary"
                        blockDuration <= 45 -> "Standard sessions: Good for most subjects"
                        blockDuration <= 75 -> "Focused sessions: Ideal for deep learning"
                        blockDuration <= 120 -> "Extended sessions: Perfect for complex topics"
                        else -> "Marathon sessions: For intensive study periods"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Study Method Examples
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recommended Study Methods",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Visual timeline showing study vs break pattern
                when {
                    blockDuration <= 30 -> {
                        StudyMethodCard(
                            title = "Micro-Learning",
                            description = "Perfect for: Flashcards, quick reviews, vocabulary",
                            pattern = "Study ${blockDuration}min → 5min break",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    blockDuration <= 60 -> {
                        StudyMethodCard(
                            title = "Standard Focus",
                            description = "Perfect for: Most subjects, balanced learning",
                            pattern = "Study ${blockDuration}min → 15min break",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    blockDuration <= 90 -> {
                        StudyMethodCard(
                            title = "Deep Work",
                            description = "Perfect for: Complex problems, writing, analysis",
                            pattern = "Study ${blockDuration}min → 20min break",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    else -> {
                        StudyMethodCard(
                            title = "Extended Focus",
                            description = "Perfect for: Research, projects, comprehensive review",
                            pattern = "Study ${blockDuration}min → 30min break",
                            color = MaterialTheme.colorScheme.error
                        )
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
                Text("Next")
            }
        }
        
        // Progress indicator
        Text(
            text = "Step 4 of 6",
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
private fun StudyMethodCard(
    title: String,
    description: String,
    pattern: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "⏱️ $pattern",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}