package com.example.studyblocks

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyBlocksApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Note: Notification scheduling moved to when user is authenticated
        // to avoid early dependency injection issues
    }
}