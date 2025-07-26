package com.example.studyblocks.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidenceReevaluationScreen(
    navController: NavController,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val allSubjects by viewModel.allSubjects.collectAsState()
    
    var confidenceUpdates by remember { 
        mutableStateOf(allSubjects.associate { it.id to it.confidence })
    }
    
    // Update confidence updates when subjects change
    LaunchedEffect(allSubjects) {
        confidenceUpdates = allSubjects.associate { it.id to it.confidence }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "🎉 Schedule Complete!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.height(64.dp)
            )
            
            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Celebration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌟",
                            fontSize = 48.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Congratulations!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "You've completed all your scheduled study blocks! How do you feel about your understanding of each subject now?",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Subjects Confidence Rating
                Text(
                    text = "Update Your Confidence Levels",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allSubjects) { subject ->
                        ConfidenceRatingCard(
                            subject = subject,
                            currentConfidence = confidenceUpdates[subject.id] ?: subject.confidence,
                            onConfidenceChanged = { newConfidence ->
                                confidenceUpdates = confidenceUpdates.toMutableMap().apply {
                                    put(subject.id, newConfidence)
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip for Now")
                    }
                    
                    Button(
                        onClick = {
                            viewModel.updateSubjectConfidences(confidenceUpdates)
                            navController.navigate(Screen.ScheduleSettings.route) {
                                popUpTo(Screen.Today.route)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Update & Generate Schedule")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ConfidenceRatingCard(
    subject: Subject,
    currentConfidence: Int,
    onConfidenceChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Subject Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Level ${subject.level} • ${subject.xp} XP",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Current Confidence Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Confidence:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(10) { index ->
                        val isFilled = index < currentConfidence
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Circle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isFilled) {
                                when {
                                    currentConfidence <= 3 -> MaterialTheme.colorScheme.error
                                    currentConfidence <= 6 -> Color(0xFFFF9800) // Orange
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                        if (index < 9) Spacer(modifier = Modifier.width(2.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "($currentConfidence/10)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confidence Slider
            Text(
                text = "New Confidence Level: $currentConfidence/10",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = currentConfidence.toFloat(),
                onValueChange = { onConfidenceChanged(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = when {
                        currentConfidence <= 3 -> MaterialTheme.colorScheme.error
                        currentConfidence <= 6 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    activeTrackColor = when {
                        currentConfidence <= 3 -> MaterialTheme.colorScheme.error
                        currentConfidence <= 6 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            )
            
            Text(
                text = when (currentConfidence) {
                    in 1..3 -> "Struggling - Need lots of practice"
                    in 4..6 -> "Learning - Making progress"
                    in 7..8 -> "Good - Pretty confident"
                    else -> "Excellent - Very confident"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}