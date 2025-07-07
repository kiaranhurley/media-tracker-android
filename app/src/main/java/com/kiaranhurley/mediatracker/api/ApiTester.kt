package com.kiaranhurley.mediatracker.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Separate class for testing API functionality
 * Call this from anywhere to test your APIs without modifying MainActivity
 */
object ApiTester {

    fun testAllApis() {
        CoroutineScope(Dispatchers.IO).launch {
            testTmdbApi()
            testIgdbApi()
        }
    }

    private suspend fun testTmdbApi() {
        try {
            Log.d("API_TEST", "Testing TMDB API...")
            val response = ApiConfig.tmdbService.searchMovies(
                apiKey = ApiConfig.getTmdbApiKey(),
                query = "Inception"
            )

            if (response.isSuccessful) {
                val movies = response.body()
                Log.d("API_TEST", "✅ TMDB Success: Found ${movies?.results?.size} movies")
                movies?.results?.firstOrNull()?.let { movie ->
                    Log.d("API_TEST", "First movie: ${movie.title} (${movie.releaseDate})")
                }
            } else {
                Log.e("API_TEST", "❌ TMDB Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "❌ TMDB Exception: ${e.message}", e)
        }
    }

    private suspend fun testIgdbApi() {
        try {
            Log.d("API_TEST", "Testing IGDB API...")
            
            // Get access token first
            val accessToken = ApiConfig.getIgdbAccessToken()
            if (accessToken == null) {
                Log.e("API_TEST", "❌ Failed to get IGDB access token")
                return
            }
            
            // Improved IGDB query format
            val searchQuery = """
                fields name,summary,first_release_date,aggregated_rating;
                search "Witcher 3";
                limit 5;
            """.trimIndent()

            val response = ApiConfig.igdbService.searchGames(
                clientId = ApiConfig.getIgdbClientId(),
                authorization = "Bearer $accessToken",
                query = searchQuery
            )

            if (response.isSuccessful) {
                val games = response.body()
                Log.d("API_TEST", "✅ IGDB Success: Found ${games?.size} games")
                games?.firstOrNull()?.let { game ->
                    Log.d("API_TEST", "First game: ${game.name ?: "ID: ${game.id}"}")
                }
            } else {
                Log.e("API_TEST", "❌ IGDB Error: ${response.code()} - ${response.message()}")
                Log.e("API_TEST", "Response body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "❌ IGDB Exception: ${e.message}", e)
        }
    }
}