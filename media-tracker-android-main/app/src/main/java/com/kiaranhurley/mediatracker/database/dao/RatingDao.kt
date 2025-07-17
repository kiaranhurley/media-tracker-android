package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.Rating
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun getUserRating(userId: Int, itemId: Int, itemType: String): Rating?

    @Query("SELECT * FROM ratings WHERE userId = :userId")
    fun getUserRatings(userId: Int): Flow<List<Rating>>

    @Query("SELECT AVG(rating) FROM ratings WHERE itemId = :itemId AND itemType = :itemType")
    suspend fun getAverageRating(itemId: Int, itemType: String): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: Rating)

    @Update
    suspend fun updateRating(rating: Rating)

    @Delete
    suspend fun deleteRating(rating: Rating)

    @Query("DELETE FROM ratings WHERE userId = :userId AND itemId = :itemId AND itemType = :itemType")
    suspend fun deleteUserRating(userId: Int, itemId: Int, itemType: String)
}