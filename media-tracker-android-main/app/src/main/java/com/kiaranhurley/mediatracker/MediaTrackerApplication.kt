package com.kiaranhurley.mediatracker

import android.app.Application
import android.util.Log
import com.kiaranhurley.mediatracker.api.ApiConfig
import com.kiaranhurley.mediatracker.api.ApiTester
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MediaTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API configuration
        initializeApis()
        
        // Test APIs for development
        testApis()
    }
    
    private fun initializeApis() {
        try {
            ApiConfig.initialize(this)
            Log.d("MediaTracker", "✅ APIs initialized successfully")
        } catch (e: Exception) {
            Log.e("MediaTracker", "❌ Failed to initialize APIs: ${e.message}")
        }
    }
    
    private fun testApis() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MediaTracker", "🧪 Testing API connections...")
                
                // Test IGDB credentials first
                val igdbWorking = ApiConfig.testIgdbCredentials()
                if (igdbWorking) {
                    Log.d("MediaTracker", "✅ IGDB API is working correctly!")
                } else {
                    Log.e("MediaTracker", "❌ IGDB API setup needs attention - check logs for details")
                }
                
                // Test full API suite with context
                ApiTester.testApis(this@MediaTrackerApplication)
                
                // Clear sample data on app start (uncomment to clear)
                // clearSampleData()
            } catch (e: Exception) {
                Log.e("MediaTracker", "❌ API test failed: ${e.message}")
            }
        }
    }
    
    private suspend fun clearSampleData() {
        try {
            Log.d("MediaTracker", "🗑️ Clearing sample data...")
            
            // Get database instance
            val database = MediaTrackerDatabase.getDatabase(this)
            
            // Clear all games and films
            database.gameDao().deleteAllGames()
            database.filmDao().deleteAllFilms()
            
            Log.d("MediaTracker", "✅ Sample data cleared successfully!")
        } catch (e: Exception) {
            Log.e("MediaTracker", "❌ Failed to clear sample data: ${e.message}")
        }
    }
}