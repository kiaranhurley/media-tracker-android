package com.kiaranhurley.mediatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.ui.AuthScreen
import com.kiaranhurley.mediatracker.ui.MainNavigation
import com.kiaranhurley.mediatracker.ui.theme.MediaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaTrackerTheme {
                MediaTrackerApp()
            }
        }
    }
}

    @Composable
fun MediaTrackerApp() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (currentUser == null) {
            // Show authentication screen
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                },
                modifier = Modifier.padding(innerPadding)
            )
                    } else {
            // Show main app content with navigation
            MainNavigation(
                user = currentUser!!,
                onLogout = {
                    currentUser = null
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MediaTrackerTheme {
        Text("Media Tracker Preview")
    }
}