package com.example.studyblocks.ui.screens.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.StudyBlock
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val availableBlocks by viewModel.availableBlocks.collectAsState()
    val breakType by viewModel.breakType.collectAsState()
    val completedPomodoros by viewModel.completedPomodoros.collectAsState()
    val showFocusDialog by viewModel.showFocusDialog.collectAsState()
    val focusScore by viewModel.focusScore.collectAsState()
    
    var showBlockSelector by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Study Timer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            actions = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pomodoro Counter
            if (completedPomodoros > 0) {
                PomodoroCounter(completedPomodoros)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Timer Display
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimerCircle(
                    progress = progress,
                    remainingTime = remainingTime,
                    timerState = timerState,
                    breakType = breakType,
                    onTimerClick = {
                        if (timerState == TimerState.IDLE && selectedBlock == null) {
                            showBlockSelector = true
                        }
                    },
                    formatTime = viewModel::formatTime
                )
            }
            
            // Current Block Info
            if (selectedBlock != null) {
                CurrentBlockInfo(
                    block = selectedBlock!!,
                    timeProgress = viewModel.getTimeProgress()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Timer Controls
            TimerControls(
                timerState = timerState,
                selectedBlock = selectedBlock,
                onStartTimer = viewModel::startTimer,
                onPauseTimer = viewModel::pauseTimer,
                onResumeTimer = viewModel::resumeTimer,
                onStopTimer = viewModel::stopTimer,
                onSelectBlock = { showBlockSelector = true },
                onStartBreak = viewModel::startBreak,
                onSkipBreak = viewModel::skipBreak
            )
        }
    }
    
    // Block Selector Dialog
    if (showBlockSelector) {
        BlockSelectorDialog(
            availableBlocks = availableBlocks,
            onBlockSelected = { block ->
                viewModel.selectBlock(block)
                showBlockSelector = false
            },
            onDismiss = { showBlockSelector = false }
        )
    }
    
    // Focus Score Dialog
    if (showFocusDialog) {
        FocusScoreDialog(
            currentScore = focusScore,
            onScoreSelected = { score ->
                viewModel.submitFocusScore(score)
            }
        )
    }
    
    // Settings Dialog
    if (showSettings) {
        TimerSettingsDialog(
            settings = viewModel.pomodoroSettings.collectAsState().value,
            onSettingsChanged = { settings ->
                viewModel.updatePomodoroSettings(settings)
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun TimerCircle(
    progress: Float,
    remainingTime: Long,
    timerState: TimerState,
    breakType: BreakType,
    onTimerClick: () -> Unit,
    formatTime: (Long) -> String
) {
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    val circleColor = when (timerState) {
        TimerState.RUNNING -> MaterialTheme.colorScheme.primary
        TimerState.PAUSED -> MaterialTheme.colorScheme.tertiary
        TimerState.BREAK -> MaterialTheme.colorScheme.secondary
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
    }
    
    Box(
        modifier = Modifier
            .size(280.dp)
            .clickable { onTimerClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Background circle
            drawCircle(
                color = circleColor.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )
            
            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = circleColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Display
            Text(
                text = formatTime(remainingTime),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // State Label
            Text(
                text = when (timerState) {
                    TimerState.IDLE -> "Tap to start"
                    TimerState.RUNNING -> "Studying"
                    TimerState.PAUSED -> "Paused"
                    TimerState.BREAK -> when (breakType) {
                        BreakType.SHORT -> "Short Break"
                        BreakType.LONG -> "Long Break"
                        BreakType.NONE -> "Break"
                    }
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PomodoroCounter(completedPomodoros: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🍅 ",
            fontSize = 24.sp
        )
        Text(
            text = "$completedPomodoros completed today",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CurrentBlockInfo(
    block: StudyBlock,
    timeProgress: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = block.subjectIcon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.subjectName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Block ${block.blockNumber}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeProgress,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TimerControls(
    timerState: TimerState,
    selectedBlock: StudyBlock?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onSelectBlock: () -> Unit,
    onStartBreak: (BreakType) -> Unit,
    onSkipBreak: () -> Unit
) {
    when (timerState) {
        TimerState.IDLE -> {
            if (selectedBlock == null) {
                Button(
                    onClick = onSelectBlock,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose a Study Block")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectBlock,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Change Block")
                    }
                    
                    Button(
                        onClick = onStartTimer,
                        modifier = Modifier.weight(2f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Timer")
                    }
                }
            }
        }
        
        TimerState.RUNNING -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onStopTimer,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
                
                Button(
                    onClick = onPauseTimer,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause")
                }
            }
        }
        
        TimerState.PAUSED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onStopTimer,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
                
                Button(
                    onClick = onResumeTimer,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume")
                }
            }
        }
        
        TimerState.BREAK -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Take a break! 🧘‍♂️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onSkipBreak) {
                        Text("Skip Break")
                    }
                    
                    Button(onClick = { onStartBreak(BreakType.SHORT) }) {
                        Text("5 min break")
                    }
                    
                    Button(onClick = { onStartBreak(BreakType.LONG) }) {
                        Text("15 min break")
                    }
                }
            }
        }
    }
}

@Composable
fun BlockSelectorDialog(
    availableBlocks: List<StudyBlock>,
    onBlockSelected: (StudyBlock) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Choose Study Block",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (availableBlocks.isEmpty()) {
                    Text(
                        text = "No study blocks available for today.\nCheck the Today tab to see your schedule!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(availableBlocks) { block ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onBlockSelected(block) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = block.subjectIcon,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    
                                    Column {
                                        Text(
                                            text = block.subjectName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Block ${block.blockNumber} • ${block.durationMinutes} min",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun FocusScoreDialog(
    currentScore: Int,
    onScoreSelected: (Int) -> Unit
) {
    Dialog(onDismissRequest = { onScoreSelected(currentScore) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How was your focus?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Rate your focus level during this study session",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Focus score selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    (1..10).forEach { score ->
                        val isSelected = score == currentScore
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onScoreSelected(score) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = score.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Text(
                    text = when (currentScore) {
                        in 1..3 -> "😵 Distracted"
                        in 4..6 -> "😐 Average focus"
                        in 7..8 -> "😊 Good focus"
                        else -> "🎯 Excellent focus!"
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { onScoreSelected(currentScore) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Session")
                }
            }
        }
    }
}

@Composable
fun TimerSettingsDialog(
    settings: PomodoroSettings,
    onSettingsChanged: (PomodoroSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var workDuration by remember { mutableStateOf(settings.workDuration / 60) }
    var shortBreak by remember { mutableStateOf(settings.shortBreakDuration / 60) }
    var longBreak by remember { mutableStateOf(settings.longBreakDuration / 60) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Timer Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Work Duration
                Text("Work Duration: ${workDuration} minutes")
                Slider(
                    value = workDuration.toFloat(),
                    onValueChange = { workDuration = it.toInt() },
                    valueRange = 15f..60f,
                    steps = 8
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Short Break
                Text("Short Break: ${shortBreak} minutes")
                Slider(
                    value = shortBreak.toFloat(),
                    onValueChange = { shortBreak = it.toInt() },
                    valueRange = 3f..15f,
                    steps = 11
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Long Break
                Text("Long Break: ${longBreak} minutes")
                Slider(
                    value = longBreak.toFloat(),
                    onValueChange = { longBreak = it.toInt() },
                    valueRange = 10f..30f,
                    steps = 19
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            onSettingsChanged(
                                PomodoroSettings(
                                    workDuration = workDuration * 60,
                                    shortBreakDuration = shortBreak * 60,
                                    longBreakDuration = longBreak * 60,
                                    longBreakInterval = settings.longBreakInterval
                                )
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}