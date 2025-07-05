package com.kiaranhurley.mediatracker

import android.app.Application
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase

class MediaTrackerApplication : Application() {
    val database by lazy { MediaTrackerDatabase.getDatabase(this) }
}