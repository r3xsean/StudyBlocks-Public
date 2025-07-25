package com.example.studyblocks.di

import android.content.Context
import androidx.room.Room
import com.example.studyblocks.data.local.StudyBlocksDatabase
import com.example.studyblocks.data.local.dao.SubjectDao
import com.example.studyblocks.data.local.dao.StudyBlockDao
import com.example.studyblocks.data.local.dao.StudySessionDao
import com.example.studyblocks.data.local.dao.UserDao
import com.example.studyblocks.data.local.dao.SchedulePreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyBlocksDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            StudyBlocksDatabase::class.java,
            "study_blocks_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: StudyBlocksDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideSubjectDao(database: StudyBlocksDatabase): SubjectDao {
        return database.subjectDao()
    }
    
    @Provides
    fun provideStudyBlockDao(database: StudyBlocksDatabase): StudyBlockDao {
        return database.studyBlockDao()
    }
    
    @Provides
    fun provideStudySessionDao(database: StudyBlocksDatabase): StudySessionDao {
        return database.studySessionDao()
    }
    
    @Provides
    fun provideSchedulePreferencesDao(database: StudyBlocksDatabase): SchedulePreferencesDao {
        return database.schedulePreferencesDao()
    }
}