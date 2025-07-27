package com.example.studyblocks.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.*
import com.example.studyblocks.repository.StudyRepository
import com.example.studyblocks.repository.LevelPrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    // Removed period selector - focusing on all-time analytics
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // XP-based analytics
    val xpProgression: StateFlow<List<XPDataPoint>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getXPProgressionFlow(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Detailed XP breakdown
    val detailedXPData: StateFlow<Map<String, Any>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getDetailedXPBreakdown(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // Subject XP breakdown
    val xpPerSubject: StateFlow<Map<String, Int>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getXPEarnedPerSubject(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // Confidence vs Performance correlation
    val confidencePerformance: StateFlow<Map<String, Any>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getConfidenceVsPerformance(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // All-time study overview (no schedule-based periods)
    val studyOverview: StateFlow<StudyOverview> = _currentUser.filterNotNull().flatMapLatest { user ->
        combine(
            studyRepository.getAllSubjects(user.id),
            studyRepository.getBlocksForDateRange(user.id, LocalDate.MIN, LocalDate.MAX),
            studyRepository.getTotalStudyMinutesFlow(user.id)
        ) { subjects, allBlocks, totalMinutes ->
            val completed = allBlocks.count { it.isCompleted }
            val totalXP = allBlocks.filter { it.isCompleted }
                .sumOf { it.durationMinutes * 100 / 60 } // Basic XP calculation
            
            StudyOverview(
                totalBlocks = allBlocks.size,
                completedBlocks = completed,
                totalXPEarned = totalXP,
                totalMinutesStudied = totalMinutes,
                subjects = subjects.size,
                totalHours = totalMinutes / 60,
                averageSessionMinutes = if (completed > 0) totalMinutes / completed else 0
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyOverview()
    )
    
    // Global statistics for the schedule period
    val globalStats: StateFlow<GlobalStats> = _currentUser.filterNotNull().flatMapLatest { user ->
        combine(
            studyRepository.getTotalStudyMinutesFlow(user.id),
            studyRepository.getTotalCompletedBlocksFlow(user.id),
            studyRepository.getCurrentUserFlow()
        ) { totalMinutes, totalBlocks, currentUser ->
            GlobalStats(
                globalXP = currentUser?.globalXp ?: 0,
                globalLevel = currentUser?.globalLevel ?: 1,
                totalMinutesStudied = totalMinutes,
                totalBlocksCompleted = totalBlocks,
                averageXPPerBlock = if (totalBlocks > 0) totalMinutes * 100 / 60 / totalBlocks else 0
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalStats()
    )
    
    // Subject analytics with confidence-based insights - using actual study session data
    val subjectAnalytics: StateFlow<List<SubjectAnalyticsData>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getAllSubjects(user.id).flatMapLatest { subjects ->
            // Create flows for each subject's data
            val subjectDataFlows = subjects.map { subject ->
                combine(
                    flow { emit(studyRepository.getStudyMinutesForSubject(subject.id)) },
                    studyRepository.getBlocksForSubject(subject.id).map { blocks -> 
                        blocks.filter { it.isCompleted }.size 
                    }
                ) { totalMinutesStudied, blocksCompleted ->
                    SubjectAnalyticsData(
                        name = subject.name,
                        icon = subject.icon,
                        confidence = subject.confidence,
                        totalXP = subject.xp,
                        level = subject.level,
                        blocksCompleted = blocksCompleted,
                        totalMinutesStudied = totalMinutesStudied,
                        averageSessionMinutes = if (blocksCompleted > 0) {
                            totalMinutesStudied / blocksCompleted
                        } else 0
                    )
                }
            }
            
            if (subjectDataFlows.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(subjectDataFlows) { analyticsArray ->
                    analyticsArray.toList().sortedByDescending { it.totalXP }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Most productive hours analytics
    val productiveHours: StateFlow<Map<Int, Double>> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getProductivityByHour(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // Study streak analytics
    val studyStreak: StateFlow<StudyStreak> = _currentUser.filterNotNull().flatMapLatest { user ->
        studyRepository.getStudyStreak(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyStreak(userId = "")
    )
    
    // Level predictions for current schedule
    val levelPredictions: StateFlow<Map<String, LevelPrediction>> = _currentUser.filterNotNull().flatMapLatest { user ->
        // Get current active schedule blocks and calculate predictions
        studyRepository.getBlocksForDateRange(user.id, LocalDate.now(), LocalDate.now().plusDays(21)).flatMapLatest { blocks ->
            if (blocks.isNotEmpty()) {
                studyRepository.calculateScheduleLevelPredictions(user.id, blocks)
            } else {
                flowOf(emptyMap())
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Weekly completion percentages by day of the week (aggregated across all weeks)
    val weeklyCompletionData: StateFlow<List<Triple<String, Int, Int>>> = _currentUser.filterNotNull().flatMapLatest { user ->
        // Get blocks from the last 30 days to have enough data for meaningful day-of-week analysis
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29) // Last 30 days
        
        studyRepository.getBlocksForDateRange(user.id, startDate, endDate).map { blocks ->
            val dayAbbreviations = mapOf(
                1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 
                5 to "Fri", 6 to "Sat", 7 to "Sun"
            )
            
            // Group blocks by day of week (not specific date)
            val blocksByDayOfWeek = blocks.groupBy { it.scheduledDate.dayOfWeek.value }
            
            // Calculate completion data for each day of the week
            // Ensure we always return all 7 days in correct order
            listOf(1, 2, 3, 4, 5, 6, 7).map { dayOfWeek ->
                val dayAbbr = dayAbbreviations[dayOfWeek] ?: "Unknown"
                val blocksForDay = blocksByDayOfWeek[dayOfWeek] ?: emptyList()
                
                val totalBlocks = blocksForDay.size
                val completedBlocks = blocksForDay.count { it.isCompleted }
                val completionPercentage = if (totalBlocks > 0) {
                    (completedBlocks * 100) / totalBlocks
                } else 0
                
                // Return: (day, total blocks, completion percentage)
                Triple(dayAbbr, totalBlocks, completionPercentage)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }

    // Removed period selection - analytics are now all-time focused

    fun refreshData() {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                _isLoading.value = true
                // Trigger flows to recompute - handled automatically by StateFlow
                _isLoading.value = false
            }
        }
    }
}

// Data models for the new analytics

data class StudyOverview(
    val totalBlocks: Int = 0,
    val completedBlocks: Int = 0,
    val totalXPEarned: Int = 0,
    val totalMinutesStudied: Int = 0,
    val totalHours: Int = 0,
    val subjects: Int = 0,
    val averageSessionMinutes: Int = 0
)

data class GlobalStats(
    val globalXP: Int = 0,
    val globalLevel: Int = 1,
    val totalMinutesStudied: Int = 0,
    val totalBlocksCompleted: Int = 0,
    val averageXPPerBlock: Int = 0,
    val streakDays: Int = 0
)

data class SubjectAnalyticsData(
    val name: String,
    val icon: String,
    val confidence: Int,
    val totalXP: Int,
    val level: Int,
    val blocksCompleted: Int,
    val totalMinutesStudied: Int,
    val averageSessionMinutes: Int
)

// Removed AnalyticsPeriod - focusing on all-time analytics only