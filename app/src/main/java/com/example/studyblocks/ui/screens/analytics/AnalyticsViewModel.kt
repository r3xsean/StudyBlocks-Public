package com.example.studyblocks.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.StudySession
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.User
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.THIS_WEEK)
    val selectedPeriod = _selectedPeriod.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    val weeklyProgress: StateFlow<List<DailyProgress>> = combine(
        _currentUser,
        _selectedPeriod
    ) { user, period ->
        Pair(user, period)
    }.flatMapLatest { (user, period) ->
        if (user != null) {
            val (startDate, endDate) = getPeriodDates(period)
            studyRepository.getBlocksForDateRange(user.id, startDate, endDate).map { blocks ->
                generateDailyProgress(blocks, startDate, endDate)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val subjectBreakdown: StateFlow<List<SubjectProgress>> = combine(
        _currentUser,
        _selectedPeriod
    ) { user, period ->
        Pair(user, period)
    }.flatMapLatest { (user, period) ->
        if (user != null) {
            val (startDate, endDate) = getPeriodDates(period)
            combine(
                studyRepository.getAllSubjects(user.id),
                studyRepository.getBlocksForDateRange(user.id, startDate, endDate)
            ) { subjects, blocks ->
                generateSubjectBreakdown(subjects, blocks)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val overallStats: StateFlow<OverallStats> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            combine(
                studyRepository.getTotalStudyMinutesFlow(user.id),
                studyRepository.getTotalCompletedBlocksFlow(user.id),
                studyRepository.getAllSessions(user.id)
            ) { totalMinutes, totalBlocks, sessions ->
                val completedSessions = sessions.filter { it.isCompleted }
                val averageSessionDuration = if (completedSessions.isNotEmpty()) {
                    completedSessions.mapNotNull { it.actualDurationMinutes }.average()
                } else 0.0
                
                val longestStreak = calculateLongestStreak(sessions)
                val currentStreak = calculateCurrentStreak(sessions)
                
                OverallStats(
                    totalMinutesStudied = totalMinutes,
                    totalBlocksCompleted = totalBlocks,
                    averageSessionDuration = averageSessionDuration,
                    longestStreak = longestStreak,
                    currentStreak = currentStreak,
                    totalSessions = completedSessions.size
                )
            }
        } else {
            flowOf(OverallStats())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverallStats()
    )
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    fun selectPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
    }
    
    private fun getPeriodDates(period: AnalyticsPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (period) {
            AnalyticsPeriod.THIS_WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
                Pair(startOfWeek, startOfWeek.plusDays(6))
            }
            AnalyticsPeriod.LAST_WEEK -> {
                val lastWeekStart = today.minusDays(today.dayOfWeek.value + 6L)
                Pair(lastWeekStart, lastWeekStart.plusDays(6))
            }
            AnalyticsPeriod.THIS_MONTH -> {
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)
                Pair(startOfMonth, endOfMonth)
            }
            AnalyticsPeriod.LAST_30_DAYS -> {
                Pair(today.minusDays(29), today)
            }
        }
    }
    
    private fun generateDailyProgress(
        blocks: List<StudyBlock>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyProgress> {
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        
        return (0 until daysBetween).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayBlocks = blocks.filter { it.scheduledDate == date }
            val completedBlocks = dayBlocks.filter { it.isCompleted }
            
            DailyProgress(
                date = date,
                totalBlocks = dayBlocks.size,
                completedBlocks = completedBlocks.size,
                completionRate = if (dayBlocks.isNotEmpty()) {
                    (completedBlocks.size.toFloat() / dayBlocks.size * 100).toInt()
                } else 0,
                totalMinutes = completedBlocks.sumOf { it.durationMinutes }
            )
        }
    }
    
    private fun generateSubjectBreakdown(
        subjects: List<Subject>,
        blocks: List<StudyBlock>
    ): List<SubjectProgress> {
        return subjects.map { subject ->
            val subjectBlocks = blocks.filter { it.subjectId == subject.id }
            val completedBlocks = subjectBlocks.filter { it.isCompleted }
            
            SubjectProgress(
                subject = subject,
                totalBlocks = subjectBlocks.size,
                completedBlocks = completedBlocks.size,
                completionRate = if (subjectBlocks.isNotEmpty()) {
                    (completedBlocks.size.toFloat() / subjectBlocks.size * 100).toInt()
                } else 0,
                totalMinutes = completedBlocks.sumOf { it.durationMinutes },
                averageConfidence = subject.confidence
            )
        }.sortedByDescending { it.completedBlocks }
    }
    
    private fun calculateLongestStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        
        val completedDates = sessions
            .filter { it.isCompleted }
            .map { it.startTime.toLocalDate() }
            .distinct()
            .sorted()
        
        var longestStreak = 1
        var currentStreakLength = 1
        
        for (i in 1 until completedDates.size) {
            val prevDate = completedDates[i - 1]
            val currentDate = completedDates[i]
            
            if (ChronoUnit.DAYS.between(prevDate, currentDate) == 1L) {
                currentStreakLength++
                longestStreak = maxOf(longestStreak, currentStreakLength)
            } else {
                currentStreakLength = 1
            }
        }
        
        return longestStreak
    }
    
    private fun calculateCurrentStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        
        val today = LocalDate.now()
        val completedDates = sessions
            .filter { it.isCompleted }
            .map { it.startTime.toLocalDate() }
            .distinct()
            .sorted()
            .reversed()
        
        var streak = 0
        var checkDate = today
        
        for (date in completedDates) {
            if (date == checkDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (date == checkDate.minusDays(1) && streak == 0) {
                // Allow for yesterday if no study today
                streak++
                checkDate = checkDate.minusDays(2)
            } else {
                break
            }
        }
        
        return streak
    }
}

enum class AnalyticsPeriod(val displayName: String) {
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days")
}

data class DailyProgress(
    val date: LocalDate,
    val totalBlocks: Int,
    val completedBlocks: Int,
    val completionRate: Int,
    val totalMinutes: Int
)

data class SubjectProgress(
    val subject: Subject,
    val totalBlocks: Int,
    val completedBlocks: Int,
    val completionRate: Int,
    val totalMinutes: Int,
    val averageConfidence: Int
)

data class OverallStats(
    val totalMinutesStudied: Int = 0,
    val totalBlocksCompleted: Int = 0,
    val averageSessionDuration: Double = 0.0,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0,
    val totalSessions: Int = 0
) {
    val hoursStudied: Double
        get() = totalMinutesStudied / 60.0
}