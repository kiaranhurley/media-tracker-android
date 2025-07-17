package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.ReviewDao
import com.kiaranhurley.mediatracker.database.entities.Review
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao
) {
    
    /**
     * Get a review by its ID
     */
    suspend fun getReviewById(reviewId: Int): Review? {
        return reviewDao.getReviewById(reviewId)
    }
    
    /**
     * Get all reviews for a user as a Flow
     */
    fun getUserReviews(userId: Int): Flow<List<Review>> {
        return reviewDao.getUserReviews(userId)
    }
    
    /**
     * Get all reviews for a specific item (film or game)
     */
    suspend fun getReviewsForItem(itemId: Int, itemType: String): List<Review> {
        return reviewDao.getReviewsForItem(itemId, itemType)
    }
    
    /**
     * Get a user's review for a specific item
     */
    suspend fun getUserReviewForItem(userId: Int, itemId: Int, itemType: String): Review? {
        return reviewDao.getUserReviewForItem(userId, itemId, itemType)
    }
    
    /**
     * Get all reviews ordered by creation date as a Flow
     */
    fun getAllReviewsOrderedByDate(): Flow<List<Review>> {
        return reviewDao.getAllReviewsOrderedByDate()
    }
    
    /**
     * Add a new review
     */
    suspend fun addReview(review: Review): Long {
        return reviewDao.insertReview(review)
    }
    
    /**
     * Update an existing review
     */
    suspend fun updateReview(review: Review) {
        reviewDao.updateReview(review)
    }
    
    /**
     * Delete a review
     */
    suspend fun deleteReview(review: Review) {
        reviewDao.deleteReview(review)
    }
    
    /**
     * Delete a user's review for a specific item
     */
    suspend fun deleteUserReviewForItem(userId: Int, itemId: Int, itemType: String) {
        reviewDao.deleteUserReviewForItem(userId, itemId, itemType)
    }
    
    /**
     * Check if user has reviewed an item
     */
    suspend fun hasUserReviewed(userId: Int, itemId: Int, itemType: String): Boolean {
        return reviewDao.getUserReviewForItem(userId, itemId, itemType) != null
    }
} 