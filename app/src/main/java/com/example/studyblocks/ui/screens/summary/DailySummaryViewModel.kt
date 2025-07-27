package com.example.studyblocks.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.User
import com.example.studyblocks.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class DailySummaryData(
    val date: LocalDate = LocalDate.now().minusDays(1),
    val completedBlocks: List<StudyBlock> = emptyList(),
    val missedBlocks: List<StudyBlock> = emptyList(),
    val tomorrowBlocks: List<StudyBlock> = emptyList(),
    val rescheduledByUserCount: Int = 0,
    val rescheduledDueToMissedCount: Int = 0,
    val totalBlocks: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _summaryData = MutableStateFlow(DailySummaryData())
    val summaryData = _summaryData.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            studyRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    loadSummaryData(user.id)
                }
            }
        }
    }
    
    private fun loadSummaryData(userId: String, date: LocalDate = LocalDate.now().minusDays(1)) {
        viewModelScope.launch {
            _summaryData.value = _summaryData.value.copy(isLoading = true)
            
            try {
                // Get all blocks for the specified date
                val allBlocksForDate = studyRepository.getBlocksForDate(userId, date).first()
                
                // Get completed blocks for the date
                val completedBlocks = allBlocksForDate.filter { it.isCompleted }
                
                // Get missed blocks - blocks scheduled for this date but not completed and now overdue
                val missedBlocks = allBlocksForDate.filter { 
                    !it.isCompleted && it.scheduledDate.isBefore(LocalDate.now())
                }
                
                // Get tomorrow's blocks
                val tomorrowDate = LocalDate.now()
                val tomorrowBlocks = studyRepository.getBlocksForDate(userId, tomorrowDate).first()
                
                // Calculate rescheduled blocks
                // For now, we'll use a simplified approach - in a full implementation,
                // you'd track these in the database with specific flags
                val rescheduledDueToMissedCount = calculateRescheduledDueToMissed(userId, date)
                val rescheduledByUserCount = calculateRescheduledByUser(userId, date)
                
                _summaryData.value = DailySummaryData(
                    date = date,
                    completedBlocks = completedBlocks,
                    missedBlocks = missedBlocks,
                    tomorrowBlocks = tomorrowBlocks,
                    rescheduledByUserCount = rescheduledByUserCount,
                    rescheduledDueToMissedCount = rescheduledDueToMissedCount,
                    totalBlocks = allBlocksForDate.size,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                android.util.Log.e("DailySummaryViewModel", "Error loading summary data", e)
                _summaryData.value = _summaryData.value.copy(isLoading = false)
            }
        }
    }
    
    // Calculate blocks that were rescheduled due to being missed
    private suspend fun calculateRescheduledDueToMissed(userId: String, date: LocalDate): Int {
        // This is a simplified calculation. In a production app, you'd track this more precisely
        // by storing reschedule reasons in the database
        return try {
            val overdueBlocks = studyRepository.getOverdueBlocks(userId).first()
            // Count blocks that were originally scheduled for dates before the summary date
            overdueBlocks.count { block ->
                block.scheduledDate.isBefore(date) && !block.isCompleted
            }
        } catch (e: Exception) {
            android.util.Log.e("DailySummaryViewModel", "Error calculating rescheduled due to missed", e)
            0
        }
    }
    
    // Calculate blocks that were manually rescheduled by the user
    private suspend fun calculateRescheduledByUser(userId: String, date: LocalDate): Int {
        // This would require additional tracking in the database to distinguish between
        // user-initiated reschedules vs automatic reschedules due to missed blocks
        // For now, return 0 as this feature would need database schema changes
        return 0
    }
    
    fun refreshSummaryData(date: LocalDate = LocalDate.now().minusDays(1)) {
        _currentUser.value?.let { user ->
            loadSummaryData(user.id, date)
        }
    }
    
    fun setDate(newDate: LocalDate) {
        _currentUser.value?.let { user ->
            loadSummaryData(user.id, newDate)
        }
    }
}