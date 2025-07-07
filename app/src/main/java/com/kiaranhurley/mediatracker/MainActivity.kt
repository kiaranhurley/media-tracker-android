package com.kiaranhurley.mediatracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.kiaranhurley.mediatracker.api.ApiConfig
import com.kiaranhurley.mediatracker.api.models.TmdbSearchMovie
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.ui.theme.MediaTrackerTheme
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaTrackerTheme {
                TestingScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TestingScreen() {
        var testResults by remember { mutableStateOf(listOf<String>()) }
        var movies by remember { mutableStateOf(listOf<TmdbSearchMovie>()) }
        var isLoading by remember { mutableStateOf(false) }

        fun addResult(result: String) {
            testResults = testResults + result
            Log.d("TEST_UI", result)
        }

        fun testDatabase() {
            lifecycleScope.launch {
                isLoading = true
                try {
                    val app = application as MediaTrackerApplication
                    val userDao = app.database.userDao()

                    // Test user creation
                    val testUser = User(
                        username = "testuser",
                        displayName = "Test User",
                        email = "test@example.com",
                        password = "password123"
                    )

                    val userId = userDao.insertUser(testUser)
                    addResult("✅ Database: User created with ID $userId")

                    // Test user retrieval
                    val retrievedUser = userDao.getUserById(userId.toInt())
                    if (retrievedUser != null) {
                        addResult("✅ Database: User retrieved successfully")
                    } else {
                        addResult("❌ Database: Failed to retrieve user")
                    }

                } catch (e: Exception) {
                    addResult("❌ Database Error: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }

        fun testTmdbApi() {
            lifecycleScope.launch {
                isLoading = true
                try {
                    addResult("🔄 Testing TMDB API...")
                    val response = ApiConfig.tmdbService.searchMovies(query = "Inception")

                    if (response.isSuccessful) {
                        val movieResults = response.body()
                        if (movieResults != null && movieResults.results.isNotEmpty()) {
                            addResult("✅ TMDB API: Found ${movieResults.results.size} movies")
                            movies = movieResults.results.take(5) // Show first 5 results
                            addResult("📽️ First movie: ${movieResults.results[0].title}")
                        } else {
                            addResult("⚠️ TMDB API: No movies found")
                        }
                    } else {
                        addResult("❌ TMDB API Error: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    addResult("❌ TMDB Exception: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }

        fun testIgdbApi() {
            lifecycleScope.launch {
                isLoading = true
                try {
                    addResult("🔄 Testing IGDB API...")
                    val searchQuery = """
                        fields name,summary,first_release_date,aggregated_rating;
                        search "The Witcher 3";
                        limit 3;
                    """.trimIndent()

                    // Note: This will fail without a real access token
                    val response = ApiConfig.igdbService.searchGames(
                        authorization = "Bearer YOUR_ACCESS_TOKEN",
                        query = searchQuery
                    )

                    if (response.isSuccessful) {
                        val games = response.body()
                        addResult("✅ IGDB API: Found ${games?.size} games")
                        games?.firstOrNull()?.let { game ->
                            addResult("🎮 First game: ${game.name}")
                        }
                    } else {
                        addResult("❌ IGDB API Error: ${response.code()}")
                        addResult("ℹ️ Note: IGDB needs a real access token")
                    }
                } catch (e: Exception) {
                    addResult("❌ IGDB Exception: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        }

        fun clearResults() {
            testResults = emptyList()
            movies = emptyList()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "MediaTracker API Tests",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Test Buttons
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "API & Database Tests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { testDatabase() },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test Database")
                            }

                            Button(
                                onClick = { testTmdbApi() },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test TMDB")
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { testIgdbApi() },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test IGDB")
                            }

                            OutlinedButton(
                                onClick = { clearResults() },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear")
                            }
                        }

                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text("Testing...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Test Results
                if (testResults.isNotEmpty()) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Test Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(testResults.reversed()) { result ->
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Movies Results
                if (movies.isNotEmpty()) {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Movies Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(movies) { movie ->
                                    Card(colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = movie.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            movie.releaseDate?.let {
                                                Text(
                                                    text = "Release: $it",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Text(
                                                text = "Rating: ${movie.voteAverage}/10",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Instructions
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Test Database: Creates a test user and verifies storage\n" +
                                    "• Test TMDB: Searches for movies using your API key\n" +
                                    "• Test IGDB: Will fail without access token (expected)\n" +
                                    "• Check Logcat for detailed API responses",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}