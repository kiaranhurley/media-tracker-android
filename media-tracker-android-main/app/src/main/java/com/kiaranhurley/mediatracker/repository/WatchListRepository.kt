package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.WatchListDao
import com.kiaranhurley.mediatracker.database.entities.WatchList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchListRepository @Inject constructor(
    private val watchListDao: WatchListDao
) {
    
    /**
     * Get a user's complete watchlist as a Flow
     */
    fun getUserWatchList(userId: Int): Flow<List<WatchList>> {
        return watchListDao.getUserWatchList(userId)
    }
    
    /**
     * Get a user's watchlist filtered by item type (FILM or GAME)
     */
    fun getUserWatchListByType(userId: Int, itemType: String): Flow<List<WatchList>> {
        return watchListDao.getUserWatchListByType(userId, itemType)
    }
    
    /**
     * Get a specific watchlist item
     */
    suspend fun getWatchListItem(userId: Int, itemId: Int, itemType: String): WatchList? {
        return watchListDao.getWatchListItem(userId, itemId, itemType)
    }
    
    /**
     * Add an item to the user's watchlist
     */
    suspend fun addToWatchList(watchList: WatchList) {
        watchListDao.addToWatchList(watchList)
    }
    
    /**
     * Remove an item from the user's watchlist
     */
    suspend fun removeFromWatchList(watchList: WatchList) {
        watchListDao.removeFromWatchList(watchList)
    }
    
    /**
     * Remove an item from the user's watchlist by ID
     */
    suspend fun removeFromWatchList(userId: Int, itemId: Int, itemType: String) {
        watchListDao.removeFromWatchList(userId, itemId, itemType)
    }
    
    /**
     * Get the count of items in a user's watchlist
     */
    suspend fun getWatchListCount(userId: Int): Int {
        return watchListDao.getWatchListCount(userId)
    }
    
    /**
     * Check if an item is in the user's watchlist
     */
    suspend fun isItemInWatchlist(userId: Int, itemId: Int, itemType: String): Boolean {
        return watchListDao.getWatchListItem(userId, itemId, itemType) != null
    }
    
    /**
     * Toggle an item in the watchlist (add if not present, remove if present)
     */
    suspend fun toggleWatchListItem(userId: Int, itemId: Int, itemType: String) {
        val existingItem = watchListDao.getWatchListItem(userId, itemId, itemType)
        if (existingItem != null) {
            watchListDao.removeFromWatchList(existingItem)
        } else {
            val newItem = WatchList(
                userId = userId,
                itemId = itemId,
                itemType = itemType
            )
            watchListDao.addToWatchList(newItem)
        }
    }
} 