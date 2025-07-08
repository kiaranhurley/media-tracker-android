package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.RatingDao
import com.kiaranhurley.mediatracker.database.entities.Rating
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(
    private val ratingDao: RatingDao
) {
    
    /**
     * Get a user's rating for a specific item
     */
    suspend fun getUserRating(userId: Int, itemId: Int, itemType: String): Rating? {
        return ratingDao.getUserRating(userId, itemId, itemType)
    }
    
    /**
     * Get all ratings for a user as a Flow
     */
    fun getUserRatings(userId: Int): Flow<List<Rating>> {
        return ratingDao.getUserRatings(userId)
    }
    
    /**
     * Get average rating for an item
     */
    suspend fun getAverageRating(itemId: Int, itemType: String): Float? {
        return ratingDao.getAverageRating(itemId, itemType)
    }
    
    /**
     * Add or update a user's rating
     */
    suspend fun setRating(rating: Rating) {
        ratingDao.insertRating(rating)
    }
    
    /**
     * Update an existing rating
     */
    suspend fun updateRating(rating: Rating) {
        ratingDao.updateRating(rating)
    }
    
    /**
     * Delete a user's rating
     */
    suspend fun deleteRating(rating: Rating) {
        ratingDao.deleteRating(rating)
    }
    
    /**
     * Delete a user's rating for a specific item
     */
    suspend fun deleteUserRating(userId: Int, itemId: Int, itemType: String) {
        ratingDao.deleteUserRating(userId, itemId, itemType)
    }
    
    /**
     * Check if user has rated an item
     */
    suspend fun hasUserRated(userId: Int, itemId: Int, itemType: String): Boolean {
        return ratingDao.getUserRating(userId, itemId, itemType) != null
    }
} 