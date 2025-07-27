package com.example.studyblocks.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
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
import com.example.studyblocks.navigation.Screen
import com.example.studyblocks.data.model.SubjectIcon
import com.example.studyblocks.data.model.SubjectIconMatcher
import com.example.studyblocks.repository.SchedulingResult
import com.example.studyblocks.ui.components.SubjectCard
import com.example.studyblocks.ui.components.AnimatedFloatingActionButton
import com.example.studyblocks.ui.components.ModernCard
import com.example.studyblocks.ui.components.GradientButton
import com.example.studyblocks.ui.components.ConfidenceRatingIndicator
import com.example.studyblocks.ui.components.XPProgressBar
import com.example.studyblocks.ui.components.GlassMorphicCard
import com.example.studyblocks.ui.components.ModernMetricCard
import com.example.studyblocks.ui.components.PillSegmentedControl
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.ui.theme.ConfidenceColors
import com.example.studyblocks.ui.theme.ConfidenceGradients
import com.example.studyblocks.ui.theme.ModernPurple
import com.example.studyblocks.ui.theme.ModernTeal
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
    var viewMode by remember { mutableStateOf(0) } // 0 = grid, 1 = list
    
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
            // Modern Header with glassmorphic design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = StudyGradients.purpleTealGradient
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "My Subjects",
                                style = StudyBlocksTypography.welcomeTitle,
                                color = Color.White
                            )
                            Text(
                                text = "${subjects.size} subjects to master",
                                style = StudyBlocksTypography.cardSubtitle,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        // Action button with improved glassmorphic design
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.25f),
                                        CircleShape
                                    )
                                    .clickable { showSortMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.White
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(16.dp)
                                )
                            ) {
                                SortBy.values().forEach { sortOption ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                sortOption.displayName,
                                                style = StudyBlocksTypography.cardTitle,
                                                fontWeight = if (sortBy == sortOption) FontWeight.Bold else FontWeight.Normal
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.setSortBy(sortOption)
                                            showSortMenu = false
                                        },
                                        leadingIcon = {
                                            if (sortBy == sortOption) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = ModernTeal,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // View mode selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PillSegmentedControl(
                            selectedIndex = viewMode,
                            options = listOf("Grid", "List"),
                            onSelectionChange = { viewMode = it },
                            modifier = Modifier.width(140.dp),
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            selectedColor = Color.White,
                            unselectedColor = Color.White.copy(alpha = 0.8f)
                        )
                        
                        // Total XP display
                        val totalXP = subjects.sumOf { it.xp }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${totalXP.toInt()}",
                                style = StudyBlocksTypography.levelDisplayLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Total XP",
                                style = StudyBlocksTypography.microLabel,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Schedule Generation Section
                if (subjects.isNotEmpty()) {
                    GlassMorphicCard(
                        onClick = { navController.navigate(Screen.ScheduleSettings.route) },
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            StudyGradients.purplePinkGradient,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Text(
                                    text = "📅",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Column {
                                    Text(
                                        text = "Generate Schedule",
                                        style = StudyBlocksTypography.subjectTitle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "AI-powered study plan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Compact Generate button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isGeneratingSchedule) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    if (isGeneratingSchedule) "Generating..." else "Generate",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Modern Subjects List with grid/list toggle
                if (subjects.isEmpty()) {
                    ModernEmptySubjectsState(
                        onAddSubject = { viewModel.showAddSubjectDialog() }
                    )
                } else {
                    if (viewMode == 0) {
                        // Grid View
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(subjects) { subject ->
                                ModernSubjectGridCard(
                                    subject = subject,
                                    onClick = { navController.navigate("subject_detail/${subject.id}") }
                                )
                            }
                        }
                    } else {
                        // List View
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(subjects) { subject ->
                                ModernSubjectListCard(
                                    subject = subject,
                                    onClick = { navController.navigate("subject_detail/${subject.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Enhanced Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .background(
                    brush = StudyGradients.primaryGradient,
                    shape = CircleShape
                )
                .clickable { viewModel.showAddSubjectDialog() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Subject",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
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

@Composable
fun ModernSubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubjectCard(
        onClick = onClick,
        modifier = modifier,
        confidenceLevel = subject.confidence,
        xp = subject.xp.toLong(),
        level = subject.level
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon with gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        StudyGradients.primaryGradient,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.icon,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Subject Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subject.name,
                    style = StudyBlocksTypography.subjectTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // XP Progress Bar
                XPProgressBar(
                    currentXP = subject.xp.toLong(),
                    targetXP = subject.xpForNextLevel.toLong(),
                    level = subject.level,
                    showXPNumbers = false,
                    animationDuration = 1000
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Block Duration
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
                            text = "${subject.blockDurationMinutes}min blocks",
                            style = StudyBlocksTypography.blockDuration,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Level Display
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
                            text = "Level ${subject.level}",
                            style = StudyBlocksTypography.levelDisplay,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Confidence Rating Indicator
            ConfidenceRatingIndicator(
                confidence = subject.confidence,
                size = 60.dp,
                interactive = false
            )
        }
    }
}

@Composable
fun EnhancedSubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val confidenceColor = when (subject.confidence) {
        in 1..3 -> Color(0xFFEF5350) // Red for low confidence
        in 4..6 -> Color(0xFFFF9800) // Orange for medium confidence  
        in 7..8 -> Color(0xFF66BB6A) // Green for good confidence
        else -> Color(0xFF42A5F5) // Blue for excellent confidence
    }
    
    ModernCard(
        onClick = { onClick() },
        modifier = modifier.fillMaxWidth(),
        elevation = 8.dp,
        cornerRadius = 20.dp,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon with colored background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        confidenceColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.icon,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Subject Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = subject.name,
                        style = StudyBlocksTypography.subjectTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Level badge - now properly constrained and won't get cut off
                    Box(
                        modifier = Modifier
                            .background(
                                confidenceColor.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Level ${subject.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = confidenceColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // XP Progress with better styling
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${subject.xp} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${subject.xpForNextLevel} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                confidenceColor.copy(alpha = 0.1f),
                                RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(subject.levelProgress)
                                .fillMaxHeight()
                                .background(
                                    confidenceColor,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Enhanced stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Block Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.blockDurationMinutes}min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Confidence indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(confidenceColor, CircleShape)
                        )
                        Text(
                            text = "${subject.confidence}/10 confidence",
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

// New modern component functions

@Composable
fun ModernEmptySubjectsState(
    onAddSubject: () -> Unit
) {
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        StudyGradients.glassPurpleGradient,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ModernPurple
                )
            }
            
            Text(
                text = "No subjects yet",
                style = StudyBlocksTypography.welcomeTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Add your first subject to start your study journey",
                style = StudyBlocksTypography.cardSubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onAddSubject,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ModernPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Subject",
                    style = StudyBlocksTypography.pillButtonText
                )
            }
        }
    }
}

@Composable
fun ModernSubjectGridCard(
    subject: Subject,
    onClick: () -> Unit
) {
    val confidenceGradient = if (subject.confidence <= 10) {
        ConfidenceGradients[subject.confidence - 1]
    } else {
        StudyGradients.glassPrimaryGradient
    }
    
    GlassMorphicCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        cornerRadius = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with icon and confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = subject.icon,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp)
                )
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            confidenceGradient,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${subject.confidence}",
                        style = StudyBlocksTypography.achievementBadge,
                        color = Color.White
                    )
                }
            }
            
            // Subject name
            Text(
                text = subject.name,
                style = StudyBlocksTypography.cardTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            
            // Level and XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Level ${subject.level}",
                        style = StudyBlocksTypography.microLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${subject.xp.toInt()} XP",
                        style = StudyBlocksTypography.statisticsLabel,
                        color = ModernTeal
                    )
                }
                
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernSubjectListCard(
    subject: Subject,
    onClick: () -> Unit
) {
    val confidenceGradient = if (subject.confidence <= 10) {
        ConfidenceGradients[subject.confidence - 1]
    } else {
        StudyGradients.glassPrimaryGradient
    }
    
    GlassMorphicCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section with icon and info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Subject icon with gradient background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            confidenceGradient,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                // Subject info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subject.name,
                        style = StudyBlocksTypography.subjectTitleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Level ${subject.level}",
                            style = StudyBlocksTypography.microLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.blockDurationMinutes}min blocks",
                            style = StudyBlocksTypography.microLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${subject.xp.toInt()} XP",
                        style = StudyBlocksTypography.statisticsLabel,
                        color = ModernTeal
                    )
                }
            }
            
            // Right section with confidence and arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${subject.confidence}/10",
                        style = StudyBlocksTypography.microLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}