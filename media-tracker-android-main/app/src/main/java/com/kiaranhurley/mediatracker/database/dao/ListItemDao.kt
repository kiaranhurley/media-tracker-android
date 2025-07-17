package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.ListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ListItemDao {
    @Query("SELECT * FROM list_items WHERE listId = :listId")
    fun getListItems(listId: Int): Flow<List<ListItem>>

    @Query("SELECT * FROM list_items WHERE listId = :listId AND itemType = :itemType")
    fun getListItemsByType(listId: Int, itemType: String): Flow<List<ListItem>>

    @Query("SELECT * FROM list_items WHERE listId = :listId AND itemId = :itemId AND itemType = :itemType")
    suspend fun getListItem(listId: Int, itemId: Int, itemType: String): ListItem?

    @Insert
    suspend fun addItemToList(listItem: ListItem)

    @Delete
    suspend fun removeItemFromList(listItem: ListItem)

    @Query("DELETE FROM list_items WHERE listId = :listId AND itemId = :itemId AND itemType = :itemType")
    suspend fun removeItemFromList(listId: Int, itemId: Int, itemType: String)

    @Query("DELETE FROM list_items WHERE listId = :listId")
    suspend fun clearList(listId: Int)

    @Query("SELECT COUNT(*) FROM list_items WHERE listId = :listId")
    suspend fun getListItemCount(listId: Int): Int
}