package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE reviewId = :reviewId")
    suspend fun getReviewById(reviewId: Int): Review?

    @Query("SELECT * FROM reviews WHERE userId = :userId")
    fun getUserReviews(userId: Int): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE itemId = :itemId AND itemType = :itemType")
    suspend fun getReviewsForItem(itemId: Int, itemType: String): List<Review>

    @Query("SELECT * FROM reviews WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun getUserReviewForItem(userId: Int, itemId: Int, itemType: String): Review?

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviewsOrderedByDate(): Flow<List<Review>>

    @Insert
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("DELETE FROM reviews WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun deleteUserReviewForItem(userId: Int, itemId: Int, itemType: String)
}