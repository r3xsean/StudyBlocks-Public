package com.example.studyblocks.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.ui.theme.StudyGradients
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class RescheduleOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
) {
    LATER_TODAY("Later Today", "Move to end of today", Icons.Default.Schedule, Color(0xFF4CAF50)),
    TODAY("Today", "Reschedule for today", Icons.Default.Today, Color(0xFF4CAF50)),
    TOMORROW("Tomorrow", "Reschedule for tomorrow", Icons.Default.CalendarToday, Color(0xFF2196F3)),
    CUSTOM_TIME("Custom Date", "Pick specific date", Icons.Default.DateRange, Color(0xFF9C27B0))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleDialog(
    studyBlock: StudyBlock,
    onDismiss: () -> Unit,
    onReschedule: (RescheduleOption, LocalDate?, LocalTime?) -> Unit,
    currentDate: LocalDate = LocalDate.now(),
    isVisible: Boolean = true
) {
    var selectedOption by remember { mutableStateOf<RescheduleOption?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(currentDate.plusDays(1)) }
    
    // Determine if the block is scheduled for today
    val isBlockScheduledForToday = studyBlock.scheduledDate == LocalDate.now()
    
    // Determine which options to show based on current scheduling
    val availableOptions = if (isBlockScheduledForToday) {
        listOf(RescheduleOption.TOMORROW, RescheduleOption.CUSTOM_TIME)
    } else {
        listOf(RescheduleOption.TODAY, RescheduleOption.CUSTOM_TIME)
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = studyBlock.subjectIcon,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Reschedule Block",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${studyBlock.subjectName} • ${studyBlock.durationMinutes} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Reschedule Options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        availableOptions.forEach { option ->
                            RescheduleOptionCard(
                                option = option,
                                isSelected = selectedOption == option,
                                onClick = { 
                                    selectedOption = option
                                    when (option) {
                                        RescheduleOption.LATER_TODAY -> {
                                            onReschedule(option, currentDate, null)
                                        }
                                        RescheduleOption.TODAY -> {
                                            onReschedule(option, LocalDate.now(), null)
                                        }
                                        RescheduleOption.TOMORROW -> {
                                            onReschedule(option, currentDate.plusDays(1), null)
                                        }
                                        RescheduleOption.CUSTOM_TIME -> {
                                            showDatePicker = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
                onReschedule(RescheduleOption.CUSTOM_TIME, selectedDate, null)
            },
            onDismiss = {
                showDatePicker = false
                selectedOption = null
            },
            initialDate = selectedDate
        )
    }
}

@Composable
private fun RescheduleOptionCard(
    option: RescheduleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "option_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                option.color.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        option.color.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = option.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = option.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
fun UndoSnackbar(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onUndo,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.inversePrimary
                        )
                    ) {
                        Text("UNDO", fontWeight = FontWeight.SemiBold)
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}