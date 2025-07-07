package com.kiaranhurley.mediatracker

import android.app.Application
import com.kiaranhurley.mediatracker.api.ApiTester
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase

class MediaTrackerApplication : Application() {
    val database by lazy { MediaTrackerDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        // Test APIs on app startup (remove this later)
        ApiTester.testAllApis()
    }
}