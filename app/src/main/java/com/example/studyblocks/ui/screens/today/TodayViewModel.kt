package com.example.studyblocks.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.User
import com.example.studyblocks.data.model.getStatus
import com.example.studyblocks.repository.StudyRepository
import com.example.studyblocks.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    val notificationService: com.example.studyblocks.notifications.NotificationService
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _isRescheduling = MutableStateFlow(false)
    val isRescheduling = _isRescheduling.asStateFlow()
    
    private val _showCustomBlockDialog = MutableStateFlow(false)
    val showCustomBlockDialog = _showCustomBlockDialog.asStateFlow()
    
    private val _showRescheduleDialog = MutableStateFlow<StudyBlock?>(null)
    val showRescheduleDialog = _showRescheduleDialog.asStateFlow()
    
    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog = _showSummaryDialog.asStateFlow()
    
    private val _undoSnackbarVisible = MutableStateFlow(false)
    val undoSnackbarVisible = _undoSnackbarVisible.asStateFlow()
    
    private val _undoMessage = MutableStateFlow("")
    val undoMessage = _undoMessage.asStateFlow()
    
    // Store the last reschedule operation for undo functionality
    private var lastRescheduleOperation: RescheduleOperation? = null
    
    
    private val _weekDates = MutableStateFlow<List<WeekDate>>(emptyList())
    val weekDates = _weekDates.asStateFlow()
    
    // XP animation tracking
    data class XPAnimation(
        val blockId: String,
        val xpChange: Int,
        val tapX: Float,
        val tapY: Float,
        val timestamp: Long
    )
    private val _xpAnimations = MutableStateFlow<List<XPAnimation>>(emptyList())
    val xpAnimations = _xpAnimations.asStateFlow()
    
    // Track blocks currently being processed to prevent race conditions
    private val processingBlocks = mutableSetOf<String>()
    
    // Track cooldown timestamps for each block (300ms cooldown to match CompletionAnimation)
    private val blockCooldowns = mutableMapOf<String, Long>()
    private val BLOCK_COOLDOWN_MS = 300L // Match CompletionAnimation duration
    
    init {
        loadCurrentUser()
        generateWeekDates()
        checkAndGenerateScheduleIfNeeded()
        
        // Initialize notification scheduling when user is available
        viewModelScope.launch {
            _currentUser.first { it != null }
            // Schedule daily summary notifications once user is authenticated
            notificationService.scheduleDailySummary(enabled = true)
        }
        
        // Check for pending summary when ViewModel is created (app opened/resumed)
        viewModelScope.launch {
            // Wait for user to be loaded first
            _currentUser.first { it != null }
            checkAndShowPendingSummary()
        }
        
        // Watch for changes in user data to handle live summary triggers
        viewModelScope.launch {
            _currentUser.collect { user ->
                if (user?.pendingSummaryDate != null && !_showSummaryDialog.value) {
                    // Show summary dialog when pending date is set and dialog isn't already shown
                    _showSummaryDialog.value = true
                }
            }
        }
    }
    
    val studyBlocksForSelectedDate: StateFlow<List<StudyBlock>> = combine(
        _selectedDate,
        _currentUser
    ) { date, user ->
        Pair(date, user)
    }.flatMapLatest { (date, user) ->
        if (user != null) {
            studyRepository.getBlocksForDate(user.id, date)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val allSubjects: StateFlow<List<Subject>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            studyRepository.getAllSubjects(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Get all blocks for all subjects to calculate correct block numbering
    val allStudyBlocks: StateFlow<List<StudyBlock>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            val startDate = LocalDate.now()
            val endDate = startDate.plusDays(60) // Look ahead for schedule horizon
            studyRepository.getBlocksForDateRange(user.id, startDate, endDate)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Yesterday's blocks for summary dialog
    val yesterdayBlocks: StateFlow<List<StudyBlock>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            val yesterday = LocalDate.now().minusDays(1)
            studyRepository.getBlocksForDate(user.id, yesterday)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Tomorrow's blocks for summary dialog
    val tomorrowBlocks: StateFlow<List<StudyBlock>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            val tomorrow = LocalDate.now().plusDays(1)
            studyRepository.getBlocksForDate(user.id, tomorrow)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val completionStats: StateFlow<CompletionStats> = studyBlocksForSelectedDate.map { blocks ->
        val total = blocks.size
        val completed = blocks.count { it.isCompleted }
        val available = blocks.count { it.getStatus().name == "AVAILABLE" }
        val overdue = blocks.count { it.getStatus().name == "OVERDUE" }
        
        CompletionStats(
            total = total,
            completed = completed,
            available = available,
            overdue = overdue,
            completionPercentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompletionStats()
    )
    
    // Track if schedule is complete to show confidence dialog
    private var hasShownConfidenceDialog = false
    
    // Navigation callback for schedule completion
    private var onNavigateToConfidenceReevaluation: (() -> Unit)? = null
    
    // Navigation callback for daily summary when all today's blocks are completed
    private var onNavigateToDailySummary: (() -> Unit)? = null
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    private fun checkForScheduleCompletionAfterBlockCompletion() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && user.hasCompletedOnboarding && !hasShownConfidenceDialog) {
                // Get current blocks safely
                try {
                    val blocks = allStudyBlocks.value
                    checkForScheduleCompletion(blocks)
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
        }
    }
    
    private fun checkForScheduleCompletion(allBlocks: List<StudyBlock>) {
        // Only consider scheduled blocks (not custom blocks) for schedule completion
        val scheduledBlocks = allBlocks.filter { !it.isCustomBlock }
        
        if (scheduledBlocks.isNotEmpty()) {
            val allScheduledBlocksCompleted = scheduledBlocks.all { it.isCompleted }
            
            if (allScheduledBlocksCompleted) {
                hasShownConfidenceDialog = true
                onNavigateToConfidenceReevaluation?.invoke()
            }
        }
        
        // Check if all TODAY's blocks are completed and trigger daily summary
        checkForTodayCompletion(allBlocks)
    }
    
    private fun checkForTodayCompletion(allBlocks: List<StudyBlock>) {
        if (isSelectedDateToday()) {
            val today = LocalDate.now()
            val todayBlocks = allBlocks.filter { it.scheduledDate == today }
            
            if (todayBlocks.isNotEmpty()) {
                val allTodayBlocksCompleted = todayBlocks.all { it.isCompleted }
                
                if (allTodayBlocksCompleted) {
                    // Trigger daily summary for today
                    triggerDailySummary()
                }
            }
        }
    }
    
    private fun triggerDailySummary() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TodayViewModel", "All today's blocks completed - triggering daily summary")
                // Navigate to daily summary
                onNavigateToDailySummary?.invoke()
            } catch (e: Exception) {
                android.util.Log.e("TodayViewModel", "Error triggering daily summary", e)
            }
        }
    }
    
    private fun checkAndGenerateScheduleIfNeeded() {
        viewModelScope.launch {
            // Wait for user to be loaded, then check if schedule needs to be generated
            _currentUser.collect { user ->
                if (user != null && user.hasCompletedOnboarding) {
                    try {
                        val subjects = studyRepository.getAllSubjects(user.id).first()
                        val startDate = LocalDate.now()
                        val endDate = startDate.plusDays(7) // Look ahead 1 week
                        val blocks = studyRepository.getBlocksForDateRange(user.id, startDate, endDate).first()
                        
                        // If user has subjects but no blocks, generate schedule
                        if (subjects.isNotEmpty() && blocks.isEmpty()) {
                            try {
                                studyRepository.generateNewSchedule(user.id)
                            } catch (e: Exception) {
                                // Log error but don't crash
                                println("Auto-schedule generation failed: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        // Log error but don't crash
                        println("Failed to check schedule status: ${e.message}")
                    }
                    return@collect // Only check once when user is loaded
                }
            }
        }
    }
    
    private fun generateWeekDates() {
        generateDateRange()
    }
    
    private fun generateDateRange() {
        val today = LocalDate.now()
        val startDate = today.minusDays(30) // Show 30 days before today
        val endDate = today.plusDays(60)    // Show 60 days after today
        
        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .map { date ->
                WeekDate(
                    date = date,
                    dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    dayOfMonth = date.dayOfMonth,
                    isToday = date == today,
                    isSelected = date == _selectedDate.value
                )
            }.toList()
        
        _weekDates.value = allDates
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateWeekDatesSelection()
    }
    
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        updateWeekDatesSelection()
    }
    
    private fun generateWeekDatesForDate(targetDate: LocalDate) {
        val startOfWeek = targetDate.minusDays(targetDate.dayOfWeek.value.toLong() - 1)
        val today = LocalDate.now()
        
        val weekDates = (0..6).map { offset ->
            val date = startOfWeek.plusDays(offset.toLong())
            WeekDate(
                date = date,
                dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                dayOfMonth = date.dayOfMonth,
                isToday = date == today,
                isSelected = date == _selectedDate.value
            )
        }
        
        _weekDates.value = weekDates
    }
    
    private fun updateWeekDatesSelection() {
        val updated = _weekDates.value.map { weekDate ->
            weekDate.copy(isSelected = weekDate.date == _selectedDate.value)
        }
        _weekDates.value = updated
    }
    
    fun toggleBlockCompletion(block: StudyBlock, tapX: Float = 0f, tapY: Float = 0f) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Clean up old cooldowns periodically to prevent memory buildup
            clearOldCooldowns()
            
            // Check cooldown - prevent spamming by enforcing minimum time between taps
            synchronized(blockCooldowns) {
                val lastTapTime = blockCooldowns[block.id] ?: 0L
                if (currentTime - lastTapTime < BLOCK_COOLDOWN_MS) {
                    return@launch // Block is in cooldown, ignore this tap
                }
                blockCooldowns[block.id] = currentTime
            }
            
            // Prevent concurrent operations on the same block
            synchronized(processingBlocks) {
                if (processingBlocks.contains(block.id)) {
                    return@launch // Block is already being processed, ignore this tap
                }
                processingBlocks.add(block.id)
            }
            
            _isLoading.value = true
            try {
                val xpChange = if (block.isCompleted) {
                    // Delete associated study sessions when marking incomplete
                    studyRepository.deleteSessionsForBlock(block.id)
                    val xp = studyRepository.markBlockIncomplete(block.id)
                    -xp // Negative for subtraction
                } else {
                    // Create a completed study session when completing a block
                    val user = _currentUser.value
                    if (user != null) {
                        studyRepository.createCompletedStudySession(
                            studyBlockId = block.id,
                            subjectId = block.subjectId,
                            userId = user.id,
                            actualDurationMinutes = block.durationMinutes
                        )
                    }
                    studyRepository.markBlockComplete(block.id)
                }

                // Add XP animation with tap coordinates
                if (xpChange != 0) {
                    val newXPAnimation = XPAnimation(
                        block.id,
                        xpChange,
                        tapX,
                        tapY,
                        System.currentTimeMillis()
                    )
                    _xpAnimations.value = _xpAnimations.value + newXPAnimation
                }
                
                // Check for schedule completion after completing a block
                if (!block.isCompleted && xpChange > 0) {
                    checkForScheduleCompletionAfterBlockCompletion()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
                // Remove from processing set when done
                synchronized(processingBlocks) {
                    processingBlocks.remove(block.id)
                }
            }
        }
    }
    
    fun clearXPAnimation(timestamp: Long) {
        _xpAnimations.value = _xpAnimations.value.filter { it.timestamp != timestamp }
    }
    
    fun clearAllXPAnimations() {
        _xpAnimations.value = emptyList()
    }
    
    private fun clearOldCooldowns() {
        val currentTime = System.currentTimeMillis()
        synchronized(blockCooldowns) {
            blockCooldowns.entries.removeAll { (_, lastTapTime) ->
                currentTime - lastTapTime > BLOCK_COOLDOWN_MS
            }
        }
    }
    
    fun getFormattedSelectedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
        return _selectedDate.value.format(formatter)
    }
    
    fun isSelectedDateToday(): Boolean {
        return _selectedDate.value == LocalDate.now()
    }
    
    fun showAddCustomBlockDialog() {
        _showCustomBlockDialog.value = true
    }
    
    fun hideCustomBlockDialog() {
        _showCustomBlockDialog.value = false
    }
    
    fun showSummaryDialog() {
        _showSummaryDialog.value = true
    }
    
    fun hideSummaryDialog() {
        _showSummaryDialog.value = false
        // Clear the pending summary date when dialog is dismissed
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                studyRepository.setPendingSummaryDate(user.id, null)
            }
        }
    }
    
    fun checkAndShowPendingSummary() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user?.pendingSummaryDate != null) {
                // Show summary dialog if there's a pending summary
                _showSummaryDialog.value = true
            }
        }
    }
    
    // Test method to manually trigger summary (for testing without waiting for midnight)
    fun testTriggerSummary() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                val yesterday = LocalDate.now().minusDays(1)
                studyRepository.setPendingSummaryDate(user.id, yesterday.toString())
                android.util.Log.d("TodayViewModel", "Test: Triggered summary for date $yesterday")
            }
        }
    }
    
    // Test method to trigger the full daily summary worker (for testing complete flow)
    fun testTriggerWorker() {
        if (notificationService.hasNotificationPermission()) {
            notificationService.testTriggerDailySummary()
            android.util.Log.d("TodayViewModel", "Test: Triggered full daily summary worker")
        } else {
            android.util.Log.d("TodayViewModel", "Test: Requesting notification permission first")
            com.example.studyblocks.MainActivity.requestNotificationPermission()
        }
    }
    
    // Test method to show a simple notification (for testing permissions)
    fun testShowNotification() {
        android.util.Log.d("TodayViewModel", "Test: testShowNotification() called")
        
        val hasPermission = notificationService.hasNotificationPermission()
        android.util.Log.d("TodayViewModel", "Test: hasNotificationPermission = $hasPermission")
        
        if (hasPermission) {
            android.util.Log.d("TodayViewModel", "Test: Permission granted, showing notification")
            notificationService.testShowSimpleNotification()
        } else {
            android.util.Log.d("TodayViewModel", "Test: No permission - will attempt to show notification anyway (may trigger system permission dialog)")
            // For Android 13+, trying to show a notification without permission should trigger the system permission dialog
            notificationService.testShowSimpleNotification()
        }
    }
    
    fun addCustomBlock(subjectId: String, durationMinutes: Int) {
        viewModelScope.launch {
            val user = _currentUser.value
            val subject = allSubjects.value.find { it.id == subjectId }
            
            if (user != null && subject != null) {
                val customBlock = StudyBlock(
                    id = java.util.UUID.randomUUID().toString(),
                    subjectId = subjectId,
                    subjectName = subject.name,
                    subjectIcon = subject.icon,
                    blockNumber = 1,
                    durationMinutes = durationMinutes,
                    scheduledDate = _selectedDate.value,
                    userId = user.id,
                    spacedRepetitionInterval = 1,
                    totalBlocksForSubject = 1,
                    isCustomBlock = true
                )
                
                try {
                    studyRepository.insertStudyBlock(customBlock)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    fun rescheduleWithMissedBlocks() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            
            _isRescheduling.value = true
            try {
                studyRepository.rescheduleWithMissedBlocks(user.id)
            } catch (e: Exception) {
                // Handle error - could show a snackbar or error dialog
                println("Failed to reschedule missed blocks: ${e.message}")
            } finally {
                _isRescheduling.value = false
            }
        }
    }
    
    fun hasOverdueBlocks(): Boolean {
        return studyBlocksForSelectedDate.value.any { it.isOverdue }
    }
    
    fun setNavigationCallback(onNavigate: () -> Unit) {
        onNavigateToConfidenceReevaluation = onNavigate
    }
    
    fun setDailySummaryNavigationCallback(onNavigate: () -> Unit) {
        onNavigateToDailySummary = onNavigate
    }
    
    
    fun updateSubjectConfidences(confidenceUpdates: Map<String, Int>) {
        viewModelScope.launch {
            try {
                confidenceUpdates.forEach { (subjectId, confidence) ->
                    studyRepository.updateSubjectConfidence(subjectId, confidence)
                }
                // Reset the dialog flag so it can show again for future schedules
                hasShownConfidenceDialog = false
            } catch (e: Exception) {
                // Handle error
                println("Failed to update subject confidences: ${e.message}")
            }
        }
    }
    
    // Reschedule functionality
    fun showRescheduleDialog(block: StudyBlock) {
        _showRescheduleDialog.value = block
    }
    
    fun hideRescheduleDialog() {
        _showRescheduleDialog.value = null
    }
    
    fun rescheduleBlock(
        block: StudyBlock,
        option: com.example.studyblocks.ui.components.RescheduleOption,
        customDate: LocalDate? = null,
        customTime: LocalTime? = null
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            
            _isRescheduling.value = true
            try {
                val targetDate = when (option) {
                    com.example.studyblocks.ui.components.RescheduleOption.LATER_TODAY -> {
                        LocalDate.now()
                    }
                    com.example.studyblocks.ui.components.RescheduleOption.TODAY -> {
                        LocalDate.now()
                    }
                    com.example.studyblocks.ui.components.RescheduleOption.TOMORROW -> {
                        LocalDate.now().plusDays(1)
                    }
                    com.example.studyblocks.ui.components.RescheduleOption.CUSTOM_TIME -> {
                        customDate ?: LocalDate.now().plusDays(1)
                    }
                }
                
                // Store for undo functionality
                lastRescheduleOperation = RescheduleOperation(
                    blockId = block.id,
                    originalDate = block.scheduledDate,
                    newDate = targetDate,
                    option = option
                )
                
                // Update the block's scheduled date
                studyRepository.rescheduleBlock(block.id, targetDate)
                
                // Show undo snackbar
                val optionText = when (option) {
                    com.example.studyblocks.ui.components.RescheduleOption.LATER_TODAY -> "later today"
                    com.example.studyblocks.ui.components.RescheduleOption.TODAY -> "to today"
                    com.example.studyblocks.ui.components.RescheduleOption.TOMORROW -> "tomorrow"
                    com.example.studyblocks.ui.components.RescheduleOption.CUSTOM_TIME -> {
                        val formatter = DateTimeFormatter.ofPattern("MMM d")
                        "to ${targetDate.format(formatter)}"
                    }
                }
                
                _undoMessage.value = "Block rescheduled $optionText"
                _undoSnackbarVisible.value = true
                
                // Auto-hide snackbar after 5 seconds
                kotlinx.coroutines.delay(5000)
                _undoSnackbarVisible.value = false
                
            } catch (e: Exception) {
                println("Failed to reschedule block: ${e.message}")
            } finally {
                _isRescheduling.value = false
                hideRescheduleDialog()
            }
        }
    }
    
    fun undoReschedule() {
        viewModelScope.launch {
            val operation = lastRescheduleOperation ?: return@launch
            
            try {
                // Restore the original date
                studyRepository.rescheduleBlock(operation.blockId, operation.originalDate)
                
                // Clear the operation
                lastRescheduleOperation = null
                
                // Hide snackbar
                _undoSnackbarVisible.value = false
                
            } catch (e: Exception) {
                println("Failed to undo reschedule: ${e.message}")
            }
        }
    }
    
    fun dismissUndoSnackbar() {
        _undoSnackbarVisible.value = false
        lastRescheduleOperation = null
    }
    
    // Get completed blocks count for a subject on the selected date
    fun getCompletedBlocksForSubject(subjectId: String): Int {
        return studyBlocksForSelectedDate.value
            .filter { it.subjectId == subjectId && it.isCompleted }
            .size
    }
    
    // Get total blocks count for a subject on the selected date
    fun getTotalBlocksForSubject(subjectId: String): Int {
        return studyBlocksForSelectedDate.value
            .filter { it.subjectId == subjectId }
            .size
    }
}

data class WeekDate(
    val date: LocalDate,
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class CompletionStats(
    val total: Int = 0,
    val completed: Int = 0,
    val available: Int = 0,
    val overdue: Int = 0,
    val completionPercentage: Int = 0
)

data class RescheduleOperation(
    val blockId: String,
    val originalDate: LocalDate,
    val newDate: LocalDate,
    val option: com.example.studyblocks.ui.components.RescheduleOption
)