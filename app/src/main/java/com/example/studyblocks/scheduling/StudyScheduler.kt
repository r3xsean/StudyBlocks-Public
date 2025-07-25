package com.example.studyblocks.scheduling

import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.Subject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.*

class StudyScheduler {
    
    fun rescheduleWithMissedBlocks(
        allBlocks: List<StudyBlock>,
        userId: String,
        blocksPerWeekday: Int = 3,
        blocksPerWeekend: Int = 2,
        scheduleHorizon: Int = 21
    ): List<StudyBlock> {
        val currentDate = LocalDate.now()
        
        // Separate completed, missed, and future blocks
        val completedBlocks = allBlocks.filter { it.isCompleted }
        val missedBlocks = allBlocks.filter { it.isOverdue && !it.isCompleted }
        val futureBlocks = allBlocks.filter { 
            it.scheduledDate >= currentDate && !it.isCompleted 
        }
        
        // If no missed blocks, return original schedule
        if (missedBlocks.isEmpty()) {
            return allBlocks
        }
        
        // Create new schedule starting from today
        val rescheduledBlocks = mutableListOf<StudyBlock>()
        val scheduleMap = mutableMapOf<LocalDate, MutableList<StudyBlock>>()
        
        // Initialize schedule map for the horizon
        for (i in 0 until scheduleHorizon) {
            val date = currentDate.plusDays(i.toLong())
            scheduleMap[date] = mutableListOf()
        }
        
        // First, add completed blocks to maintain their dates
        completedBlocks.forEach { block ->
            rescheduledBlocks.add(block)
        }
        
        // Combine missed blocks with future blocks for redistribution
        val blocksToReschedule = missedBlocks + futureBlocks
        
        // Redistribute all non-completed blocks
        val redistributedBlocks = redistributeBlocks(
            blocks = blocksToReschedule,
            scheduleMap = scheduleMap,
            blocksPerWeekday = blocksPerWeekday,
            blocksPerWeekend = blocksPerWeekend,
            startDate = currentDate,
            scheduleHorizon = scheduleHorizon
        )
        
        rescheduledBlocks.addAll(redistributedBlocks)
        
        return rescheduledBlocks.sortedBy { it.scheduledDate }
    }
    
    private fun redistributeBlocks(
        blocks: List<StudyBlock>,
        scheduleMap: MutableMap<LocalDate, MutableList<StudyBlock>>,
        blocksPerWeekday: Int,
        blocksPerWeekend: Int,
        startDate: LocalDate,
        scheduleHorizon: Int
    ): List<StudyBlock> {
        val redistributedBlocks = mutableListOf<StudyBlock>()
        var blockIndex = 0
        
        // Distribute blocks day by day, respecting weekday/weekend capacity
        for (dayOffset in 0 until scheduleHorizon) {
            val currentDate = startDate.plusDays(dayOffset.toLong())
            
            // Determine capacity for this day
            val dailyCapacity = when (currentDate.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> blocksPerWeekend
                else -> blocksPerWeekday
            }
            
            // Fill this day up to capacity
            var blocksScheduledToday = 0
            while (blocksScheduledToday < dailyCapacity && blockIndex < blocks.size) {
                val originalBlock = blocks[blockIndex]
                val rescheduledBlock = originalBlock.copy(
                    id = originalBlock.id, // Keep original ID
                    scheduledDate = currentDate
                )
                redistributedBlocks.add(rescheduledBlock)
                blockIndex++
                blocksScheduledToday++
            }
            
            // If we've scheduled all blocks, break
            if (blockIndex >= blocks.size) break
        }
        
        // If there are remaining blocks that didn't fit in the horizon,
        // extend the schedule as needed
        var currentExtensionDay = scheduleHorizon
        while (blockIndex < blocks.size) {
            val extensionDate = startDate.plusDays(currentExtensionDay.toLong())
            val dailyCapacity = when (extensionDate.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> blocksPerWeekend
                else -> blocksPerWeekday
            }
            
            // Fill this extension day up to its capacity
            var blocksScheduledThisExtensionDay = 0
            while (blocksScheduledThisExtensionDay < dailyCapacity && blockIndex < blocks.size) {
                val originalBlock = blocks[blockIndex]
                val rescheduledBlock = originalBlock.copy(
                    id = originalBlock.id,
                    scheduledDate = extensionDate
                )
                redistributedBlocks.add(rescheduledBlock)
                blockIndex++
                blocksScheduledThisExtensionDay++
            }
            
            currentExtensionDay++
        }
        
        return redistributedBlocks
    }
    
