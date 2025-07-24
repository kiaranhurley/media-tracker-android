package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.api.IgdbTokenProvider
import com.kiaranhurley.mediatracker.api.services.IgdbService
import com.kiaranhurley.mediatracker.database.dao.GameDao
import com.kiaranhurley.mediatracker.database.entities.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.Date

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val igdbService: IgdbService,
    @Named("igdb_client_id") private val igdbClientId: String,
    private val igdbTokenProvider: IgdbTokenProvider
) {
    
    /**
     * Get a game by its local ID
     */
    suspend fun getGameById(gameId: Int): Game? {
        return gameDao.getGameById(gameId)
    }
    
    /**
     * Get a game by its IGDB ID
     */
    suspend fun getGameByIgdbId(igdbId: Int): Game? {
        return gameDao.getGameByIgdbId(igdbId)
    }
    
    /**
     * Get all games ordered by rating as a Flow
     */
    fun getAllGamesOrderedByRating(): Flow<List<Game>> {
        return gameDao.getAllGamesOrderedByRating()
    }
    
    /**
     * Search games by name
     */
    suspend fun searchGames(searchQuery: String): List<Game> {
        return gameDao.searchGames(searchQuery)
    }
    
    /**
     * Insert a new game and return the generated ID
     */
    suspend fun insertGame(game: Game): Long {
        return gameDao.insertGame(game)
    }
    
    /**
     * Update an existing game
     */
    suspend fun updateGame(game: Game) {
        gameDao.updateGame(game)
    }
    
    /**
     * Delete a game
     */
    suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game)
    }
    
    /**
     * Delete all games (clear database)
     */
    suspend fun deleteAllGames() {
        gameDao.deleteAllGames()
    }
    
    /**
     * Get recent games
     */
    suspend fun getRecentGames(limit: Int = 10): List<Game> {
        return gameDao.getRecentGames(limit)
    }
    
    /**
     * Check if a game exists by IGDB ID
     */
    suspend fun gameExistsByIgdbId(igdbId: Int): Boolean {
        return gameDao.getGameByIgdbId(igdbId) != null
    }
    
    /**
     * Insert or update a game (upsert operation)
     */
    suspend fun insertOrUpdateGame(game: Game): Long {
        val existingGame = gameDao.getGameByIgdbId(game.igdbId)
        return if (existingGame != null) {
            // Update existing game with new data
            val updatedGame = game.copy(gameId = existingGame.gameId)
            gameDao.updateGame(updatedGame)
            existingGame.gameId.toLong()
        } else {
            // Insert new game
            gameDao.insertGame(game)
        }
    }
    
    /**
     * Get games with high ratings (above threshold)
     */
    suspend fun getHighlyRatedGames(ratingThreshold: Float = 8.0f): List<Game> {
        // This would need to be implemented in the DAO if needed
        // For now, we'll get all games and filter in memory
        // Note: This is a simplified implementation. In a real app, you'd want to implement this in the DAO
        return emptyList() // Placeholder - implement proper DAO query
    }
    
    /**
     * Get games by platform
     */
    suspend fun getGamesByPlatform(platform: String): List<Game> {
        // This would need to be implemented in the DAO
        // For now, we'll search games and filter by platform
        return gameDao.searchGames(platform)
    }
    
    // API Integration Methods
    
    /**
     * Search games from IGDB API and cache results
     */
    suspend fun searchGamesFromApi(query: String): List<Game> {
        return try {
            val accessToken = igdbTokenProvider.getAccessToken()
            if (accessToken == null) {
                println("DEBUG: Failed to get IGDB access token")
                return searchGames(query)
            }
            
            // Simplified search query - more likely to work
            val searchQuery = """
                search "$query";
                fields name,summary,first_release_date,aggregated_rating,cover.url;
                where name != null;
                limit 20;
            """.trimIndent()
            
            println("DEBUG: IGDB Search Query: $searchQuery")
            println("DEBUG: Using token: ${accessToken.take(10)}...")
            
            val response = igdbService.searchGames(igdbClientId, "Bearer $accessToken", searchQuery)
            
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                println("DEBUG: IGDB API returned ${games.size} games")
                
                val gameEntities = games.mapNotNull { game ->
                    try {
                        // Skip games with null names
                        if (game.name == null) {
                            println("DEBUG: Skipping game with null name, ID: ${game.id}")
                            return@mapNotNull null
                        }
                        
                        // Fix cover URL by adding https: prefix if missing
                        val coverUrl = game.cover?.url?.let { url ->
                            if (url.startsWith("//")) "https:$url" else url
                        }
                        
                        println("DEBUG: Processing game: ${game.name}, Cover URL: $coverUrl")
                        
                        Game(
                            igdbId = game.id,
                            name = game.name,
                            summary = game.summary,
                            firstReleaseDate = game.firstReleaseDate,
                            aggregatedRating = game.aggregatedRating,
                            aggregatedRatingCount = null, // Not included in simplified query
                            coverId = game.cover?.id,
                            coverUrl = coverUrl,
                            platforms = null, // Will be fetched separately if needed
                            developer = null, // Will be fetched separately if needed
                            publisher = null // Will be fetched separately if needed
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Error processing game ${game.id}: ${e.message}")
                        null
                    }
                }
                
                println("DEBUG: Mapped ${gameEntities.size} game entities")
                
                // Cache games in database
                gameEntities.forEach { game ->
                    insertOrUpdateGame(game)
                }
                
                gameEntities
            } else {
                println("DEBUG: IGDB API failed: ${response.code()} - ${response.message()}")
                println("DEBUG: Error body: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: IGDB Exception: ${e.message}")
            e.printStackTrace()
            // Return cached results if API fails
            searchGames(query)
        }
    }
    
    /**
     * Get popular games from IGDB API and cache results
     */
    suspend fun getPopularGamesFromApi(): List<Game> {
        return try {
            println("DEBUG: GameRepository - Starting getPopularGamesFromApi")
            val accessToken = igdbTokenProvider.getAccessToken()
            println("DEBUG: GameRepository - Got access token: ${accessToken?.take(10)}...")
            
            if (accessToken == null) {
                println("DEBUG: GameRepository - Failed to get IGDB access token for popular games")
                return getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
            }
            
            // Enhanced popular games query with more fields and better sorting
            val popularQuery = """
                fields name,summary,first_release_date,aggregated_rating,cover.url;
                where name != null & first_release_date != null;
                sort first_release_date desc;
                limit 50;
            """.trimIndent()
            
            println("DEBUG: GameRepository - IGDB Popular Query: $popularQuery")
            println("DEBUG: GameRepository - Using Client ID: ${igdbClientId.take(10)}...")
            println("DEBUG: GameRepository - Using Auth: Bearer ${accessToken.take(10)}...")
            
            val response = igdbService.getPopularGames(igdbClientId, "Bearer $accessToken", popularQuery)
            println("DEBUG: GameRepository - API Response Code: ${response.code()}")
            println("DEBUG: GameRepository - API Response Message: ${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                println("DEBUG: GameRepository - IGDB Popular API returned ${games.size} games")
                
                val gameEntities = games.mapNotNull { game ->
                    try {
                        // Skip games with null names
                        if (game.name == null) {
                            println("DEBUG: GameRepository - Skipping game with null name, ID: ${game.id}")
                            return@mapNotNull null
                        }
                        
                        // Fix cover URL by adding https: prefix if missing
                        val coverUrl = game.cover?.url?.let { url ->
                            if (url.startsWith("//")) "https:$url" else url
                        }
                        
                        println("DEBUG: GameRepository - Processing game: ${game.name}, Cover URL: $coverUrl")
                        
                        Game(
                            igdbId = game.id,
                            name = game.name,
                            summary = game.summary,
                            firstReleaseDate = game.firstReleaseDate,
                            aggregatedRating = game.aggregatedRating,
                            aggregatedRatingCount = null, // Not included in simplified query
                            coverId = game.cover?.id,
                            coverUrl = coverUrl,
                            platforms = null, // Will be fetched separately if needed
                            developer = null, // Will be fetched separately if needed
                            publisher = null // Will be fetched separately if needed
                        )
                    } catch (e: Exception) {
                        println("DEBUG: GameRepository - Error processing game ${game.id}: ${e.message}")
                        null
                    }
                }
                
                println("DEBUG: GameRepository - Mapped ${gameEntities.size} game entities")
                
                // Cache games in database
                gameEntities.forEach { game ->
                    insertOrUpdateGame(game)
                }
                
                gameEntities
            } else {
                println("DEBUG: GameRepository - IGDB API failed: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                println("DEBUG: GameRepository - Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: GameRepository - IGDB Exception: ${e.message}")
            println("DEBUG: GameRepository - Stack trace:")
            e.printStackTrace()
            // Return cached results if API fails
            getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
        }
    }
    
    /**
     * Get game details from IGDB API and update cache
     */
    suspend fun getGameDetailsFromApi(igdbId: Int): Game? {
        return try {
            val accessToken = igdbTokenProvider.getAccessToken() ?: return getGameByIgdbId(igdbId)
            val detailQuery = """
                where id = $igdbId;
                fields name,summary,first_release_date,aggregated_rating,cover.url;
            """.trimIndent()
            
            val response = igdbService.getGameDetails(igdbClientId, "Bearer $accessToken", detailQuery)
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                if (games.isNotEmpty()) {
                    val game = games[0]
                    try {
                        // Skip games with null names
                        if (game.name == null) {
                            println("DEBUG: Skipping game with null name, ID: ${game.id}")
                            return null
                        }
                        
                        // Fix cover URL by adding https: prefix if missing
                        val coverUrl = game.cover?.url?.let { url ->
                            if (url.startsWith("//")) "https:$url" else url
                        }
                        
                        println("DEBUG: Processing game details: ${game.name}, Cover URL: $coverUrl")
                        
                        val gameEntity = Game(
                            igdbId = game.id,
                            name = game.name,
                            summary = game.summary,
                            firstReleaseDate = game.firstReleaseDate,
                            aggregatedRating = game.aggregatedRating,
                            aggregatedRatingCount = null, // Not included in simplified query
                            coverId = game.cover?.id,
                            coverUrl = coverUrl,
                            platforms = null, // Will be fetched separately if needed
                            developer = null, // Will be fetched separately if needed
                            publisher = null // Will be fetched separately if needed
                        )
                        
                        // Update cache
                        insertOrUpdateGame(gameEntity)
                        gameEntity
                    } catch (e: Exception) {
                        println("DEBUG: Error processing game details ${game.id}: ${e.message}")
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            // Return cached result if API fails
            getGameByIgdbId(igdbId)
        }
    }

    /**
     * Temporary test function to verify manual token works
     */
    suspend fun testManualToken(): List<Game> {
        return try {
            println("DEBUG: GameRepository - Testing manual token...")
            val manualToken = "e1ts2afevujzvttx6t6d5czswcvced"
            
            // IGDB requires a more specific query format
            val testQuery = """
                fields id,name,summary,cover.*,first_release_date,aggregated_rating;
                where version_parent = null & category = 0;
                sort popularity desc;
                limit 10;
            """.trimIndent()
            
            println("DEBUG: GameRepository - Manual token query: $testQuery")
            
            val response = igdbService.getPopularGames(
                clientId = igdbClientId,
                authorization = "Bearer $manualToken",
                query = testQuery
            )
            println("DEBUG: GameRepository - Manual token response: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                println("DEBUG: GameRepository - Manual token success: ${games.size} games")
                
                games.forEachIndexed { index, game ->
                    println("DEBUG: GameRepository - Raw game ${index + 1}: ID=${game.id}, name='${game.name}', release_date=${game.firstReleaseDate}, rating=${game.aggregatedRating}")
                }
                
                val gameEntities = games.mapNotNull { game ->
                    if (game.name != null && game.name.isNotBlank()) {
                        println("DEBUG: GameRepository - Processing game: ${game.name}")
                        Game(
                            igdbId = game.id,
                            name = game.name,
                            summary = game.summary,
                            firstReleaseDate = game.firstReleaseDate?.let { Date(it * 1000) },
                            aggregatedRating = game.aggregatedRating,
                            aggregatedRatingCount = null,
                            coverId = game.cover?.id,
                            coverUrl = game.cover?.url?.let { url ->
                                if (url.startsWith("//")) "https:$url" else url
                            },
                            platforms = null,
                            developer = null,
                            publisher = null
                        )
                    } else {
                        println("DEBUG: GameRepository - Skipping game with null/empty name: ID=${game.id}")
                        null
                    }
                }
                
                println("DEBUG: GameRepository - Manual token processed: ${gameEntities.size} entities")
                gameEntities
            } else {
                println("DEBUG: GameRepository - Manual token failed: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                println("DEBUG: GameRepository - Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            println("DEBUG: GameRepository - Manual token exception: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
} 