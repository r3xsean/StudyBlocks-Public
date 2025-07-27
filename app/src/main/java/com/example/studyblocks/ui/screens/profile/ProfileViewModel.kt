package com.example.studyblocks.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.studyblocks.auth.AuthRepository // Disabled for open source version
import com.example.studyblocks.data.model.AppTheme
import com.example.studyblocks.data.model.User
import com.example.studyblocks.data.model.UserPreferences
import com.example.studyblocks.repository.StudyRepository
import com.example.studyblocks.repository.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    // private val authRepository: AuthRepository, // Disabled for open source version
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _userPreferences = MutableStateFlow(UserPreferences(""))
    val userPreferences = _userPreferences.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _showSignOutDialog = MutableStateFlow(false)
    val showSignOutDialog = _showSignOutDialog.asStateFlow()
    
    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog = _showDeleteAccountDialog.asStateFlow()
    
    private val _profileStats = MutableStateFlow(ProfileStats())
    val profileStats = _profileStats.asStateFlow()
    
    // Check if onboarding is needed - true if no user exists or user hasn't completed onboarding or has no subjects
    val needsOnboarding: StateFlow<Boolean> = _currentUser.flatMapLatest { user ->
        if (user == null || !user.hasCompletedOnboarding) {
            flowOf(true)
        } else {
            // Check if user has any subjects (indicates successful onboarding completion)
            studyRepository.getAllSubjects(user.id).map { subjects ->
                subjects.isEmpty()
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true // Default to needing onboarding
    )
    
    init {
        loadCurrentUser()
        loadUserStats()
        createDefaultUserIfNeeded()
    }
    
    private fun createDefaultUserIfNeeded() {
        viewModelScope.launch {
            val currentUser = studyRepository.getCurrentUser()
            if (currentUser == null) {
                // Create a default offline user for the open source version
                val defaultUser = User(
                    id = "offline_user_001",
                    email = "offline@local.user",
                    displayName = "Local User",
                    hasCompletedOnboarding = false,
                    createdAt = LocalDateTime.now(),
                    lastSyncAt = LocalDateTime.now(),
                    globalXp = 0,
                    globalLevel = 1
                )
                studyRepository.insertUser(defaultUser)
            }
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    _userPreferences.value = _userPreferences.value.copy(userId = user.id)
                }
            }
        }
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                combine(
                    studyRepository.getTotalStudyMinutesFlow(user.id),
                    studyRepository.getTotalCompletedBlocksFlow(user.id),
                    studyRepository.getAllSubjects(user.id),
                    studyRepository.getAllSessions(user.id)
                ) { totalMinutes, totalBlocks, subjects, sessions ->
                    val completedSessions = sessions.filter { it.isCompleted }
                    val averageSessionDuration = if (completedSessions.isNotEmpty()) {
                        completedSessions.mapNotNull { it.actualDurationMinutes }.average()
                    } else 0.0
                    
                    ProfileStats(
                        totalHours = totalMinutes / 60.0,
                        totalBlocksCompleted = totalBlocks,
                        totalSubjects = subjects.size,
                        totalSessions = completedSessions.size,
                        averageSessionDuration = averageSessionDuration,
                        memberSince = user.createdAt.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        lastSync = user.lastSyncAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))
                    )
                }.collect { stats ->
                    _profileStats.value = stats
                }
            }
        }
    }
    
    fun updateUserPreferences(preferences: UserPreferences) {
        _userPreferences.value = preferences
        // In a real app, this would save to DataStore or Room
    }
    
    fun updateTheme(theme: AppTheme) {
        val updated = _userPreferences.value.copy(theme = theme)
        _userPreferences.value = updated
        // Save preferences logic here
    }
    
    fun updateNotificationSettings(
        notificationsEnabled: Boolean,
        studyReminders: Boolean,
        breakReminders: Boolean,
        morning: Boolean,
        afternoon: Boolean,
        evening: Boolean
    ) {
        val updated = _userPreferences.value.copy(
            notificationsEnabled = notificationsEnabled,
            studyRemindersEnabled = studyReminders,
            breakRemindersEnabled = breakReminders,
            morningRemindersEnabled = morning,
            afternoonRemindersEnabled = afternoon,
            eveningRemindersEnabled = evening
        )
        _userPreferences.value = updated
        // Save preferences logic here
    }
    
    fun updateStudySettings(
        focusModeEnabled: Boolean,
        autoStartTimer: Boolean
    ) {
        val updated = _userPreferences.value.copy(
            focusModeEnabled = focusModeEnabled,
            autoStartTimer = autoStartTimer
        )
        _userPreferences.value = updated
        // Save preferences logic here
    }
    
    fun showSignOutDialog() {
        _showSignOutDialog.value = true
    }
    
    fun hideSignOutDialog() {
        _showSignOutDialog.value = false
    }
    
    fun showDeleteAccountDialog() {
        _showDeleteAccountDialog.value = true
    }
    
    fun hideDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
    }
    
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // authRepository.signOut() // Disabled for open source version
                _showSignOutDialog.value = false
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUser.value?.let { user ->
                    // Delete user data
                    studyRepository.deleteAllDataForUser(user.id)
                    // Sign out
                    // authRepository.signOut() // Disabled for open source version
                }
                _showDeleteAccountDialog.value = false
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun exportData(): String {
        // Generate export data
        val user = _currentUser.value ?: return "No user data available"
        val stats = _profileStats.value
        
        return buildString {
            appendLine("StudyBlocks Data Export")
            appendLine("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
            appendLine()
            appendLine("User Information:")
            appendLine("Name: ${user.displayName}")
            appendLine("Email: ${user.email}")
            appendLine("Member since: ${stats.memberSince}")
            appendLine()
            appendLine("Study Statistics:")
            appendLine("Total hours studied: ${String.format("%.1f", stats.totalHours)}")
            appendLine("Total blocks completed: ${stats.totalBlocksCompleted}")
            appendLine("Total subjects: ${stats.totalSubjects}")
            appendLine("Total sessions: ${stats.totalSessions}")
            appendLine("Average session duration: ${String.format("%.1f", stats.averageSessionDuration)} minutes")
            appendLine()
            appendLine("Last sync: ${stats.lastSync}")
        }
    }
    
    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUser.value?.let { user ->
                    when (val result = studyRepository.syncUserData(user.id)) {
                        is SyncResult.Success -> {
                            // Sync successful - data is automatically updated through flows
                            loadUserStats() // Refresh stats
                        }
                        is SyncResult.Error -> {
                            // Handle sync error
                            // In a real app, show error message to user
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetOnboarding() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUser.value?.let { user ->
                    val updatedUser = user.copy(hasCompletedOnboarding = false)
                    studyRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetOnboardingAndDeleteSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUser.value?.let { user ->
                    // Delete all subjects and their associated blocks
                    studyRepository.deleteAllSubjectsForUser(user.id)
                    
                    // Reset onboarding completion status
                    val updatedUser = user.copy(hasCompletedOnboarding = false)
                    studyRepository.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ProfileStats(
    val totalHours: Double = 0.0,
    val totalBlocksCompleted: Int = 0,
    val totalSubjects: Int = 0,
    val totalSessions: Int = 0,
    val averageSessionDuration: Double = 0.0,
    val memberSince: String = "",
    val lastSync: String = ""
)