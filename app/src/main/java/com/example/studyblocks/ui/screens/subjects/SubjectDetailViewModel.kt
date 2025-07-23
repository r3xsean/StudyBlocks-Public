package com.example.studyblocks.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.SubjectIcon
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _subject = MutableStateFlow<Subject?>(null)
    val subject = _subject.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog = _showEditDialog.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()
    
    val studyStats: StateFlow<StudyStats> = _subject.flatMapLatest { subject ->
        if (subject != null) {
            studyRepository.getBlocksForSubject(subject.id).map { blocks ->
                StudyStats(
                    totalBlocks = blocks.size,
                    completedBlocks = blocks.count { it.isCompleted },
                    totalMinutes = blocks.filter { it.isCompleted }
                        .sumOf { it.durationMinutes }
                )
            }
        } else {
            flowOf(StudyStats())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyStats()
    )
    
    fun loadSubject(subjectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val subject = studyRepository.getSubjectById(subjectId)
                _subject.value = subject
            } catch (e: Exception) {
                // Handle error
                _subject.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun showEditDialog() {
        _showEditDialog.value = true
    }
    
    fun hideEditDialog() {
        _showEditDialog.value = false
    }
    
    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }
    
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }
    
    fun updateSubject(name: String, icon: SubjectIcon) {
        val currentSubject = _subject.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedSubject = currentSubject.copy(
                    name = name,
                    icon = icon.emoji,
                    updatedAt = LocalDateTime.now()
                )
                
                studyRepository.updateSubject(updatedSubject)
                _subject.value = updatedSubject
                hideEditDialog()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateConfidence(confidence: Int) {
        val currentSubject = _subject.value ?: return
        
        viewModelScope.launch {
            try {
                val updatedSubject = currentSubject.copy(
                    confidence = confidence,
                    updatedAt = LocalDateTime.now()
                )
                
                studyRepository.updateSubject(updatedSubject)
                _subject.value = updatedSubject
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    
    fun deleteSubject() {
        val currentSubject = _subject.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                studyRepository.deleteSubject(currentSubject)
                hideDeleteDialog()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}