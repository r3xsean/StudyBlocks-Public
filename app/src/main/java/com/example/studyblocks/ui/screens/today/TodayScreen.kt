package com.example.studyblocks.ui.screens.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.getStatus
import com.example.studyblocks.data.model.StudyBlockStatus
import com.example.studyblocks.ui.animations.CompletionAnimation
import com.example.studyblocks.ui.animations.EnterAnimation
import com.example.studyblocks.ui.animations.StaggeredAnimation
import com.example.studyblocks.ui.components.StudyBlockCard
import com.example.studyblocks.ui.components.AnimatedFloatingActionButton
import com.example.studyblocks.ui.theme.StudyBlocksTypography
import com.example.studyblocks.ui.theme.StudyGradients
import com.example.studyblocks.navigation.Screen
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    navController: NavController,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val weekDates by viewModel.weekDates.collectAsState()
    val studyBlocks by viewModel.studyBlocksForSelectedDate.collectAsState()
    val completionStats by viewModel.completionStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showCustomBlockDialog by viewModel.showCustomBlockDialog.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allStudyBlocks by viewModel.allStudyBlocks.collectAsState()
    val xpAnimations by viewModel.xpAnimations.collectAsState()
    val isRescheduling by viewModel.isRescheduling.collectAsState()

    // Set up navigation callback for confidence reevaluation
    LaunchedEffect(Unit) {
        viewModel.setNavigationCallback {
            navController.navigate(Screen.ConfidenceReevaluation.route)
        }
    }

    // Clear animations when screen is disposed (navigating away)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAllXPAnimations()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Fixed Height Gradient Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Fixed height to prevent expansion
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
                            text = if (viewModel.isSelectedDateToday()) "Today" else viewModel.getFormattedSelectedDate(),
                            style = StudyBlocksTypography.screenTitle.copy(fontSize = 28.sp),
                            color = Color.White,
                            maxLines = 2
                        )
                        Text(
                            text = "Your study journey",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                    
                    // Reorganized button layout with column structure
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reschedule missed blocks button (only show if there are overdue blocks)
                            if (completionStats.overdue > 0 && viewModel.isSelectedDateToday()) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                        .clickable { viewModel.rescheduleWithMissedBlocks() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRescheduling) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Reschedule Missed Blocks",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .clickable { viewModel.showAddCustomBlockDialog() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Custom Block",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        
                        if (!viewModel.isSelectedDateToday()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.goToToday() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Body with optimized spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enhanced Date Picker
                DatePickerRow(
                    weekDates = weekDates,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    selectedDate = selectedDate
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Study Blocks List
                StudyBlocksList(
                    studyBlocks = studyBlocks,
                    allStudyBlocks = allStudyBlocks,
                    isLoading = isLoading,
                    onBlockToggle = { block, offset ->
                        viewModel.toggleBlockCompletion(block, offset.x, offset.y)
                    }
                )
            }
        } // <-- closes the Column ✅

        // Global XP Animation Overlay (keep this outside the Column but inside the root Box)
        Box(modifier = Modifier.fillMaxSize()) {
            xpAnimations.forEach { animation ->
                GlobalXPAnimation(
                    xpChange = animation.xpChange,
                    tapX = animation.tapX,
                    tapY = animation.tapY,
                    onAnimationComplete = { viewModel.clearXPAnimation(animation.timestamp) }
                )
            }
        }
    } // <-- closes the root Box ✅

    // Custom Block Dialog
    if (showCustomBlockDialog) {
        AddCustomBlockDialog(
            subjects = allSubjects,
            selectedDate = selectedDate,
            onDismiss = { viewModel.hideCustomBlockDialog() },
            onSave = { subjectId, durationMinutes ->
                viewModel.addCustomBlock(subjectId, durationMinutes)
            }
        )
    }
}

