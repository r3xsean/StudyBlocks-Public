package com.example.studyblocks.scheduling

import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.*

class StudyScheduler {
    
    fun generateSchedule(
        subjects: List<Subject>,
        userId: String,
        scheduleHorizon: Int = 21, // days
        preferredBlocksPerDay: Int = 3, // user's preferred blocks per day
        blockDurationMinutes: Int = 60 // duration for each block
    ): List<StudyBlock> {
        val allStudyBlocks = mutableListOf<StudyBlock>()
        val scheduleMap = mutableMapOf<LocalDate, MutableList<StudyBlock>>()
        val subjectPriorities = calculateSubjectPriorities(subjects)
        
        // Initialize schedule map with fixed capacity per day
        val startDate = LocalDate.now()
        for (i in 0 until scheduleHorizon) {
            val daily = startDate.plusDays(i.toLong())
            scheduleMap[daily] = mutableListOf() // Initialize with fixed capacity
        }
        
        // Calculate total blocks to distribute
        val totalDays = scheduleHorizon
        val totalBlocks = preferredBlocksPerDay * totalDays
        
        // Calculate blocks per subject based on confidence weights only
        val totalWeight = subjects.sumOf { it.confidenceWeight }
        
        subjects.forEach { subject ->
            val subjectWeight = subject.confidenceWeight
            val subjectBlockCount = ((subjectWeight / totalWeight) * totalBlocks).toInt()
            
            // Debug logging (remove in production)
            println("Subject: ${subject.name}, Confidence: ${subject.confidence}, Weight: $subjectWeight, Blocks: $subjectBlockCount")
            
            // Generate exact number of blocks for this subject
            val subjectBlocks = generateExactBlocksForSubject(
                subject = subject,
                userId = userId,
                blockCount = subjectBlockCount,
                scheduleHorizon = scheduleHorizon,
                blockDurationMinutes = blockDurationMinutes
            )
            allStudyBlocks.addAll(subjectBlocks)
        }
        
        
        // Shuffle blocks to mix subjects throughout the schedule
        val shuffledBlocks = allStudyBlocks.shuffled()
        
        // Force distribute to ensure daily capacity is met
        return forceDistributeFixedDailyCapacity(
            allStudyBlocks = shuffledBlocks,
            scheduleMap = scheduleMap,
            preferredBlocksPerDay = preferredBlocksPerDay,
            totalDays = totalDays
        )
    }
    
    // Calculate subject priorities based on confidence weights only
    private fun calculateSubjectPriorities(subjects: List<Subject>): Map<String, Double> {
        val priorities = mutableMapOf<String, Double>()
        val totalWeight = subjects.sumOf { it.confidenceWeight }
        
        subjects.forEach { subject ->
            val priority = subject.confidenceWeight / totalWeight
            priorities[subject.id] = priority
        }
        
        return priorities
    }
    
    private fun generateExactBlocksForSubject(
        subject: Subject,
        userId: String,
        blockCount: Int,
        scheduleHorizon: Int,
        blockDurationMinutes: Int
    ): List<StudyBlock> {
        val blocks = mutableListOf<StudyBlock>()
        
        // Generate exactly the specified number of blocks
        for (i in 0 until blockCount) {
            val block = StudyBlock(
                id = UUID.randomUUID().toString(),
                subjectId = subject.id,
                subjectName = subject.name,
                subjectIcon = subject.icon,
                blockNumber = i + 1,
                durationMinutes = blockDurationMinutes,
                scheduledDate = LocalDate.now(), // Will be redistributed later
                userId = userId,
                spacedRepetitionInterval = 1,
                totalBlocksForSubject = blockCount
            )
            blocks.add(block)
        }

        return blocks
    }
    
    private fun generatePreciseDailyDistribution(
        blockCount: Int,
        scheduleHorizon: Int
    ): List<Pair<LocalDate, Int>> {
        val distribution = mutableListOf<Pair<LocalDate, Int>>()
        val startDate = LocalDate.now()
        
        // Calculate spacing intervals based on fixed pattern
        val daysPerBlock = maxOf(scheduleHorizon / blockCount.toDouble(), 1.0)
        
        for (i in 0 until blockCount) {
            val targetDate = startDate.plusDays((i * daysPerBlock).roundToInt().toLong())
            val spacingInterval = daysPerBlock.roundToInt()
            
            if (i == 0) distribution.add(targetDate to 1) // First block always comes after 1 day
            else distribution.add(targetDate to spacingInterval)
        }
        
        return distribution
    }
    
    private fun forceDistributeFixedDailyCapacity(
        allStudyBlocks: List<StudyBlock>,
        scheduleMap: MutableMap<LocalDate, MutableList<StudyBlock>>,
        preferredBlocksPerDay: Int,
        totalDays: Int
    ): List<StudyBlock> {
        val distributedBlocks = mutableListOf<StudyBlock>()
        val startDate = LocalDate.now()
        
        // Calculate total required blocks
        val totalRequiredBlocks = preferredBlocksPerDay * totalDays
        
        // If we don't have enough blocks, duplicate existing ones to fill capacity
        val blocksToDistribute = if (allStudyBlocks.size < totalRequiredBlocks) {
            generateExactBlockCount(allStudyBlocks, totalRequiredBlocks)
        } else {
            allStudyBlocks.take(totalRequiredBlocks)
        }
        
        // Distribute exactly preferredBlocksPerDay blocks to each day
        var blockIndex = 0
        for (dayOffset in 0 until totalDays) {
            val currentDate = startDate.plusDays(dayOffset.toLong())
            
            // Add exactly preferredBlocksPerDay blocks to this day
            for (blockInDay in 0 until preferredBlocksPerDay) {
                if (blockIndex < blocksToDistribute.size) {
                    val originalBlock = blocksToDistribute[blockIndex]
                    val distributedBlock = originalBlock.copy(
                        id = UUID.randomUUID().toString(), // Ensure unique ID
                        scheduledDate = currentDate
                        // Keep original blockNumber for subject-based sequence
                    )
                    distributedBlocks.add(distributedBlock)
                    blockIndex++
                }
            }
        }

        return distributedBlocks.sortedBy { it.scheduledDate }
    }
    
    private fun generateExactBlockCount(originalBlocks: List<StudyBlock>, targetCount: Int): List<StudyBlock> {
        if (originalBlocks.isEmpty()) return emptyList()
        
        val result = mutableListOf<StudyBlock>()
        var currentIndex = 0
        
        // Cycle through original blocks until we reach target count
        for (i in 0 until targetCount) {
            val originalBlock = originalBlocks[currentIndex % originalBlocks.size]
            result.add(originalBlock.copy(
                id = UUID.randomUUID().toString(),
                blockNumber = i + 1
            ))
            currentIndex++
        }
        
        return result
    }
    
    private fun generatePossibleDates(targetDate: LocalDate, scheduleHorizon: Int): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val maxDate = LocalDate.now().plusDays(scheduleHorizon.toLong() - 1)
        
        // Start with target date
        dates.add(targetDate)
        
        // Add nearby dates (±2 days)
        for (offset in 1..2) {
            val before = targetDate.minusDays(offset.toLong())
            val after = targetDate.plusDays(offset.toLong())
            
            if (before >= LocalDate.now()) {
                dates.add(before)
            }
            if (after <= maxDate) {
                dates.add(after)
            }
        }
        
        return dates.distinct().sorted()
    }
}

data class SchedulingResult(
    val blocks: List<StudyBlock>,
    val totalBlocks: Int,
    val scheduleHorizon: Int,
    val averageBlocksPerDay: Double,
    val subjectDistribution: Map<String, Int>
)