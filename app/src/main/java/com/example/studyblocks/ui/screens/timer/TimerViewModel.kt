package com.example.studyblocks.ui.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.StudySession
import com.example.studyblocks.data.model.User
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState = _timerState.asStateFlow()
    
    private val _selectedBlock = MutableStateFlow<StudyBlock?>(null)
    val selectedBlock = _selectedBlock.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(0L) // seconds
    val remainingTime = _remainingTime.asStateFlow()
    
    private val _totalTime = MutableStateFlow(0L) // seconds
    val totalTime = _totalTime.asStateFlow()
    
    private val _currentSession = MutableStateFlow<StudySession?>(null)
    val currentSession = _currentSession.asStateFlow()
    
    private val _breakTime = MutableStateFlow(0L) // seconds
    val breakTime = _breakTime.asStateFlow()
    
    private val _breakType = MutableStateFlow(BreakType.NONE)
    val breakType = _breakType.asStateFlow()
    
    private val _focusScore = MutableStateFlow(5)
    val focusScore = _focusScore.asStateFlow()
    
    private val _showFocusDialog = MutableStateFlow(false)
    val showFocusDialog = _showFocusDialog.asStateFlow()
    
    private val _pomodoroSettings = MutableStateFlow(
        PomodoroSettings(
            workDuration = 25 * 60, // 25 minutes
            shortBreakDuration = 5 * 60, // 5 minutes
            longBreakDuration = 15 * 60, // 15 minutes
            longBreakInterval = 4
        )
    )
    val pomodoroSettings = _pomodoroSettings.asStateFlow()
    
    private val _completedPomodoros = MutableStateFlow(0)
    val completedPomodoros = _completedPomodoros.asStateFlow()
    
    private var timerJob: Job? = null
    
    init {
        loadCurrentUser()
        loadActiveSession()
    }
    
    val todayBlocks: StateFlow<List<StudyBlock>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            studyRepository.getBlocksForDate(user.id, LocalDate.now())
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val availableBlocks: StateFlow<List<StudyBlock>> = todayBlocks.map { blocks ->
        blocks.filter { !it.isCompleted && it.canComplete }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val progress: StateFlow<Float> = combine(_remainingTime, _totalTime) { remaining, total ->
        if (total > 0) {
            1f - (remaining.toFloat() / total.toFloat())
        } else 0f
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    private fun loadActiveSession() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                studyRepository.getActiveSession(user.id).collect { session ->
                    _currentSession.value = session
                    if (session != null && _timerState.value == TimerState.IDLE) {
                        // Resume existing session
                        resumeSession(session)
                    }
                }
            }
        }
    }
    
    fun selectBlock(block: StudyBlock) {
        if (_timerState.value == TimerState.IDLE) {
            _selectedBlock.value = block
            _totalTime.value = block.durationMinutes * 60L
            _remainingTime.value = _totalTime.value
        }
    }
    
    fun startTimer() {
        val user = _currentUser.value ?: return
        val block = _selectedBlock.value ?: return
        
        if (_timerState.value != TimerState.IDLE) return
        
        viewModelScope.launch {
            try {
                // Create new study session
                val session = studyRepository.startStudySession(
                    studyBlockId = block.id,
                    subjectId = block.subjectId,
                    userId = user.id,
                    plannedDuration = block.durationMinutes
                )
                _currentSession.value = session
                
                _timerState.value = TimerState.RUNNING
                startCountdown()
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }
    
    fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            _timerState.value = TimerState.RUNNING
            startCountdown()
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                studyRepository.endStudySession(session.id, _focusScore.value)
            }
            
            resetTimer()
        }
    }
    
    fun completeSession() {
        _showFocusDialog.value = true
    }
    
    fun submitFocusScore(score: Int) {
        _focusScore.value = score
        _showFocusDialog.value = false
        
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                studyRepository.endStudySession(session.id, score)
                
                // Mark block as completed
                _selectedBlock.value?.let { block ->
                    studyRepository.markBlockComplete(block.id)
                }
            }
            
            _completedPomodoros.value += 1
            resetTimer()
        }
    }
    
    fun startBreak(type: BreakType) {
        val duration = when (type) {
            BreakType.SHORT -> _pomodoroSettings.value.shortBreakDuration
            BreakType.LONG -> _pomodoroSettings.value.longBreakDuration
            BreakType.NONE -> 0
        }
        
        _breakType.value = type
        _breakTime.value = duration.toLong()
        _timerState.value = TimerState.BREAK
        _remainingTime.value = duration.toLong()
        _totalTime.value = duration.toLong()
        
        startCountdown()
    }
    
    fun skipBreak() {
        _breakType.value = BreakType.NONE
        _timerState.value = TimerState.IDLE
        resetTimer()
    }
    
    fun updatePomodoroSettings(settings: PomodoroSettings) {
        if (_timerState.value == TimerState.IDLE) {
            _pomodoroSettings.value = settings
        }
    }
    
    private fun resumeSession(session: StudySession) {
        val elapsedMinutes = ChronoUnit.MINUTES.between(session.startTime, LocalDateTime.now())
        val plannedSeconds = session.plannedDurationMinutes * 60L
        val elapsedSeconds = elapsedMinutes * 60L
        
        if (elapsedSeconds < plannedSeconds) {
            _remainingTime.value = plannedSeconds - elapsedSeconds
            _totalTime.value = plannedSeconds
            _timerState.value = TimerState.PAUSED
            
            // Try to find the associated block
            viewModelScope.launch {
                val block = studyRepository.getBlockById(session.studyBlockId)
                _selectedBlock.value = block
            }
        }
    }
    
    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0 && _timerState.value == TimerState.RUNNING || _timerState.value == TimerState.BREAK) {
                delay(1000L)
                _remainingTime.value = _remainingTime.value - 1
            }
            
            // Timer finished
            when (_timerState.value) {
                TimerState.RUNNING -> {
                    // Study session completed
                    completeSession()
                }
                TimerState.BREAK -> {
                    // Break finished
                    _timerState.value = TimerState.IDLE
                    _breakType.value = BreakType.NONE
                    resetTimer()
                }
                else -> {}
            }
        }
    }
    
    private fun resetTimer() {
        _selectedBlock.value = null
        _currentSession.value = null
        _remainingTime.value = 0L
        _totalTime.value = 0L
        _breakTime.value = 0L
    }
    
    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    fun getTimeProgress(): String {
        val elapsed = _totalTime.value - _remainingTime.value
        return "${formatTime(elapsed)} / ${formatTime(_totalTime.value)}"
    }
}

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    BREAK
}

enum class BreakType {
    NONE,
    SHORT,
    LONG
}

data class PomodoroSettings(
    val workDuration: Int = 25 * 60, // seconds
    val shortBreakDuration: Int = 5 * 60, // seconds
    val longBreakDuration: Int = 15 * 60, // seconds
    val longBreakInterval: Int = 4 // after how many pomodoros to take long break
)