@Composable
fun DatePickerRow(
    weekDates: List<WeekDate>,
    onDateSelected: (LocalDate) -> Unit,
    selectedDate: LocalDate
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to selected date
    LaunchedEffect(selectedDate, weekDates) {
        if (weekDates.isNotEmpty()) {
            val selectedIndex = weekDates.indexOfFirst { it.date == selectedDate }
            if (selectedIndex >= 0) {
                listState.animateScrollToItem(selectedIndex)
            }
        }
    }
    
    // Modern scrollable date picker with glass effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.95f),
                RoundedCornerShape(20.dp)
            )
            .padding(vertical = 20.dp)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(weekDates) { weekDate ->
                EnhancedWeekDateItem(
                    weekDate = weekDate,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

@Composable
fun WeekDateItem(
    weekDate: WeekDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            weekDate.isSelected -> MaterialTheme.colorScheme.primary
            weekDate.isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        }
    )
    
    val textColor = when {
        weekDate.isSelected -> MaterialTheme.colorScheme.onPrimary
        weekDate.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onDateSelected(weekDate.date) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = weekDate.dayOfWeek,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = if (weekDate.isToday) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = weekDate.dayOfMonth.toString(),
            fontSize = 16.sp,
            color = textColor,
            fontWeight = if (weekDate.isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EnhancedWeekDateItem(
    weekDate: WeekDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (weekDate.isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            weekDate.isSelected -> MaterialTheme.colorScheme.primary
            weekDate.isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300)
    )
    
    val textColor = when {
        weekDate.isSelected -> Color.White
        weekDate.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onDateSelected(weekDate.date) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = weekDate.dayOfWeek.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = weekDate.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = if (weekDate.isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
        
        if (weekDate.isToday && !weekDate.isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        }
    }
}


@Composable
fun StudyBlocksList(
    studyBlocks: List<StudyBlock>,
    allStudyBlocks: List<StudyBlock>,
    isLoading: Boolean,
    onBlockToggle: (StudyBlock, Offset) -> Unit
) {
    if (studyBlocks.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(studyBlocks) { block ->
                val itemIndex = studyBlocks.indexOf(block)
                // Calculate subject-specific block numbering using all blocks for the subject
                val allSubjectBlocks = allStudyBlocks.filter { it.subjectId == block.subjectId && !it.isCustomBlock }
                val subjectBlockNumber = if (block.isCustomBlock) 0 else {
                    // Use block ID to find the correct position instead of object equality
                    val sortedBlocks = allSubjectBlocks.sortedBy { it.blockNumber }
                    val blockIndex = sortedBlocks.indexOfFirst { it.id == block.id }
                    if (blockIndex >= 0) blockIndex + 1 else block.blockNumber
                }
                val totalSubjectBlocks = if (block.isCustomBlock) 0 else allSubjectBlocks.size
                
                StaggeredAnimation(itemIndex = itemIndex) {
                    ModernStudyBlockCard(
                        studyBlock = block,
                        subjectBlockNumber = subjectBlockNumber,
                        totalSubjectBlocks = totalSubjectBlocks,
                        isLoading = isLoading,
                        onToggle = onBlockToggle
                    )
                }
            }
        }
    }
}

@Composable
fun StudyBlockCard(
    studyBlock: StudyBlock,
    subjectBlockNumber: Int = studyBlock.blockNumber,
    totalSubjectBlocks: Int = studyBlock.totalBlocksForSubject,
    isLoading: Boolean,
    onToggle: (StudyBlock, Offset) -> Unit
) {
    val status = studyBlock.getStatus()
    val cardColor = when (status) {
        StudyBlockStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        StudyBlockStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
        StudyBlockStatus.AVAILABLE -> MaterialTheme.colorScheme.surface
        StudyBlockStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (status) {
        StudyBlockStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        StudyBlockStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
        StudyBlockStatus.AVAILABLE -> MaterialTheme.colorScheme.onSurface
        StudyBlockStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (studyBlock.isCompleted) 0.7f else 1f,
        animationSpec = tween(300)
    )
    
    CompletionAnimation(isCompleted = studyBlock.isCompleted) {
        var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { cardCoords = it }
                .pointerInput(studyBlock.canComplete, studyBlock.isCompleted) {
                    detectTapGestures { offset ->
                        if (studyBlock.canComplete || studyBlock.isCompleted) {
                            val rootPos = cardCoords?.positionInRoot() ?: Offset.Zero
                            onToggle(studyBlock, rootPos + offset)
                        }
                    }
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor.copy(alpha = alpha)
            )
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Icon
            Text(
                text = studyBlock.subjectIcon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Subject Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = studyBlock.subjectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    textDecoration = if (studyBlock.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = if (studyBlock.isCustomBlock) {
                        "Custom Block • ${studyBlock.durationMinutes} min"
                    } else {
                        "Block $subjectBlockNumber out of $totalSubjectBlocks • ${studyBlock.durationMinutes} min"
                    },
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.7f),
                    textDecoration = if (studyBlock.isCompleted) TextDecoration.LineThrough else null
                )
            }
            
            // XP Animation
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (studyBlock.canComplete || studyBlock.isCompleted) {
                    Icon(
                        imageVector = if (studyBlock.isCompleted)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.FiberManualRecord,
                        contentDescription = if (studyBlock.isCompleted) "Completed" else "Mark Complete",
                        tint = when {
                            studyBlock.isCompleted -> MaterialTheme.colorScheme.primary
                            studyBlock.canComplete -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No study blocks scheduled for today!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Great job staying on track!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomBlockDialog(
    subjects: List<Subject>,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var durationMinutes by remember { mutableStateOf(60) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    
    val selectedSubject = subjects.find { it.id == selectedSubjectId }
    
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
                    text = "Add Custom Block",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subject Selection
                ExposedDropdownMenuBox(
                    expanded = showSubjectDropdown,
                    onExpandedChange = { showSubjectDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Subject") },
                        leadingIcon = {
                            if (selectedSubject != null) {
                                Text(
                                    text = selectedSubject.icon,
                                    fontSize = 20.sp
                                )
                            }
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubjectDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = subject.icon,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(subject.name)
                                    }
                                },
                                onClick = {
                                    selectedSubjectId = subject.id
                                    showSubjectDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Duration Selection
                Text(
                    text = "Duration: $durationMinutes minutes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(15, 30, 45, 60, 90, 120)) { duration ->
                        FilterChip(
                            onClick = { durationMinutes = duration },
                            label = { 
                                Text(
                                    text = when (duration) {
                                        15 -> "15m"
                                        30 -> "30m"
                                        45 -> "45m"
                                        60 -> "1h"
                                        90 -> "1.5h"
                                        120 -> "2h"
                                        else -> "${duration}m"
                                    }
                                )
                            },
                            selected = duration == durationMinutes
                        )
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
                            if (selectedSubjectId.isNotEmpty()) {
                                onSave(selectedSubjectId, durationMinutes)
                                onDismiss()
                            }
                        },
                        enabled = selectedSubjectId.isNotEmpty()
                    ) {
                        Text("Add Block")
                    }
                }
            }
        }
    }
}


@Composable
fun ModernStudyBlockCard(
    studyBlock: StudyBlock,
    subjectBlockNumber: Int = studyBlock.blockNumber,
    totalSubjectBlocks: Int = studyBlock.totalBlocksForSubject,
    isLoading: Boolean,
    onToggle: (StudyBlock, Offset) -> Unit
) {
    val status = studyBlock.getStatus()
    
    CompletionAnimation(isCompleted = studyBlock.isCompleted) {
        var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        
        StudyBlockCard(
            onClick = if (studyBlock.canComplete || studyBlock.isCompleted) {
                {
                    val coords = cardCoords?.positionInRoot() ?: Offset.Zero
                    onToggle(studyBlock, coords)
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { cardCoords = it },
            isCompleted = status == StudyBlockStatus.COMPLETED,
            isOverdue = status == StudyBlockStatus.OVERDUE,
            isPending = status == StudyBlockStatus.PENDING
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Icon with background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studyBlock.subjectIcon,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Subject Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = studyBlock.subjectName,
                        style = StudyBlocksTypography.subjectTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (studyBlock.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!studyBlock.isCustomBlock) {
                            Text(
                                text = "Block $subjectBlockNumber/$totalSubjectBlocks",
                                style = StudyBlocksTypography.blockDuration,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                style = StudyBlocksTypography.blockDuration,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Custom Block",
                                style = StudyBlocksTypography.blockDuration,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "•",
                                style = StudyBlocksTypography.blockDuration,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${studyBlock.durationMinutes} min",
                            style = StudyBlocksTypography.blockDuration,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            when (status) {
                                StudyBlockStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                StudyBlockStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
                                StudyBlockStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                                StudyBlockStatus.AVAILABLE -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (status) {
                        StudyBlockStatus.COMPLETED -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        StudyBlockStatus.OVERDUE -> {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Overdue",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        StudyBlockStatus.PENDING -> {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = "Pending",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        StudyBlockStatus.AVAILABLE -> {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

