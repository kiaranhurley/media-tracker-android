package com.kiaranhurley.mediatracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.kiaranhurley.mediatracker.api.ApiConfig
import com.kiaranhurley.mediatracker.ui.theme.MediaTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Test API calls
        testApis()

        setContent {
            MediaTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ApiTestScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun testApis() {
        lifecycleScope.launch {
            // Test TMDB API
            try {
                Log.d("API_TEST", "Testing TMDB API...")
                val tmdbResponse = ApiConfig.tmdbService.searchMovies(query = "Inception")
                if (tmdbResponse.isSuccessful) {
                    val movies = tmdbResponse.body()
                    Log.d("API_TEST", "TMDB Success: Found ${movies?.results?.size} movies")
                    movies?.results?.firstOrNull()?.let { movie ->
                        Log.d("API_TEST", "First movie: ${movie.title}")
                    }
                } else {
                    Log.e("API_TEST", "TMDB Error: ${tmdbResponse.code()} - ${tmdbResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "TMDB Exception: ${e.message}", e)
            }

            // Test IGDB API (Note: You'll need a valid access token)
            try {
                Log.d("API_TEST", "Testing IGDB API...")
                val searchQuery = """
                    fields name,summary,first_release_date,aggregated_rating,cover.url;
                    search "The Witcher 3";
                    limit 5;
                """.trimIndent()

                // Note: Replace "YOUR_ACCESS_TOKEN" with an actual IGDB access token
                val igdbResponse = ApiConfig.igdbService.searchGames(
                    authorization = "Bearer YOUR_ACCESS_TOKEN",
                    query = searchQuery
                )
                if (igdbResponse.isSuccessful) {
                    val games = igdbResponse.body()
                    Log.d("API_TEST", "IGDB Success: Found ${games?.size} games")
                    games?.firstOrNull()?.let { game ->
                        Log.d("API_TEST", "First game: ${game.name}")
                    }
                } else {
                    Log.e("API_TEST", "IGDB Error: ${igdbResponse.code()} - ${igdbResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "IGDB Exception: ${e.message}", e)
            }
        }
    }
}

@Composable
fun ApiTestScreen(modifier: Modifier = Modifier) {
    var apiStatus by remember { mutableStateOf("Testing APIs...") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = apiStatus,
            style = MaterialTheme.typography.headlineMedium
        )
    }

    // Update status after a delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        apiStatus = "Check Logcat for API results!"
    }
}

@Preview(showBackground = true)
@Composable
fun ApiTestScreenPreview() {
    MediaTrackerTheme {
        ApiTestScreen()
    }
}