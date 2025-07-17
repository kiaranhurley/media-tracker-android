package com.kiaranhurley.mediatracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Hilt will now handle all dependency injection
        // Database and API services are provided via Hilt modules
    }
}