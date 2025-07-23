package com.example.studyblocks.di

import com.example.studyblocks.data.model.XPManager
import com.example.studyblocks.scheduling.StudyScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideStudyScheduler(): StudyScheduler {
        return StudyScheduler()
    }
    
    @Provides
    @Singleton
    fun provideXPManager(): XPManager {
        return XPManager
    }
}