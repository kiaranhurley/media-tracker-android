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
            val accessToken = igdbTokenProvider.getAccessToken() ?: return searchGames(query)
            val searchQuery = """
                search "${query}";
                fields name,summary,first_release_date,aggregated_rating,cover.url;
                limit 20;
            """.trimIndent()
            
            val response = igdbService.searchGames(igdbClientId, "Bearer $accessToken", searchQuery)
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                val gameEntities = games.map { game ->
                    Game(
                        igdbId = game.id,
                        name = game.name,
                        summary = game.summary,
                        firstReleaseDate = game.firstReleaseDate,
                        aggregatedRating = game.aggregatedRating,
                        aggregatedRatingCount = null, // Not available in search response
                        coverId = game.cover?.id,
                        coverUrl = game.cover?.url,
                        platforms = null, // Not available in search response
                        developer = null, // Not available in search response
                        publisher = null // Not available in search response
                    )
                }
                
                // Cache games in database
                gameEntities.forEach { game ->
                    insertOrUpdateGame(game)
                }
                
                gameEntities
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Return cached results if API fails
            searchGames(query)
        }
    }
    
    /**
     * Get popular games from IGDB API and cache results
     */
    suspend fun getPopularGamesFromApi(): List<Game> {
        return try {
            val accessToken = igdbTokenProvider.getAccessToken() ?: return getAllGamesOrderedByRating().firstOrNull() ?: emptyList()
            val popularQuery = """
                fields name,summary,first_release_date,aggregated_rating,cover.url;
                sort aggregated_rating desc;
                limit 20;
            """.trimIndent()
            val response = igdbService.getPopularGames(igdbClientId, "Bearer $accessToken", popularQuery)
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                val gameEntities = games.map { game ->
                    Game(
                        igdbId = game.id,
                        name = game.name,
                        summary = game.summary,
                        firstReleaseDate = game.firstReleaseDate,
                        aggregatedRating = game.aggregatedRating,
                        aggregatedRatingCount = null, // Not available in search response
                        coverId = game.cover?.id,
                        coverUrl = game.cover?.url,
                        platforms = null, // Not available in search response
                        developer = null, // Not available in search response
                        publisher = null // Not available in search response
                    )
                }
                
                // Cache games in database
                gameEntities.forEach { game ->
                    insertOrUpdateGame(game)
                }
                
                gameEntities
            } else {
                emptyList()
            }
        } catch (e: Exception) {
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
                    val gameEntity = Game(
                        igdbId = game.id,
                        name = game.name,
                        summary = game.summary,
                        firstReleaseDate = game.firstReleaseDate,
                        aggregatedRating = game.aggregatedRating,
                        aggregatedRatingCount = null, // Not available in search response
                        coverId = game.cover?.id,
                        coverUrl = game.cover?.url,
                        platforms = null, // Not available in search response
                        developer = null, // Not available in search response
                        publisher = null // Not available in search response
                    )
                    
                    // Update cache
                    insertOrUpdateGame(gameEntity)
                    gameEntity
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
} 