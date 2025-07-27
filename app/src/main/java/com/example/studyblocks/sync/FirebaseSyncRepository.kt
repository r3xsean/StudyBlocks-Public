package com.example.studyblocks.sync

// Firebase Sync disabled for open source version
// Uncomment and add firebase dependencies to build.gradle if you want to use Firebase Sync

/*
import com.example.studyblocks.data.local.dao.StudyBlockDao
import com.example.studyblocks.data.local.dao.StudySessionDao
import com.example.studyblocks.data.local.dao.SubjectDao
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val subjectDao: SubjectDao,
    private val studyBlockDao: StudyBlockDao,
    private val studySessionDao: StudySessionDao
) {
    
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    suspend fun syncUserData(userId: String): SyncResult {
        return try {
            // Upload local data to Firestore
            uploadUserData(userId)
            
            // Download and merge data from Firestore
            downloadAndMergeData(userId)
            
            // Update last sync timestamp
            userDao.updateLastSync(userId, LocalDateTime.now())
            
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Sync failed")
        }
    }
    
    private suspend fun uploadUserData(userId: String) {
        val userRef = firestore.collection("users").document(userId)
        
        // Upload user profile
        val user = userDao.getUserById(userId)
        if (user != null) {
            val userMap = mapOf(
                "id" to user.id,
                "email" to user.email,
                "displayName" to user.displayName,
                "profilePictureUrl" to user.profilePictureUrl,
                "defaultBlockDuration" to user.defaultBlockDuration,
                "createdAt" to user.createdAt.format(dateTimeFormatter),
                "lastSyncAt" to LocalDateTime.now().format(dateTimeFormatter),
                "isSignedIn" to user.isSignedIn
            )
            userRef.set(userMap).await()
        }
        
        // Upload subjects
        val subjects = subjectDao.getAllSubjects(userId).first()
        val subjectsRef = userRef.collection("subjects")
        
        subjects.forEach { subject ->
            val subjectMap = mapOf(
                "id" to subject.id,
                "name" to subject.name,
                "icon" to subject.icon,
                "confidence" to subject.confidence,
                "xp" to subject.xp,
                "level" to subject.level,
                "blockDurationMinutes" to subject.blockDurationMinutes,
                "createdAt" to subject.createdAt.format(dateTimeFormatter),
                "updatedAt" to subject.updatedAt.format(dateTimeFormatter),
                "userId" to subject.userId
            )
            subjectsRef.document(subject.id).set(subjectMap).await()
        }
        
        // Upload study blocks
        val blocks = studyBlockDao.getBlocksForDateRange(
            userId = userId,
            startDate = LocalDate.now().minusMonths(1),
            endDate = LocalDate.now().plusMonths(3)
        ).first()
        
        val blocksRef = userRef.collection("studyBlocks")
        blocks.forEach { block ->
            val blockMap = mapOf(
                "id" to block.id,
                "subjectId" to block.subjectId,
                "subjectName" to block.subjectName,
                "subjectIcon" to block.subjectIcon,
                "blockNumber" to block.blockNumber,
                "durationMinutes" to block.durationMinutes,
                "scheduledDate" to block.scheduledDate.format(dateFormatter),
                "isCompleted" to block.isCompleted,
                "completedAt" to block.completedAt?.format(dateTimeFormatter),
                "createdAt" to block.createdAt.format(dateTimeFormatter),
                "userId" to block.userId,
                "spacedRepetitionInterval" to block.spacedRepetitionInterval
            )
            blocksRef.document(block.id).set(blockMap).await()
        }
        
        // Upload study sessions
        val sessions = studySessionDao.getAllSessions(userId).first()
        val sessionsRef = userRef.collection("studySessions")
        
        sessions.forEach { session ->
            val sessionMap = mapOf(
                "id" to session.id,
                "studyBlockId" to session.studyBlockId,
                "subjectId" to session.subjectId,
                "userId" to session.userId,
                "startTime" to session.startTime.format(dateTimeFormatter),
                "endTime" to session.endTime?.format(dateTimeFormatter),
                "plannedDurationMinutes" to session.plannedDurationMinutes,
                "actualDurationMinutes" to session.actualDurationMinutes,
                "isCompleted" to session.isCompleted,
                "breaksTaken" to session.breaksTaken,
                "focusScore" to session.focusScore
            )
            sessionsRef.document(session.id).set(sessionMap).await()
        }
    }
    
    private suspend fun downloadAndMergeData(userId: String) {
        val userRef = firestore.collection("users").document(userId)
        
        // Download and merge subjects
        val subjectsSnapshot = userRef.collection("subjects").get().await()
        val remoteSubjects = subjectsSnapshot.documents.mapNotNull { doc ->
            try {
                Subject(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    icon = doc.getString("icon") ?: "📚",
                    confidence = (doc.getLong("confidence") ?: 5).toInt(),
                    xp = (doc.getLong("xp") ?: 0).toInt(),
                    level = (doc.getLong("level") ?: 1).toInt(),
                    blockDurationMinutes = (doc.getLong("blockDurationMinutes") ?: 30).toInt(),
                    createdAt = LocalDateTime.parse(doc.getString("createdAt") ?: return@mapNotNull null, dateTimeFormatter),
                    updatedAt = LocalDateTime.parse(doc.getString("updatedAt") ?: return@mapNotNull null, dateTimeFormatter),
                    userId = doc.getString("userId") ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }
        
        // Merge subjects (keep most recently updated)
        remoteSubjects.forEach { remoteSubject ->
            val localSubject = subjectDao.getSubjectById(remoteSubject.id)
            if (localSubject == null || remoteSubject.updatedAt.isAfter(localSubject.updatedAt)) {
                subjectDao.insertSubject(remoteSubject)
            }
        }
        
        // Download and merge study blocks
        val blocksSnapshot = userRef.collection("studyBlocks").get().await()
        val remoteBlocks = blocksSnapshot.documents.mapNotNull { doc ->
            try {
                StudyBlock(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    subjectId = doc.getString("subjectId") ?: return@mapNotNull null,
                    subjectName = doc.getString("subjectName") ?: return@mapNotNull null,
                    subjectIcon = doc.getString("subjectIcon") ?: "📚",
                    blockNumber = (doc.getLong("blockNumber") ?: 1).toInt(),
                    durationMinutes = (doc.getLong("durationMinutes") ?: 30).toInt(),
                    scheduledDate = LocalDate.parse(doc.getString("scheduledDate") ?: return@mapNotNull null, dateFormatter),
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    completedAt = doc.getString("completedAt")?.let { 
                        LocalDateTime.parse(it, dateTimeFormatter) 
                    },
                    createdAt = LocalDateTime.parse(doc.getString("createdAt") ?: return@mapNotNull null, dateTimeFormatter),
                    userId = doc.getString("userId") ?: return@mapNotNull null,
                    spacedRepetitionInterval = (doc.getLong("spacedRepetitionInterval") ?: 1).toInt()
                )
            } catch (e: Exception) {
                null
            }
        }
        
        // Insert remote blocks (replace existing)
        remoteBlocks.forEach { block ->
            studyBlockDao.insertBlock(block)
        }
        
        // Download and merge study sessions
        val sessionsSnapshot = userRef.collection("studySessions").get().await()
        val remoteSessions = sessionsSnapshot.documents.mapNotNull { doc ->
            try {
                StudySession(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    studyBlockId = doc.getString("studyBlockId") ?: return@mapNotNull null,
                    subjectId = doc.getString("subjectId") ?: return@mapNotNull null,
                    userId = doc.getString("userId") ?: return@mapNotNull null,
                    startTime = LocalDateTime.parse(doc.getString("startTime") ?: return@mapNotNull null, dateTimeFormatter),
                    endTime = doc.getString("endTime")?.let { 
                        LocalDateTime.parse(it, dateTimeFormatter) 
                    },
                    plannedDurationMinutes = (doc.getLong("plannedDurationMinutes") ?: 30).toInt(),
                    actualDurationMinutes = doc.getLong("actualDurationMinutes")?.toInt(),
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    breaksTaken = (doc.getLong("breaksTaken") ?: 0).toInt(),
                    focusScore = doc.getLong("focusScore")?.toInt()
                )
            } catch (e: Exception) {
                null
            }
        }
        
        // Insert remote sessions (replace existing)
        remoteSessions.forEach { session ->
            studySessionDao.insertSession(session)
        }
    }
    
    suspend fun deleteUserDataFromFirestore(userId: String) {
        try {
            val userRef = firestore.collection("users").document(userId)
            
            // Delete all subcollections
            val collections = listOf("subjects", "studyBlocks", "studySessions")
            collections.forEach { collection ->
                val snapshot = userRef.collection(collection).get().await()
                snapshot.documents.forEach { doc ->
                    doc.reference.delete().await()
                }
            }
            
            // Delete user document
            userRef.delete().await()
            
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }
}


sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}
*/