    fun generateSchedule(
        subjects: List<Subject>,
        userId: String,
        scheduleHorizon: Int = 21, // days
        blocksPerWeekday: Int = 3, // user's preferred blocks per weekday
        blocksPerWeekend: Int = 2, // user's preferred blocks per weekend day
        blockDurationMinutes: Int = 60 // duration for each block
    ): List<StudyBlock> {
        val allStudyBlocks = mutableListOf<StudyBlock>()
        val scheduleMap = mutableMapOf<LocalDate, MutableList<StudyBlock>>()
        val subjectPriorities = calculateSubjectPriorities(subjects)
        
        // Initialize schedule map with different capacity for weekdays/weekends
        val startDate = LocalDate.now()
        var totalWeekdays = 0
        var totalWeekendDays = 0
        
        for (i in 0 until scheduleHorizon) {
            val date = startDate.plusDays(i.toLong())
            scheduleMap[date] = mutableListOf()
            
            // Count weekdays vs weekend days
            when (date.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> totalWeekendDays++
                else -> totalWeekdays++
            }
        }
        
        // Calculate total blocks to distribute based on weekday/weekend preferences
        val totalBlocks = (totalWeekdays * blocksPerWeekday) + (totalWeekendDays * blocksPerWeekend)
        
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
        
        // Force distribute to ensure daily capacity is met with weekday/weekend awareness
        return forceDistributeWithWeekdayWeekendCapacity(
            allStudyBlocks = shuffledBlocks,
            scheduleMap = scheduleMap,
            blocksPerWeekday = blocksPerWeekday,
            blocksPerWeekend = blocksPerWeekend,
            scheduleHorizon = scheduleHorizon
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
    
    private fun forceDistributeWithWeekdayWeekendCapacity(
        allStudyBlocks: List<StudyBlock>,
        scheduleMap: MutableMap<LocalDate, MutableList<StudyBlock>>,
        blocksPerWeekday: Int,
        blocksPerWeekend: Int,
        scheduleHorizon: Int
    ): List<StudyBlock> {
        val distributedBlocks = mutableListOf<StudyBlock>()
        val startDate = LocalDate.now()
        
        // Count total required blocks for weekdays and weekends
        var totalWeekdayBlocks = 0
        var totalWeekendBlocks = 0
        
        for (i in 0 until scheduleHorizon) {
            val date = startDate.plusDays(i.toLong())
            when (date.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> totalWeekendBlocks += blocksPerWeekend
                else -> totalWeekdayBlocks += blocksPerWeekday
            }
        }
        
        val totalRequiredBlocks = totalWeekdayBlocks + totalWeekendBlocks
        
        // If we don't have enough blocks, duplicate existing ones to fill capacity
        val blocksToDistribute = if (allStudyBlocks.size < totalRequiredBlocks) {
            generateExactBlockCount(allStudyBlocks, totalRequiredBlocks)
        } else {
            allStudyBlocks.take(totalRequiredBlocks)
        }
        
        // Distribute blocks based on weekday/weekend capacity
        var blockIndex = 0
        for (dayOffset in 0 until scheduleHorizon) {
            val currentDate = startDate.plusDays(dayOffset.toLong())
            
            // Determine blocks for this day based on weekday/weekend
            val blocksForThisDay = when (currentDate.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> blocksPerWeekend
                else -> blocksPerWeekday
            }
            
            // Add the appropriate number of blocks to this day
            for (blockInDay in 0 until blocksForThisDay) {
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