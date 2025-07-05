package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.WatchList
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchListDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId")
    fun getUserWatchList(userId: Int): Flow<List<WatchList>>

    @Query("SELECT * FROM watchlist WHERE userId = :userId AND itemType = :itemType")
    fun getUserWatchListByType(userId: Int, itemType: String): Flow<List<WatchList>>

    @Query("SELECT * FROM watchlist WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun getWatchListItem(userId: Int, itemId: Int, itemType: String): WatchList?

    @Insert
    suspend fun addToWatchList(watchList: WatchList)

    @Delete
    suspend fun removeFromWatchList(watchList: WatchList)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun removeFromWatchList(userId: Int, itemId: Int, itemType: String)

    @Query("SELECT COUNT(*) FROM watchlist WHERE userId = :userId")
    suspend fun getWatchListCount(userId: Int): Int
}