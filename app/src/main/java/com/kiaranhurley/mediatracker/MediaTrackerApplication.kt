package com.kiaranhurley.mediatracker

import android.app.Application
import com.kiaranhurley.mediatracker.api.ApiConfig
import com.kiaranhurley.mediatracker.api.ApiTester
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase

class MediaTrackerApplication : Application() {
    val database by lazy { MediaTrackerDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        // Initialize API configuration
        ApiConfig.initialize(this)

        // Test APIs on app startup (remove this later)
        ApiTester.testAllApis()
    }
}