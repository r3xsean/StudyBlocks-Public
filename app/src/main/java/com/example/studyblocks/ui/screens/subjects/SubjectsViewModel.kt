package com.example.studyblocks.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.SubjectIcon
import com.example.studyblocks.data.model.SubjectIconMatcher
import com.example.studyblocks.data.model.SubjectGrouping
import com.example.studyblocks.data.model.User
import com.example.studyblocks.data.model.SchedulePreferences
import com.example.studyblocks.repository.SchedulingResult
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects = _subjects.asStateFlow()
    
    private val _sortBy = MutableStateFlow(SortBy.NAME)
    val sortBy = _sortBy.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()
    
    private val _editingSubject = MutableStateFlow<Subject?>(null)
    val editingSubject = _editingSubject.asStateFlow()
    
    private val _isGeneratingSchedule = MutableStateFlow(false)
    val isGeneratingSchedule = _isGeneratingSchedule.asStateFlow()
    
    private val _scheduleResult = MutableStateFlow<SchedulingResult?>(null)
    val scheduleResult = _scheduleResult.asStateFlow()
    
    // Preferred blocks per day from current user
    val preferredBlocksPerDay: StateFlow<Int> = _currentUser.map { user ->
        user?.preferredBlocksPerDay ?: 3
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 3
    )
    
    // Schedule preferences for the current user
    @OptIn(ExperimentalCoroutinesApi::class)
    val schedulePreferences: StateFlow<SchedulePreferences?> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            studyRepository.getSchedulePreferencesFlow(user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        loadCurrentUser()
    }
    
    val sortedSubjects: StateFlow<List<Subject>> = combine(_subjects, _sortBy) { subjects, sort ->
        when (sort) {
            SortBy.NAME -> subjects.sortedBy { it.name }
            SortBy.CONFIDENCE -> subjects.sortedBy { it.confidence }
            SortBy.LEVEL -> subjects.sortedByDescending { it.level }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    loadSubjects(user.id)
                }
            }
        }
    }
    
    private fun loadSubjects(userId: String) {
        viewModelScope.launch {
            studyRepository.getAllSubjects(userId).collect { subjects ->
                _subjects.value = subjects
            }
        }
    }
    
    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }
    
    fun showAddSubjectDialog() {
        _showAddDialog.value = true
    }
    
    fun hideAddSubjectDialog() {
        _showAddDialog.value = false
        _editingSubject.value = null
    }
    
    fun editSubject(subject: Subject) {
        _editingSubject.value = subject
        _showAddDialog.value = true
    }
    
    fun addSubject(
        name: String,
        icon: SubjectIcon,
        confidence: Int
    ) {
        val currentUser = _currentUser.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val subject = Subject(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    icon = icon.emoji,
                    confidence = confidence,
                    blockDurationMinutes = 60, // Default 1 hour
                    userId = currentUser.id
                )
                
                studyRepository.insertSubject(subject)
                hideAddSubjectDialog()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSubject(
        subject: Subject,
        name: String,
        icon: SubjectIcon,
        confidence: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedSubject = subject.copy(
                    name = name,
                    icon = icon.emoji,
                    confidence = confidence,
                    updatedAt = LocalDateTime.now()
                )
                
                studyRepository.updateSubject(updatedSubject)
                hideAddSubjectDialog()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                studyRepository.deleteSubject(subject)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSubjectConfidence(subjectId: String, confidence: Int) {
        viewModelScope.launch {
            try {
                studyRepository.updateSubjectConfidence(subjectId, confidence)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun generateNewSchedule(
        blocksPerWeekday: Int? = null, 
        blocksPerWeekend: Int? = null, 
        horizon: Int? = null, 
        blockDuration: Int? = null,
        subjectGrouping: SubjectGrouping? = null
    ) {
        val currentUser = _currentUser.value ?: return
        
        viewModelScope.launch {
            _isGeneratingSchedule.value = true
            try {
                val result = studyRepository.generateNewSchedule(
                    userId = currentUser.id,
                    blocksPerWeekday = blocksPerWeekday ?: currentUser.preferredBlocksPerDay,
                    blocksPerWeekend = blocksPerWeekend ?: 2,
                    scheduleHorizon = horizon ?: 21,
                    blockDurationMinutes = blockDuration ?: 60,
                    subjectGrouping = subjectGrouping
                )
                _scheduleResult.value = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isGeneratingSchedule.value = false
            }
        }
    }
    
    fun clearScheduleResult() {
        _scheduleResult.value = null
    }
    
    fun getSuggestedIcon(name: String): SubjectIcon {
        return SubjectIconMatcher.getIconForSubject(name)
    }
    
    fun updatePreferredBlocksPerDay(blocks: Int) {
        val currentUser = _currentUser.value ?: return
        
        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(preferredBlocksPerDay = blocks)
                studyRepository.updateUser(updatedUser)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

enum class SortBy(val displayName: String) {
    NAME("By Name"),
    CONFIDENCE("By Confidence"),
    LEVEL("By Level")
}

data class SubjectFormState(
    val name: String = "",
    val selectedIcon: SubjectIcon = SubjectIcon.DEFAULT,
    val confidence: Int = 5,
    val nameError: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() && nameError == null
}