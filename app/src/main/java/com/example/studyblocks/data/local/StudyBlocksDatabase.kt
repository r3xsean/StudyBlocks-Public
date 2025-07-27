package com.example.studyblocks.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.studyblocks.data.model.Subject
import com.example.studyblocks.data.model.StudyBlock
import com.example.studyblocks.data.model.StudySession
import com.example.studyblocks.data.model.User
import com.example.studyblocks.data.model.SchedulePreferences
import com.example.studyblocks.data.local.dao.SubjectDao
import com.example.studyblocks.data.local.dao.StudyBlockDao
import com.example.studyblocks.data.local.dao.StudySessionDao
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.local.dao.SchedulePreferencesDao

@Database(
    entities = [
        User::class,
        Subject::class,
        StudyBlock::class,
        StudySession::class,
        SchedulePreferences::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyBlocksDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studyBlockDao(): StudyBlockDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun schedulePreferencesDao(): SchedulePreferencesDao
    
    companion object {
        @Volatile
        private var INSTANCE: StudyBlocksDatabase? = null
        
        fun getDatabase(context: Context): StudyBlocksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyBlocksDatabase::class.java,
                    "study_blocks_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}