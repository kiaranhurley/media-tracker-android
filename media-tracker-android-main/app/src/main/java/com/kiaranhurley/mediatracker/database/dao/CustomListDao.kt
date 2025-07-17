package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.CustomList
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {
    @Query("SELECT * FROM custom_lists WHERE listId = :listId")
    suspend fun getListById(listId: Int): CustomList?

    @Query("SELECT * FROM custom_lists WHERE userId = :userId")
    fun getUserLists(userId: Int): Flow<List<CustomList>>

    @Query("SELECT * FROM custom_lists WHERE userId = :userId AND isPrivate = 0")
    fun getUserPublicLists(userId: Int): Flow<List<CustomList>>

    @Query("SELECT * FROM custom_lists WHERE isPrivate = 0 ORDER BY createdAt DESC")
    fun getAllPublicLists(): Flow<List<CustomList>>

    @Insert
    suspend fun createList(customList: CustomList): Long

    @Update
    suspend fun updateList(customList: CustomList)

    @Delete
    suspend fun deleteList(customList: CustomList)

    @Query("SELECT COUNT(*) FROM custom_lists WHERE userId = :userId")
    suspend fun getUserListCount(userId: Int): Int
}