package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE gameId = :gameId")
    suspend fun getGameById(gameId: Int): Game?

    @Query("SELECT * FROM games WHERE igdbId = :igdbId")
    suspend fun getGameByIgdbId(igdbId: Int): Game?

    @Query("SELECT * FROM games ORDER BY aggregatedRating DESC")
    fun getAllGamesOrderedByRating(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE name LIKE '%' || :searchQuery || '%'")
    suspend fun searchGames(searchQuery: String): List<Game>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game): Long

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("SELECT * FROM games ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentGames(limit: Int): List<Game>
    
    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
}