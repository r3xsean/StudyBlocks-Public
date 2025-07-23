package com.example.studyblocks.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.studyblocks.data.model.AppTheme
import com.example.studyblocks.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val profileStats by viewModel.profileStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showSignOutDialog by viewModel.showSignOutDialog.collectAsState()
    val showDeleteAccountDialog by viewModel.showDeleteAccountDialog.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showStudySettings by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            actions = {
                IconButton(onClick = { viewModel.syncData() }) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sync")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.height(64.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Header
            item {
                UserProfileHeader(
                    user = currentUser,
                    stats = profileStats
                )
            }
            
            // Account Section
            item {
                ProfileSection(
                    title = "Account",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Person,
                            title = "Profile Information",
                            subtitle = currentUser?.displayName ?: "Not signed in",
                            onClick = { /* Navigate to profile edit */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Email,
                            title = "Email",
                            subtitle = currentUser?.email ?: "",
                            onClick = { /* Navigate to email change */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Sync,
                            title = "Sync Data",
                            subtitle = "Last sync: ${profileStats.lastSync}",
                            onClick = { viewModel.syncData() }
                        )
                    )
                )
            }
            
            // App Settings Section
            item {
                ProfileSection(
                    title = "App Settings",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.ColorLens,
                            title = "Theme",
                            subtitle = userPreferences.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            onClick = { showThemeDialog = true }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = if (userPreferences.notificationsEnabled) "Enabled" else "Disabled",
                            onClick = { showNotificationSettings = true }
                        ),
                        ProfileItem(
                            icon = Icons.Default.AccessTime,
                            title = "Study Settings",
                            subtitle = "Timer and focus preferences",
                            onClick = { showStudySettings = true }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Translate,
                            title = "Language",
                            subtitle = "English (US)",
                            onClick = { /* Navigate to language selection */ }
                        )
                    )
                )
            }
            
            // Data & Privacy Section
            item {
                ProfileSection(
                    title = "Data & Privacy",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.Default.Download,
                            title = "Export Data",
                            subtitle = "Download your study data",
                            onClick = { 
                                val data = viewModel.exportData()
                                // Handle data export - in real app would save to file or share
                            }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Security,
                            title = "Privacy Settings",
                            subtitle = "Manage your data privacy",
                            onClick = { /* Navigate to privacy settings */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Delete,
                            title = "Delete Account",
                            subtitle = "Permanently delete your account",
                            onClick = { viewModel.showDeleteAccountDialog() },
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                )
            }
            
            // Support Section
            item {
                ProfileSection(
                    title = "Support",
                    items = listOf(
                        ProfileItem(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = "Help & Support",
                            subtitle = "Get help with StudyBlocks",
                            onClick = { /* Navigate to help */ }
                        ),
                        ProfileItem(
                            icon = Icons.Default.Info,
                            title = "About",
                            subtitle = "App version and information",
                            onClick = { showAboutDialog = true }
                        ),
                        ProfileItem(
                            icon = Icons.Default.RateReview,
                            title = "Rate App",
                            subtitle = "Leave a review on the Play Store",
                            onClick = { /* Open Play Store */ }
                        )
                    )
                )
            }
            
            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.showSignOutDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
    
    // Dialogs
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = userPreferences.theme,
            onThemeSelected = { theme ->
                viewModel.updateTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    if (showNotificationSettings) {
        NotificationSettingsDialog(
            preferences = userPreferences,
            onSettingsChanged = { notifications, studyReminders, breakReminders, morning, afternoon, evening ->
                viewModel.updateNotificationSettings(notifications, studyReminders, breakReminders, morning, afternoon, evening)
                showNotificationSettings = false
            },
            onDismiss = { showNotificationSettings = false }
        )
    }
    
    if (showStudySettings) {
        StudySettingsDialog(
            preferences = userPreferences,
            onSettingsChanged = { focusMode, autoStart ->
                viewModel.updateStudySettings(focusMode, autoStart)
                showStudySettings = false
            },
            onDismiss = { showStudySettings = false }
        )
    }
    
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideSignOutDialog() },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out? Your data will remain saved for when you sign back in.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.signOut() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideSignOutDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteAccountDialog() },
            title = { Text("Delete Account") },
            text = { 
                Text("Are you sure you want to permanently delete your account? This action cannot be undone and all your study data will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteAccountDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserProfileHeader(
    user: User?,
    stats: ProfileStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profilePictureUrl != null) {
                    // In real app, load image from URL
                    Text(
                        text = user.displayName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = user?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // User Info
            Text(
                text = user?.displayName ?: "Guest User",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Global Level & XP
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Global Level ${user?.globalLevel ?: 1}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${user?.globalXp ?: 0} XP",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Global level progress
                    if (user != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progress to Level ${user.globalLevel + 1}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(user.globalLevelProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LinearProgressIndicator(
                                progress = { user.globalLevelProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }
            }
            Text(
                text = "Member since ${stats.memberSince}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat(
                    value = String.format("%.1f", stats.totalHours),
                    label = "Hours Studied"
                )
                QuickStat(
                    value = stats.totalBlocksCompleted.toString(),
                    label = "Blocks Completed"
                )
                QuickStat(
                    value = stats.totalSubjects.toString(),
                    label = "Subjects"
                )
            }
        }
    }
}

@Composable
fun QuickStat(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            items.forEachIndexed { index, item ->
                ProfileItemRow(item = item)
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(item: ProfileItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.iconColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = item.textColor ?: MaterialTheme.colorScheme.onSurface
            )
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ProfileItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
    val textColor: Color? = null,
    val iconColor: Color? = null
)

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Choose Theme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                AppTheme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
fun NotificationSettingsDialog(
    preferences: com.example.studyblocks.data.model.UserPreferences,
    onSettingsChanged: (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var notifications by remember { mutableStateOf(preferences.notificationsEnabled) }
    var studyReminders by remember { mutableStateOf(preferences.studyRemindersEnabled) }
    var breakReminders by remember { mutableStateOf(preferences.breakRemindersEnabled) }
    var morningReminders by remember { mutableStateOf(preferences.morningRemindersEnabled) }
    var afternoonReminders by remember { mutableStateOf(preferences.afternoonRemindersEnabled) }
    var eveningReminders by remember { mutableStateOf(preferences.eveningRemindersEnabled) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Notification Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notifications,
                        onCheckedChange = { notifications = it }
                    )
                }
                
                // Study Reminders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Study Reminders")
                        Text(
                            text = "Get notified about upcoming study blocks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = studyReminders && notifications,
                        onCheckedChange = { studyReminders = it },
                        enabled = notifications
                    )
                }
                
                // Break Reminders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Break Reminders")
                        Text(
                            text = "Get reminded to take breaks during study sessions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = breakReminders && notifications,
                        onCheckedChange = { breakReminders = it },
                        enabled = notifications
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily Reminder Settings
                if (notifications && studyReminders) {
                    Text(
                        text = "Daily Block Reminders",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Morning Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Morning (8:00 AM)")
                            Text(
                                text = "Check remaining blocks for the day",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = morningReminders,
                            onCheckedChange = { morningReminders = it }
                        )
                    }
                    
                    // Afternoon Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Afternoon (2:00 PM)")
                            Text(
                                text = "Midday progress check-in",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = afternoonReminders,
                            onCheckedChange = { afternoonReminders = it }
                        )
                    }
                    
                    // Evening Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Evening (7:00 PM)")
                            Text(
                                text = "Final reminder for incomplete blocks",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = eveningReminders,
                            onCheckedChange = { eveningReminders = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
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
                    TextButton(
                        onClick = { 
                            onSettingsChanged(notifications, studyReminders, breakReminders, morningReminders, afternoonReminders, eveningReminders)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun StudySettingsDialog(
    preferences: com.example.studyblocks.data.model.UserPreferences,
    onSettingsChanged: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var focusMode by remember { mutableStateOf(preferences.focusModeEnabled) }
    var autoStart by remember { mutableStateOf(preferences.autoStartTimer) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Study Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Focus Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Focus Mode")
                        Text(
                            text = "Hide distracting notifications during study",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = focusMode,
                        onCheckedChange = { focusMode = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Auto Start Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-start Timer")
                        Text(
                            text = "Automatically start timer when selecting a block",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoStart,
                        onCheckedChange = { autoStart = it }
                    )
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
                    TextButton(
                        onClick = { 
                            onSettingsChanged(focusMode, autoStart)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "StudyBlocks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Version 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "AI-Powered Study Block Scheduler",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Organize your study time using AI-generated study blocks distributed over multiple days based on spaced repetition principles and confidence-weighted algorithms.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}