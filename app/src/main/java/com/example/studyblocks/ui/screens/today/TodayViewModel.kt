package com.example.studyblocks.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.User
import com.example.studyblocks.data.model.getStatus
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _showCustomBlockDialog = MutableStateFlow(false)
    val showCustomBlockDialog = _showCustomBlockDialog.asStateFlow()
    
    private val _weekDates = MutableStateFlow<List<WeekDate>>(emptyList())
    val weekDates = _weekDates.asStateFlow()
    
    // XP animation tracking
    data class XPAnimation(val blockId: String, val xpChange: Int, val tapX: Float, val tapY: Float, val timestamp: Long)
    private val _xpAnimations = MutableStateFlow<List<XPAnimation>>(emptyList())
    val xpAnimations = _xpAnimations.asStateFlow()
    
    init {
        loadCurrentUser()
        generateWeekDates()
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
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
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
    
    fun toggleBlockCompletion(block: StudyBlock, tapX: Float, tapY: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val xpChange = if (block.isCompleted) {
                    val xp = studyRepository.markBlockIncomplete(block.id)
                    -xp // Negative for subtraction
                } else {
                    studyRepository.markBlockComplete(block.id)
                }
                
                // Add XP animation with tap coordinates
                if (xpChange != 0) {
                    val newXPAnimation = XPAnimation(block.id, xpChange, tapX, tapY, System.currentTimeMillis())
                    _xpAnimations.value = _xpAnimations.value + newXPAnimation
                    
                    // Remove after animation duration
                    kotlinx.coroutines.delay(2000)
                    _xpAnimations.value = _xpAnimations.value.filter { it.timestamp != newXPAnimation.timestamp }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearXPAnimation(timestamp: Long) {
        _xpAnimations.value = _xpAnimations.value.filter { it.timestamp != timestamp }
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