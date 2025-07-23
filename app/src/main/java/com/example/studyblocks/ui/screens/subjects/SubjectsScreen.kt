package com.example.studyblocks.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.SubjectIcon
import com.example.studyblocks.data.model.SubjectIconMatcher
import com.example.studyblocks.repository.SchedulingResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    navController: NavController,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val subjects by viewModel.sortedSubjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingSubject by viewModel.editingSubject.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val isGeneratingSchedule by viewModel.isGeneratingSchedule.collectAsState()
    val scheduleResult by viewModel.scheduleResult.collectAsState()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showScheduleSettingsDialog by remember { mutableStateOf(false) }
    
    // Handle schedule result
    LaunchedEffect(scheduleResult) {
        if (scheduleResult != null) {
            showScheduleDialog = true
        }
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
                        text = "My Subjects (${subjects.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    // Sort Menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortBy.values().forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption.displayName) },
                                    onClick = {
                                        viewModel.setSortBy(sortOption)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (sortBy == sortOption) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.height(64.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Schedule Generation Section
                if (subjects.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📅 Schedule Generation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Generate button
                            Button(
                                onClick = { showScheduleSettingsDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isGeneratingSchedule
                            ) {
                                if (isGeneratingSchedule) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating...")
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate New Schedule")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Subjects List
                if (subjects.isEmpty()) {
                    EmptySubjectsState(
                        onAddSubject = { viewModel.showAddSubjectDialog() }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects) { subject ->
                            SubjectCard(
                                subject = subject,
                                onClick = { navController.navigate("subject_detail/${subject.id}") }
                            )
                        }
                    }
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.showAddSubjectDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }
    
    // Add/Edit Subject Dialog
    if (showAddDialog) {
        AddEditSubjectDialog(
            editingSubject = editingSubject,
            onDismiss = { viewModel.hideAddSubjectDialog() },
            onSave = { name, icon, confidence ->
                val currentEditingSubject = editingSubject
                if (currentEditingSubject != null) {
                    viewModel.updateSubject(currentEditingSubject, name, icon, confidence)
                } else {
                    viewModel.addSubject(name, icon, confidence)
                }
            },
            getSuggestedIcon = { name -> viewModel.getSuggestedIcon(name) }
        )
    }
    
    // Schedule Settings Dialog
    if (showScheduleSettingsDialog) {
        ScheduleSettingsDialog(
            currentPreferredBlocks = viewModel.preferredBlocksPerDay.collectAsState().value,
            onDismiss = { showScheduleSettingsDialog = false },
            onGenerate = { preferredBlocks, horizon, blockDuration ->
                viewModel.generateNewSchedule(preferredBlocks, horizon, blockDuration)
                showScheduleSettingsDialog = false
            }
        )
    }
    
    // Schedule Result Dialog
    if (showScheduleDialog && scheduleResult != null) {
        ScheduleResultDialog(
            result = scheduleResult!!,
            onDismiss = {
                showScheduleDialog = false
                viewModel.clearScheduleResult()
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.icon,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    Column {
                        Text(
                            text = subject.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${subject.xp} XP",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Level Display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level ${subject.level}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confidence Rating (Read-only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(10) { index ->
                        val isFilled = index < subject.confidence
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Circle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isFilled) {
                                when {
                                    subject.confidence <= 3 -> MaterialTheme.colorScheme.error
                                    subject.confidence <= 6 -> Color(0xFFFF9800) // Orange
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "(${subject.confidence}/10)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Level Progress Bar
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress to Level ${subject.level + 1}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(subject.levelProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { subject.levelProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptySubjectsState(
    onAddSubject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No subjects yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first subject to get started with your personalized study schedule!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddSubject,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Subject")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectDialog(
    editingSubject: Subject?,
    onDismiss: () -> Unit,
    onSave: (String, SubjectIcon, Int) -> Unit,
    getSuggestedIcon: (String) -> SubjectIcon
) {
    var formState by remember(editingSubject) {
        mutableStateOf(
            if (editingSubject != null) {
                SubjectFormState(
                    name = editingSubject.name,
                    selectedIcon = SubjectIcon.values().find { it.emoji == editingSubject.icon } ?: SubjectIcon.DEFAULT,
                    confidence = editingSubject.confidence
                )
            } else {
                SubjectFormState()
            }
        )
    }
    
    var showIconPicker by remember { mutableStateOf(false) }
    
    // Auto-suggest icon when name changes
    LaunchedEffect(formState.name) {
        if (editingSubject == null && formState.name.isNotBlank()) {
            val suggestedIcon = getSuggestedIcon(formState.name)
            if (formState.selectedIcon == SubjectIcon.DEFAULT) {
                formState = formState.copy(selectedIcon = suggestedIcon)
            }
        }
    }
    
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
                    text = if (editingSubject != null) "Edit Subject" else "Add New Subject",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subject Name
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { name ->
                        formState = formState.copy(
                            name = name,
                            nameError = if (name.isBlank()) "Subject name is required" else null
                        )
                    },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.nameError != null,
                    supportingText = formState.nameError?.let { { Text(it) } }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
                            text = formState.selectedIcon.emoji,
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confidence Slider
                Text("Confidence Level: ${formState.confidence}/10")
                Slider(
                    value = formState.confidence.toFloat(),
                    onValueChange = { formState = formState.copy(confidence = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = when (formState.confidence) {
                        in 1..3 -> "Struggling - Need lots of practice"
                        in 4..6 -> "Learning - Making progress"
                        in 7..8 -> "Good - Pretty confident"
                        else -> "Excellent - Very confident"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
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
                            if (formState.isValid) {
                                onSave(
                                    formState.name,
                                    formState.selectedIcon,
                                    formState.confidence
                                )
                            }
                        },
                        enabled = formState.name.isNotBlank() && formState.nameError == null
                    ) {
                        Text(if (editingSubject != null) "Update" else "Add")
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
                                        if (icon == formState.selectedIcon) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            Color.Transparent
                                    )
                                    .clickable {
                                        formState = formState.copy(selectedIcon = icon)
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
fun PreferredBlocksPerDaySelector(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "Preferred blocks per day: $currentValue",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..6).toList()) { blocks ->
                FilterChip(
                    onClick = { onValueChange(blocks) },
                    label = { Text("$blocks") },
                    selected = blocks == currentValue,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Text(
            text = when (currentValue) {
                1 -> "Light schedule - Minimal daily commitment"
                2 -> "Relaxed schedule - Good for busy days"
                3 -> "Balanced schedule - Steady progress"
                4 -> "Active schedule - Focused learning"
                5 -> "Intensive schedule - Exam preparation"
                else -> "Very intensive - Maximum learning"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScheduleSettingsDialog(
    currentPreferredBlocks: Int,
    onDismiss: () -> Unit,
    onGenerate: (preferredBlocks: Int, horizon: Int, blockDuration: Int) -> Unit
) {
    var selectedBlocks by remember { mutableStateOf(currentPreferredBlocks) }
    var selectedHorizon by remember { mutableStateOf(14) } // Default 2 weeks
    var selectedBlockDuration by remember { mutableStateOf(60) } // Default 1 hour
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "📅 Schedule Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Configure your study schedule preferences:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Schedule horizon
                Text(
                    text = "Schedule horizon: $selectedHorizon days",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(7, 10, 14, 17, 21)) { days ->
                        FilterChip(
                            onClick = { selectedHorizon = days },
                            label = { 
                                Text(
                                    text = when (days) {
                                        7 -> "1w"
                                        10 -> "10d"
                                        14 -> "2w"
                                        17 -> "17d"
                                        21 -> "3w"
                                        else -> "${days}d"
                                    }
                                )
                            },
                            selected = days == selectedHorizon,
                            modifier = Modifier.widthIn(min = 48.dp)
                        )
                    }
                }
                
                Text(
                    text = when (selectedHorizon) {
                        7 -> "One week - Quick sprint"
                        10 -> "Ten days - Short-term focus"
                        14 -> "Two weeks - Balanced planning"
                        17 -> "17 days - Extended coverage"
                        21 -> "Three weeks - Comprehensive schedule"
                        else -> "$selectedHorizon days"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Blocks per day
                Text(
                    text = "Blocks per day: $selectedBlocks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..6).toList()) { blocks ->
                        FilterChip(
                            onClick = { selectedBlocks = blocks },
                            label = { Text("$blocks") },
                            selected = blocks == selectedBlocks,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Block duration
                Text(
                    text = "Block duration: $selectedBlockDuration minutes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
                            modifier = Modifier.widthIn(min = 48.dp)
                        )
                    }
                }
                
                Text(
                    text = "Total time per day: ${selectedBlocks * selectedBlockDuration} minutes (${selectedBlocks * selectedBlockDuration / 60.0} hours)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📊 Schedule Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Exactly $selectedBlocks blocks every day",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Total: ${selectedBlocks * selectedHorizon} study blocks",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Duration: $selectedHorizon days",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Block length: $selectedBlockDuration minutes each",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(selectedBlocks, selectedHorizon, selectedBlockDuration) }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Schedule")
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
fun ScheduleResultDialog(
    result: SchedulingResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Schedule Generated! 🎉") },
        text = {
            Column {
                Text("Your new study schedule has been created:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• ${result.totalBlocks} study blocks")
                Text("• Distributed over ${result.scheduleHorizon} days")
                
                if (result.subjectDistribution.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Subject breakdown:")
                    result.subjectDistribution.forEach { (subject, count) ->
                        Text("• $subject: $count blocks", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Great!")
            }
        }
    )
}