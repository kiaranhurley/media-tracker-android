package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.CustomListDao
import com.kiaranhurley.mediatracker.database.dao.ListItemDao
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.database.entities.ListItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomListRepository @Inject constructor(
    private val customListDao: CustomListDao,
    private val listItemDao: ListItemDao
) {
    
    /**
     * Get a custom list by its ID
     */
    suspend fun getListById(listId: Int): CustomList? {
        return customListDao.getListById(listId)
    }
    
    /**
     * Get all lists for a user as a Flow
     */
    fun getUserLists(userId: Int): Flow<List<CustomList>> {
        return customListDao.getUserLists(userId)
    }
    
    /**
     * Get all public lists for a user as a Flow
     */
    fun getUserPublicLists(userId: Int): Flow<List<CustomList>> {
        return customListDao.getUserPublicLists(userId)
    }
    
    /**
     * Get all public lists as a Flow
     */
    fun getAllPublicLists(): Flow<List<CustomList>> {
        return customListDao.getAllPublicLists()
    }
    
    /**
     * Create a new custom list
     */
    suspend fun createList(customList: CustomList): Long {
        return customListDao.createList(customList)
    }
    
    /**
     * Update an existing custom list
     */
    suspend fun updateList(customList: CustomList) {
        customListDao.updateList(customList)
    }
    
    /**
     * Delete a custom list (this will also delete all list items due to CASCADE)
     */
    suspend fun deleteList(customList: CustomList) {
        customListDao.deleteList(customList)
    }
    
    /**
     * Get the count of lists for a user
     */
    suspend fun getUserListCount(userId: Int): Int {
        return customListDao.getUserListCount(userId)
    }
    
    // ListItem operations
    
    /**
     * Get all items in a list as a Flow
     */
    fun getListItems(listId: Int): Flow<List<ListItem>> {
        return listItemDao.getListItems(listId)
    }
    
    /**
     * Get items in a list filtered by type as a Flow
     */
    fun getListItemsByType(listId: Int, itemType: String): Flow<List<ListItem>> {
        return listItemDao.getListItemsByType(listId, itemType)
    }
    
    /**
     * Get a specific list item
     */
    suspend fun getListItem(listId: Int, itemId: Int, itemType: String): ListItem? {
        return listItemDao.getListItem(listId, itemId, itemType)
    }
    
    /**
     * Add an item to a list
     */
    suspend fun addItemToList(listItem: ListItem) {
        listItemDao.addItemToList(listItem)
    }
    
    /**
     * Remove an item from a list
     */
    suspend fun removeItemFromList(listItem: ListItem) {
        listItemDao.removeItemFromList(listItem)
    }
    
    /**
     * Remove an item from a list by ID
     */
    suspend fun removeItemFromList(listId: Int, itemId: Int, itemType: String) {
        listItemDao.removeItemFromList(listId, itemId, itemType)
    }
    
    /**
     * Clear all items from a list
     */
    suspend fun clearList(listId: Int) {
        listItemDao.clearList(listId)
    }
    
    /**
     * Get the count of items in a list
     */
    suspend fun getListItemCount(listId: Int): Int {
        return listItemDao.getListItemCount(listId)
    }
    
    /**
     * Check if an item is in a list
     */
    suspend fun isItemInList(listId: Int, itemId: Int, itemType: String): Boolean {
        return listItemDao.getListItem(listId, itemId, itemType) != null
    }
    
    /**
     * Toggle an item in a list (add if not present, remove if present)
     */
    suspend fun toggleListItem(listId: Int, itemId: Int, itemType: String) {
        val existingItem = listItemDao.getListItem(listId, itemId, itemType)
        if (existingItem != null) {
            listItemDao.removeItemFromList(existingItem)
        } else {
            val newItem = ListItem(
                listId = listId,
                itemId = itemId,
                itemType = itemType
            )
            listItemDao.addItemToList(newItem)
        }
    }
} 