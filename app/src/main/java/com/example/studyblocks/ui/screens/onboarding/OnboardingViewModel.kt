package com.example.studyblocks.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.OnboardingSchedulePreferences
import com.example.studyblocks.data.model.SchedulePreferences
import com.example.studyblocks.data.model.SubjectGrouping
import com.example.studyblocks.repository.StudyRepository
import com.example.studyblocks.repository.SchedulingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val userDao: UserDao
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _onboardingComplete = MutableStateFlow(false)
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _scheduleResult = MutableStateFlow<SchedulingResult?>(null)
    val scheduleResult: StateFlow<SchedulingResult?> = _scheduleResult.asStateFlow()
    
    private val _currentBlockDuration = MutableStateFlow(60)
    val currentBlockDuration: StateFlow<Int> = _currentBlockDuration.asStateFlow()
    
    private var pendingSubjects: List<Subject> = emptyList()
    private var pendingScheduleHorizonWeeks: Int = 3
    private var pendingBlocksPerWeekday: Int = 3
    private var pendingBlocksPerWeekend: Int = 2
    private var pendingBlockDurationMinutes: Int = 60
    private var pendingSubjectGrouping: SubjectGrouping = SubjectGrouping.BALANCED
    
    init {
        println("DEBUG OnboardingViewModel: OnboardingViewModel initialized - Instance: ${this.hashCode()}")
    }
    
    fun setSubjects(subjects: List<Subject>) {
        pendingSubjects = subjects
        println("DEBUG OnboardingViewModel: Set ${subjects.size} pending subjects - Instance: ${this.hashCode()}")
        subjects.forEach { println("DEBUG OnboardingViewModel: Subject: ${it.name}") }
    }
    
    fun setScheduleHorizonWeeks(weeks: Int) {
        pendingScheduleHorizonWeeks = weeks
        println("DEBUG OnboardingViewModel: Set schedule horizon: $weeks weeks - Instance: ${this.hashCode()}")
    }
    
    fun setDailyBlocks(weekdayBlocks: Int, weekendBlocks: Int) {
        pendingBlocksPerWeekday = weekdayBlocks
        pendingBlocksPerWeekend = weekendBlocks
        println("DEBUG OnboardingViewModel: Set daily blocks: weekday=$weekdayBlocks, weekend=$weekendBlocks - Instance: ${this.hashCode()}")
    }
    
    fun setBlockDuration(durationMinutes: Int) {
        pendingBlockDurationMinutes = durationMinutes
        _currentBlockDuration.value = durationMinutes
        println("DEBUG OnboardingViewModel: Set block duration: $durationMinutes minutes - Instance: ${this.hashCode()}")
    }
    
    fun setSubjectGrouping(grouping: SubjectGrouping) {
        pendingSubjectGrouping = grouping
        println("DEBUG OnboardingViewModel: Set subject grouping: $grouping - Instance: ${this.hashCode()}")
    }
    
    @Deprecated("Use individual setters instead")
    fun setSchedulePreferences(preferences: OnboardingSchedulePreferences) {
        // Keep for backward compatibility if needed
        pendingScheduleHorizonWeeks = preferences.scheduleHorizonDays / 7
        pendingBlocksPerWeekday = preferences.blocksPerWeekday
        pendingBlocksPerWeekend = preferences.blocksPerWeekend
        pendingBlockDurationMinutes = preferences.defaultBlockDurationMinutes
        pendingSubjectGrouping = preferences.subjectGrouping
        println("DEBUG OnboardingViewModel: Set schedule preferences (deprecated): $preferences - Instance: ${this.hashCode()}")
    }
    
    fun completeOnboarding(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                println("DEBUG OnboardingViewModel: Starting onboarding completion for userId: $userId - Instance: ${this.hashCode()}")
                println("DEBUG OnboardingViewModel: pendingSubjects count: ${pendingSubjects.size}")
                println("DEBUG OnboardingViewModel: pendingScheduleHorizonWeeks: $pendingScheduleHorizonWeeks")
                println("DEBUG OnboardingViewModel: pendingBlocksPerWeekday: $pendingBlocksPerWeekday")
                println("DEBUG OnboardingViewModel: pendingBlocksPerWeekend: $pendingBlocksPerWeekend")
                println("DEBUG OnboardingViewModel: pendingBlockDurationMinutes: $pendingBlockDurationMinutes")
                println("DEBUG OnboardingViewModel: pendingSubjectGrouping: $pendingSubjectGrouping")
                
                // Save subjects with userId
                val subjectsWithUserId = pendingSubjects.map { subject ->
                    subject.copy(userId = userId)
                }
                
                println("DEBUG OnboardingViewModel: Saving ${subjectsWithUserId.size} subjects")
                // Insert all subjects in a single transaction for better reliability
                studyRepository.insertSubjects(subjectsWithUserId)
                println("DEBUG OnboardingViewModel: Batch saved ${subjectsWithUserId.size} subjects")
                
                // Save schedule preferences
                val schedulePreferences = SchedulePreferences(
                    userId = userId,
                    scheduleHorizonDays = pendingScheduleHorizonWeeks * 7,
                    blocksPerWeekday = pendingBlocksPerWeekday,
                    blocksPerWeekend = pendingBlocksPerWeekend,
                    defaultBlockDurationMinutes = pendingBlockDurationMinutes,
                    subjectGrouping = pendingSubjectGrouping
                )
                studyRepository.insertSchedulePreferences(schedulePreferences)
                println("DEBUG OnboardingViewModel: Saved schedule preferences: $schedulePreferences")
                
                // Update user to mark onboarding as complete
                val currentUser = userDao.getUserById(userId)
                println("DEBUG OnboardingViewModel: Current user before update: $currentUser")
                currentUser?.let { user ->
                    val updatedUser = user.copy(hasCompletedOnboarding = true)
                    userDao.insertUser(updatedUser)
                    println("DEBUG OnboardingViewModel: Updated user to: $updatedUser")
                } ?: run {
                    // If no user exists with this ID, update the current user (fallback for open source version)
                    println("DEBUG OnboardingViewModel: No user found with ID $userId, trying to update current user")
                    val fallbackUser = userDao.getCurrentUser()
                    fallbackUser?.let { user ->
                        val updatedUser = user.copy(hasCompletedOnboarding = true)
                        userDao.insertUser(updatedUser)
                        println("DEBUG OnboardingViewModel: Updated fallback user to: $updatedUser")
                    }
                }
                
                // Generate initial schedule after subjects and preferences are saved
                if (subjectsWithUserId.isNotEmpty()) {
                    try {
                        println("DEBUG OnboardingViewModel: Generating schedule for ${subjectsWithUserId.size} subjects")
                        
                        // Add a small delay to ensure all database operations are completed
                        kotlinx.coroutines.delay(500)
                        
                        // Verify subjects were saved by re-querying
                        val savedSubjects = studyRepository.getAllSubjects(userId).first()
                        println("DEBUG OnboardingViewModel: Verified ${savedSubjects.size} subjects in database")
                        
                        if (savedSubjects.isNotEmpty()) {
                            val scheduleResult = studyRepository.generateNewSchedule(userId)
                            _scheduleResult.value = scheduleResult
                            println("DEBUG OnboardingViewModel: Schedule generation completed with ${scheduleResult.totalBlocks} blocks")
                        } else {
                            println("DEBUG OnboardingViewModel: No subjects found in database after save - schedule generation skipped")
                        }
                    } catch (scheduleError: Exception) {
                        // Log but don't fail onboarding if schedule generation fails
                        // The user can regenerate schedule later from subjects screen
                        println("DEBUG OnboardingViewModel: Schedule generation failed: ${scheduleError.message}")
                        scheduleError.printStackTrace()
                    }
                } else {
                    println("DEBUG OnboardingViewModel: No subjects to save - schedule generation skipped")
                }
                
                _onboardingComplete.value = true
                println("DEBUG OnboardingViewModel: Onboarding completion set to true")
                
            } catch (e: Exception) {
                _error.value = "Failed to complete onboarding: ${e.message}"
                println("DEBUG OnboardingViewModel: Error completing onboarding: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                println("DEBUG OnboardingViewModel: Onboarding completion process finished")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearScheduleResult() {
        _scheduleResult.value = null
    }
    
    fun resetOnboardingState() {
        _onboardingComplete.value = false
        _scheduleResult.value = null
        pendingSubjects = emptyList()
        pendingScheduleHorizonWeeks = 3
        pendingBlocksPerWeekday = 3
        pendingBlocksPerWeekend = 2
        pendingBlockDurationMinutes = 60
        pendingSubjectGrouping = SubjectGrouping.BALANCED
    }
}