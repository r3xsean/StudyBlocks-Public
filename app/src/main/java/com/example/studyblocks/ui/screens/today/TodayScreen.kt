package com.example.studyblocks.ui.screens.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.getStatus
import com.example.studyblocks.data.model.StudyBlockStatus
import com.example.studyblocks.ui.animations.CompletionAnimation
import com.example.studyblocks.ui.animations.EnterAnimation
import com.example.studyblocks.ui.animations.StaggeredAnimation
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
    val xpAnimations by viewModel.xpAnimations.collectAsState()
    val xpChanges by viewModel.xpChanges.collectAsState() // ✅ make sure this exists in VM

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        var globalTapPosition by remember { mutableStateOf(Offset(0f, 0f)) }

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isSelectedDateToday()) "Today" else viewModel.getFormattedSelectedDate(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showAddCustomBlockDialog() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Block")
                    }
                    if (!viewModel.isSelectedDateToday()) {
                        TextButton(
                            onClick = { viewModel.goToToday() }
                        ) {
                            Text("Today")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.height(64.dp)
            )

            // Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Date Picker
                DatePickerRow(
                    weekDates = weekDates,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    selectedDate = selectedDate
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Study Blocks List
                StudyBlocksList(
                    studyBlocks = studyBlocks,
                    isLoading = isLoading,
                    xpChanges = xpChanges,
                    onBlockToggle = { block -> viewModel.toggleBlockCompletion(block) }
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
    
    // Scrollable date picker
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(weekDates) { weekDate ->
                WeekDateItem(
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
fun StudyBlocksList(
    studyBlocks: List<StudyBlock>,
    isLoading: Boolean,
    xpChanges: List<TodayViewModel.XPChange>,
    onBlockToggle: (StudyBlock) -> Unit
) {
    if (studyBlocks.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(studyBlocks) { block ->
                val itemIndex = studyBlocks.indexOf(block)
                val blockXPChange = xpChanges.find { it.blockId == block.id }
                StaggeredAnimation(itemIndex = itemIndex) {
                    StudyBlockCard(
                        studyBlock = block,
                        isLoading = isLoading,
                        xpChange = blockXPChange,
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
    isLoading: Boolean,
    xpChange: TodayViewModel.XPChange?,
    onToggle: (StudyBlock) -> Unit
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading && studyBlock.canComplete) { 
                    onToggle(studyBlock) 
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
                        "Block ${studyBlock.blockNumber} out of ${studyBlock.totalBlocksForSubject} • ${studyBlock.durationMinutes} min"
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
                // Completion Checkbox
                if (studyBlock.canComplete || studyBlock.isCompleted) {
                    IconButton(
                        onClick = { onToggle(studyBlock) },
                        enabled = !isLoading
                    ) {
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
                
                // XP Change Animation
                if (xpChange != null) {
                    XPChangeAnimation(xpChange = xpChange)
                }
            }
        }
        }
    }
}

@Composable
fun XPChangeAnimation(
    xpChange: TodayViewModel.XPChange
) {
    val offsetY by animateFloatAsState(
        targetValue = -40f,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing),
        label = "xp_offset"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(1500, delayMillis = 500),
        label = "xp_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "xp_scale"
    )
    
    Text(
        text = if (xpChange.xpChange > 0) "+${xpChange.xpChange} XP" else "${xpChange.xpChange} XP",
        color = if (xpChange.xpChange > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset(y = offsetY.dp)
            .alpha(1f - alpha)
            .scale(scale)
    )
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
                            .menuAnchor()
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