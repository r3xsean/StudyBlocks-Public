package com.example.studyblocks.ui.screens.subjects

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.SubjectIcon
import com.example.studyblocks.data.model.SubjectIconMatcher
import com.example.studyblocks.data.model.XPDataPoint
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.components.XPProgressBar
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    navController: NavController,
    viewModel: SubjectDetailViewModel = hiltViewModel()
) {
    val subject by viewModel.subject.collectAsState()
    val studyStats by viewModel.studyStats.collectAsState()
    val subjectXPProgression by viewModel.subjectXPProgression.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    
    LaunchedEffect(subjectId) {
        viewModel.loadSubject(subjectId)
    }
    
    if (subject == null) {
        // Loading or subject not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Subject not found")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }
    
    val currentSubject = subject!!
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient Header
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { navController.navigateUp() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Subject Info
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentSubject.icon,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = currentSubject.name,
                                    style = StudyBlocksTypography.screenTitle.copy(fontSize = 24.sp),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "Level ${currentSubject.level} • ${currentSubject.xp} XP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { viewModel.showEditDialog() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { viewModel.showDeleteDialog() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        
            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Level & XP Section
                    item {
                        ModernLevelXPCard(subject = currentSubject)
                    }
                    
                    
                    // Confidence Section
                    item {
                        ModernConfidenceCard(
                            subject = currentSubject,
                            onConfidenceChange = { confidence ->
                                viewModel.updateConfidence(confidence)
                            }
                        )
                    }
                    
                    // Study Statistics
                    item {
                        ModernStudyStatsCard(stats = studyStats, subject = currentSubject)
                    }
                }
            }
        }
    }
    
    // Edit Subject Dialog
    if (showEditDialog) {
        EditSubjectDialog(
            subject = currentSubject,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { name, icon ->
                viewModel.updateSubject(name, icon)
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteSubjectDialog(
            subject = currentSubject,
            onDismiss = { viewModel.hideDeleteDialog() },
            onConfirm = {
                viewModel.deleteSubject()
                navController.navigateUp()
            }
        )
    }
}

// Removed old LevelXPCard - using ModernLevelXPCard instead

// Removed old ConfidenceCard - using ModernConfidenceCard instead

// Removed old StudyStatsCard - using ModernStudyStatsCard instead

// Removed old StatItem - using ModernStatItem instead


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubjectDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onSave: (String, SubjectIcon) -> Unit
) {
    var name by remember { mutableStateOf(subject.name) }
    var selectedIcon by remember { 
        mutableStateOf(
            SubjectIcon.values().find { it.emoji == subject.icon } ?: SubjectIcon.DEFAULT
        ) 
    }
    var showIconPicker by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Subject",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subject Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Icon Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Icon:")
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedIcon.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { showIconPicker = true }) {
                            Text("Change")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedIcon)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
    
    // Icon Picker Dialog
    if (showIconPicker) {
        Dialog(onDismissRequest = { showIconPicker = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Choose an Icon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(SubjectIconMatcher.getAllIcons()) { icon ->
                            Text(
                                text = icon.emoji,
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (icon == selectedIcon) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            Color.Transparent
                                    )
                                    .clickable {
                                        selectedIcon = icon
                                        showIconPicker = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteSubjectDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Subject?") },
        text = { 
            Text("This will permanently delete ${subject.name} and all its study blocks. This action cannot be undone.") 
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ModernLevelXPCard(subject: Subject) {
    val confidenceColor = when (subject.confidence) {
        in 1..3 -> Color(0xFFEF5350)
        in 4..6 -> Color(0xFFFF9800)
        in 7..8 -> Color(0xFF66BB6A)
        else -> Color(0xFF42A5F5)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                confidenceColor.copy(alpha = 0.1f),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level Display
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        confidenceColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${subject.level}",
                    style = StudyBlocksTypography.levelDisplay,
                    fontSize = 32.sp,
                    color = confidenceColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Level ${subject.level}",
                style = StudyBlocksTypography.subjectTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${subject.xp} XP earned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // XP Progress Bar
            XPProgressBar(
                currentXP = subject.xp.toLong(),
                targetXP = subject.xpForNextLevel.toLong(),
                level = subject.level,
                showXPNumbers = true,
                animationDuration = 1500
            )
        }
    }
}

@Composable
fun ModernConfidenceCard(
    subject: Subject,
    onConfidenceChange: (Int) -> Unit
) {
    val confidenceColor = when (subject.confidence) {
        in 1..3 -> Color(0xFFEF5350)
        in 4..6 -> Color(0xFFFF9800)
        in 7..8 -> Color(0xFF66BB6A)
        else -> Color(0xFF42A5F5)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Confidence Level",
                style = StudyBlocksTypography.subjectTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Visual Confidence Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                confidenceColor.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${subject.confidence}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = confidenceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = when (subject.confidence) {
                            in 1..3 -> "Need Focus"
                            in 4..6 -> "Making Progress"
                            in 7..8 -> "Pretty Confident"
                            else -> "Very Confident"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = confidenceColor
                    )
                    
                    Text(
                        text = when (subject.confidence) {
                            in 1..3 -> "Consider more study time"
                            in 4..6 -> "You're learning well"
                            in 7..8 -> "Great understanding"
                            else -> "Mastery level!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Interactive Slider
            Text(
                text = "Adjust confidence (1-10):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = subject.confidence.toFloat(),
                onValueChange = { onConfidenceChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = confidenceColor,
                    activeTrackColor = confidenceColor,
                    inactiveTrackColor = confidenceColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun ModernStudyStatsCard(stats: StudyStats, subject: Subject) {
    val confidenceColor = when (subject.confidence) {
        in 1..3 -> Color(0xFFEF5350)
        in 4..6 -> Color(0xFFFF9800)
        in 7..8 -> Color(0xFF66BB6A)
        else -> Color(0xFF42A5F5)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Study Progress",
                style = StudyBlocksTypography.subjectTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernStatItem(
                    label = "XP Earned",
                    value = "${stats.totalXP}",
                    color = confidenceColor,
                    icon = "⭐"
                )
                ModernStatItem(
                    label = "Study Time",
                    value = "${stats.totalMinutes / 60}h ${stats.totalMinutes % 60}m",
                    color = MaterialTheme.colorScheme.secondary,
                    icon = "⏱️"
                )
                ModernStatItem(
                    label = "Blocks",
                    value = "${stats.completedBlocks}/${stats.totalBlocks}",
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = "📊"
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Study Insights
            if (stats.totalBlocks > 0) {
                val averageSessionTime = if (stats.completedBlocks > 0) stats.totalMinutes / stats.completedBlocks else 0
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            confidenceColor.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Study Insights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Average session: ${averageSessionTime} minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (stats.totalXP > 0) {
                            Text(
                                text = "XP rate: ${(stats.totalXP.toFloat() / (stats.totalMinutes / 60f)).toInt()} XP/hour",
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
fun ModernStatItem(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

data class StudyStats(
    val totalBlocks: Int = 0,
    val completedBlocks: Int = 0,
    val totalMinutes: Int = 0,
    val totalXP: Int = 0
)