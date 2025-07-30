package com.kiaranhurley.mediatracker.api

import android.content.Context
import com.kiaranhurley.mediatracker.api.services.IgdbService
import com.kiaranhurley.mediatracker.api.services.TmdbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

object ApiTester {
    private const val TAG = "API_TEST"
    
    fun testApis(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            testTmdbApi(context)
            testIgdbApi(context)
            testIgdbQueryFormats(context)
            testManualIgdbToken(context)
        }
    }
    
    private suspend fun testTmdbApi(context: Context) {
        try {
            Log.d(TAG, "Testing TMDB API...")
            ApiConfig.initialize(context)
            val response = ApiConfig.tmdbService.searchMovies(
                apiKey = ApiConfig.getTmdbApiKey(),
                query = "Inception",
                page = 1
            )
            
            if (response.isSuccessful && response.body() != null) {
                val movieResponse = response.body()!!
                Log.d(TAG, "✅ TMDB Success: Found ${movieResponse.results.size} movies")
                Log.d(TAG, "First movie: ${movieResponse.results.firstOrNull()?.title} (${movieResponse.results.firstOrNull()?.releaseDate})")
            } else {
                Log.d(TAG, "❌ TMDB Failed: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.d(TAG, "❌ TMDB Exception: ${e.message}")
        }
    }
    
    private suspend fun testIgdbApi(context: Context) {
        try {
            Log.d(TAG, "Testing IGDB API...")
            val token = ApiConfig.getIgdbAccessToken()
            if (token != null) {
                Log.d(TAG, "✅ Got IGDB access token: ${token.take(10)}...")
                
                // Test simple query first with proper IGDB syntax
                val simpleQuery = """
                    search "Minecraft";
                    fields id,name,summary,first_release_date,aggregated_rating,cover.*;
                    limit 5;
                """.trimIndent()
                Log.d(TAG, "Testing search query: $simpleQuery")
                
                val response = ApiConfig.igdbService.searchGames(
                    clientId = ApiConfig.getIgdbClientId(),
                    authorization = "Bearer $token",
                    query = simpleQuery
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val games = response.body()!!
                    Log.d(TAG, "✅ IGDB Search Success: Found ${games.size} games")
                    games.forEach { game ->
                        Log.d(TAG, "First game: ID: ${game.id}")
                        Log.d(TAG, "Rating: ${game.aggregatedRating}")
                        Log.d(TAG, "Cover URL: ${game.cover?.url}")
                    }
                } else {
                    Log.d(TAG, "❌ IGDB Search Failed: ${response.code()} - ${response.message()}")
                }
                
                // Test popular games query with proper IGDB syntax
                val popularQuery = """
                    fields id,name,summary,first_release_date,aggregated_rating,cover.*;
                    where aggregated_rating != null & aggregated_rating > 0;
                    sort aggregated_rating desc;
                    limit 5;
                """.trimIndent()
                Log.d(TAG, "Testing popular games query: $popularQuery")
                
                val popularResponse = ApiConfig.igdbService.getPopularGames(
                    clientId = ApiConfig.getIgdbClientId(),
                    authorization = "Bearer $token",
                    query = popularQuery
                )
                
                if (popularResponse.isSuccessful && popularResponse.body() != null) {
                    val games = popularResponse.body()!!
                    Log.d(TAG, "✅ IGDB Popular Success: Found ${games.size} games")
                    games.forEach { game ->
                        Log.d(TAG, "Game: ID: ${game.id} - Rating: ${game.aggregatedRating}")
                    }
                } else {
                    Log.d(TAG, "❌ IGDB Popular Failed: ${response.code()} - ${response.message()}")
                }
                
            } else {
                Log.d(TAG, "❌ Failed to get IGDB token")
            }
        } catch (e: Exception) {
            Log.d(TAG, "❌ IGDB Exception: ${e.message}")
        }
    }
    
    private suspend fun testIgdbQueryFormats(context: Context) {
        try {
            Log.d(TAG, "Testing different IGDB query formats...")
            val token = ApiConfig.getIgdbAccessToken()
            if (token != null) {
                
                // Test 1: Basic fields only
                val basicQuery = "fields name,summary; limit 3;"
                Log.d(TAG, "Testing basic query: $basicQuery")
                
                val basicResponse = ApiConfig.igdbService.getPopularGames(
                    clientId = ApiConfig.getIgdbClientId(),
                    authorization = "Bearer $token",
                    query = basicQuery
                )
                
                if (basicResponse.isSuccessful && basicResponse.body() != null) {
                    val games = basicResponse.body()!!
                    Log.d(TAG, "✅ Basic Query Success: Found ${games.size} games")
                    games.forEach { game ->
                        Log.d(TAG, "Basic Game: ${game.name ?: "No name"}")
                    }
                } else {
                    Log.d(TAG, "❌ Basic Query Failed: ${basicResponse.code()} - ${basicResponse.message()}")
                }
                
                // Test 2: With nested fields
                val nestedQuery = "fields name,summary,cover.url; limit 3;"
                Log.d(TAG, "Testing nested query: $nestedQuery")
                
                val nestedResponse = ApiConfig.igdbService.getPopularGames(
                    clientId = ApiConfig.getIgdbClientId(),
                    authorization = "Bearer $token",
                    query = nestedQuery
                )
                
                if (nestedResponse.isSuccessful && nestedResponse.body() != null) {
                    val games = nestedResponse.body()!!
                    Log.d(TAG, "✅ Nested Query Success: Found ${games.size} games")
                    games.forEach { game ->
                        Log.d(TAG, "Nested Game: ${game.name ?: "No name"} - Cover: ${game.cover?.url ?: "No cover"}")
                    }
                } else {
                    Log.d(TAG, "❌ Nested Query Failed: ${nestedResponse.code()} - ${nestedResponse.message()}")
                }
                
                // Test 3: With where clause
                val whereQuery = "fields name,summary; where name != null; limit 3;"
                Log.d(TAG, "Testing where query: $whereQuery")
                
                val whereResponse = ApiConfig.igdbService.getPopularGames(
                    clientId = ApiConfig.getIgdbClientId(),
                    authorization = "Bearer $token",
                    query = whereQuery
                )
                
                if (whereResponse.isSuccessful && whereResponse.body() != null) {
                    val games = whereResponse.body()!!
                    Log.d(TAG, "✅ Where Query Success: Found ${games.size} games")
                    games.forEach { game ->
                        Log.d(TAG, "Where Game: ${game.name ?: "No name"}")
                    }
                } else {
                    Log.d(TAG, "❌ Where Query Failed: ${whereResponse.code()} - ${whereResponse.message()}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "❌ Query Format Test Exception: ${e.message}")
        }
    }

    // Test with manual token to verify specific token works
    private suspend fun testManualIgdbToken(context: Context) {
        try {
            Log.d(TAG, "Testing IGDB with manual token...")
            val manualToken = "e1ts2afevujzvttx6t6d5czswcvced" // Token from Postman
            
            // Test 1: Very basic query
            val basicQuery = "fields name; limit 3;"
            Log.d(TAG, "Testing manual token with basic query: $basicQuery")
            
            val basicResponse = ApiConfig.igdbService.getPopularGames(
                clientId = ApiConfig.getIgdbClientId(),
                authorization = "Bearer $manualToken",
                query = basicQuery
            )
            
            if (basicResponse.isSuccessful && basicResponse.body() != null) {
                val games = basicResponse.body()!!
                Log.d(TAG, "✅ Manual Token Basic Success: Found ${games.size} games")
                games.forEach { game ->
                    Log.d(TAG, "Manual Token Game: ${game.name ?: "No name"} (ID: ${game.id})")
                }
            } else {
                Log.d(TAG, "❌ Manual Token Basic Failed: ${basicResponse.code()} - ${basicResponse.message()}")
                val errorBody = basicResponse.errorBody()?.string()
                Log.d(TAG, "Error body: $errorBody")
            }
            
            // Test 2: With cover field
            val coverQuery = "fields name,cover.url; limit 3;"
            Log.d(TAG, "Testing manual token with cover query: $coverQuery")
            
            val coverResponse = ApiConfig.igdbService.getPopularGames(
                clientId = ApiConfig.getIgdbClientId(),
                authorization = "Bearer $manualToken",
                query = coverQuery
            )
            
            if (coverResponse.isSuccessful && coverResponse.body() != null) {
                val games = coverResponse.body()!!
                Log.d(TAG, "✅ Manual Token Cover Success: Found ${games.size} games")
                games.forEach { game ->
                    Log.d(TAG, "Manual Token Game with Cover: ${game.name ?: "No name"} - Cover: ${game.cover?.url ?: "No cover"}")
                }
            } else {
                Log.d(TAG, "❌ Manual Token Cover Failed: ${coverResponse.code()} - ${coverResponse.message()}")
            }
            
        } catch (e: Exception) {
            Log.d(TAG, "❌ Manual Token Test Exception: ${e.message}")
        }
    }
}