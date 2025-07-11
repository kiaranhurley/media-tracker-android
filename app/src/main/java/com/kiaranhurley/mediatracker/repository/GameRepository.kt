package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.api.services.IgdbService
import com.kiaranhurley.mediatracker.database.dao.GameDao
import com.kiaranhurley.mediatracker.database.entities.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val igdbService: IgdbService
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
    suspend fun searchGamesFromApi(query: String, clientId: String, accessToken: String): List<Game> {
        return try {
            val searchQuery = """
                search "${query}";
                fields name,summary,first_release_date,aggregated_rating,aggregated_rating_count,cover.url,platforms.name,involved_companies.company.name,involved_companies.developer,involved_companies.publisher;
                limit 20;
            """.trimIndent()
            
            val response = igdbService.searchGames(clientId, "Bearer $accessToken", searchQuery)
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                val gameEntities = games.map { game ->
                    Game(
                        igdbId = game.id,
                        name = game.name,
                        summary = game.summary,
                        firstReleaseDate = game.firstReleaseDate,
                        aggregatedRating = game.aggregatedRating,
                        aggregatedRatingCount = game.aggregatedRatingCount,
                        coverUrl = game.cover?.url,
                        platforms = game.platforms?.joinToString(", ") { it.name },
                        developer = game.involvedCompanies?.firstOrNull { it.developer }?.company?.name,
                        publisher = game.involvedCompanies?.firstOrNull { it.publisher }?.company?.name
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
    suspend fun getPopularGamesFromApi(clientId: String, accessToken: String): List<Game> {
        return try {
            val response = igdbService.getPopularGames(clientId, "Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                val games = response.body()!!
                val gameEntities = games.map { game ->
                    Game(
                        igdbId = game.id,
                        name = game.name,
                        summary = game.summary,
                        firstReleaseDate = game.firstReleaseDate,
                        aggregatedRating = game.aggregatedRating,
                        aggregatedRatingCount = game.aggregatedRatingCount,
                        coverUrl = game.cover?.url,
                        platforms = game.platforms?.joinToString(", ") { it.name },
                        developer = game.involvedCompanies?.firstOrNull { it.developer }?.company?.name,
                        publisher = game.involvedCompanies?.firstOrNull { it.publisher }?.company?.name
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
    suspend fun getGameDetailsFromApi(igdbId: Int, clientId: String, accessToken: String): Game? {
        return try {
            val detailQuery = """
                where id = $igdbId;
                fields name,summary,first_release_date,aggregated_rating,aggregated_rating_count,cover.url,platforms.name,involved_companies.company.name,involved_companies.developer,involved_companies.publisher;
            """.trimIndent()
            
            val response = igdbService.getGameDetails(clientId, "Bearer $accessToken", detailQuery)
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
                        aggregatedRatingCount = game.aggregatedRatingCount,
                        coverUrl = game.cover?.url,
                        platforms = game.platforms?.joinToString(", ") { it.name },
                        developer = game.involvedCompanies?.firstOrNull { it.developer }?.company?.name,
                        publisher = game.involvedCompanies?.firstOrNull { it.publisher }?.company?.name
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