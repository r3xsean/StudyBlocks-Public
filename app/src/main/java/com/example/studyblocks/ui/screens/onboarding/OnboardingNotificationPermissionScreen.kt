package com.example.studyblocks.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studyblocks.notifications.NotificationService
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingNotificationPermissionScreen(
    navController: NavController,
    notificationService: NotificationService,
    onNext: () -> Unit
) {
    var permissionGranted by remember { mutableStateOf(notificationService.hasNotificationPermission()) }
    var permissionAsked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    // Permission request launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        permissionAsked = true
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
            // Icon with animation based on permission status
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (permissionGranted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (permissionGranted) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (permissionGranted) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (permissionGranted) "Notifications Enabled! 🎉" else "Enable Notifications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (permissionGranted) {
                    "Perfect! You'll receive helpful study reminders and daily summaries to keep you on track with your learning goals."
                } else {
                    "Get gentle reminders about your study blocks and motivational daily summaries to stay consistent with your learning."
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Permission Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (permissionGranted) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (permissionGranted) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (permissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Notification Permission",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (permissionGranted) {
                        "✅ Granted - You'll receive study reminders"
                    } else if (permissionAsked) {
                        "❌ Permission denied - You can enable it later in Settings"
                    } else {
                        "⏳ Tap the button below to enable notifications"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Notifications")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Benefits explanation
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
                Text(
                    text = "📱 What You'll Receive",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationBenefit(
                    icon = "🌅",
                    title = "Morning Check-ins",
                    description = "Gentle reminders about today's study blocks"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationBenefit(
                    icon = "📊",
                    title = "Daily Summaries",
                    description = "Progress reports with achievements and tomorrow's preview"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationBenefit(
                    icon = "⏰",
                    title = "Smart Reminders",
                    description = "Timely nudges to help you stay consistent"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Privacy note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🔒 Privacy First",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Notifications are only sent to your device\n" +
                          "• No personal data is shared with third parties\n" +
                          "• You can disable notifications anytime in Settings\n" +
                          "• StudyBlocks works perfectly without notifications too",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
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
                onClick = {
                    // Test notification if permission granted
                    if (permissionGranted) {
                        notificationService.testShowSimpleNotification()
                    }
                    onNext()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (permissionGranted) "Continue & Test" else "Skip for Now")
            }
        }
        
        // Progress indicator
        Text(
            text = "Step 7 of 7",
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
private fun NotificationBenefit(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}