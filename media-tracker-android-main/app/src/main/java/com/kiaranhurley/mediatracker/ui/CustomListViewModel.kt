package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.database.entities.ListItem
import com.kiaranhurley.mediatracker.repository.CustomListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

sealed class CustomListState {
    object Idle : CustomListState()
    object Loading : CustomListState()
    data class Success(val customLists: List<CustomList>) : CustomListState()
    data class Error(val message: String) : CustomListState()
    // Add detail state
    data class Detail(val customList: CustomList, val items: List<ListItem>) : CustomListState()
}

@HiltViewModel
class CustomListViewModel @Inject constructor(
    private val customListRepository: CustomListRepository
) : ViewModel() {
    private val _customListState = MutableStateFlow<CustomListState>(CustomListState.Idle)
    val customListState: StateFlow<CustomListState> = _customListState.asStateFlow()

    fun loadCustomLists(userId: Int) {
        _customListState.value = CustomListState.Loading
        viewModelScope.launch {
            try {
                val lists = customListRepository.getUserLists(userId).firstOrNull() ?: emptyList()
                _customListState.value = CustomListState.Success(lists)
            } catch (e: Exception) {
                _customListState.value = CustomListState.Error(e.message ?: "Failed to load custom lists")
            }
        }
    }

    fun createCustomList(userId: Int, name: String, description: String) {
        viewModelScope.launch {
            try {
                val customList = CustomList(
                    userId = userId,
                    name = name,
                    description = description.ifBlank { null },
                    createdAt = Date()
                )
                customListRepository.createList(customList)
                loadCustomLists(userId) // Refresh the list
            } catch (e: Exception) {
                _customListState.value = CustomListState.Error(e.message ?: "Failed to create list")
            }
        }
    }

    fun updateCustomList(listId: Int, name: String, description: String) {
        viewModelScope.launch {
            try {
                val currentState = _customListState.value
                if (currentState is CustomListState.Success) {
                    val existingList = currentState.customLists.find { it.listId == listId }
                    if (existingList != null) {
                        val updatedList = existingList.copy(
                            name = name,
                            description = description.ifBlank { null }
                        )
                        customListRepository.updateList(updatedList)
                        loadCustomLists(existingList.userId) // Refresh the list
                    }
                }
            } catch (e: Exception) {
                _customListState.value = CustomListState.Error(e.message ?: "Failed to update list")
            }
        }
    }

    fun deleteCustomList(listId: Int) {
        viewModelScope.launch {
            try {
                val currentState = _customListState.value
                if (currentState is CustomListState.Success) {
                    val existingList = currentState.customLists.find { it.listId == listId }
                    if (existingList != null) {
                        customListRepository.deleteList(existingList)
                        loadCustomLists(existingList.userId) // Refresh the list
                    }
                }
            } catch (e: Exception) {
                _customListState.value = CustomListState.Error(e.message ?: "Failed to delete list")
            }
        }
    }

    fun addItemToList(listItem: ListItem) {
        viewModelScope.launch {
            try {
                customListRepository.addItemToList(listItem)
            } catch (_: Exception) {}
        }
    }

    fun removeItemFromList(listItem: ListItem) {
        viewModelScope.launch {
            try {
                customListRepository.removeItemFromList(listItem)
            } catch (_: Exception) {}
        }
    }

    fun toggleItemInList(listItem: ListItem) {
        viewModelScope.launch {
            try {
                val existingItems = customListRepository.getListItems(listItem.listId).firstOrNull() ?: emptyList()
                val exists = existingItems.any { 
                    it.itemId == listItem.itemId && it.itemType == listItem.itemType 
                }
                
                if (exists) {
                    customListRepository.removeItemFromList(listItem)
                } else {
                    customListRepository.addItemToList(listItem)
                }
            } catch (_: Exception) {}
        }
    }

    fun loadList(listId: Int) {
        _customListState.value = CustomListState.Loading
        viewModelScope.launch {
            try {
                val customList = customListRepository.getListById(listId)
                val items = customListRepository.getListItems(listId).firstOrNull() ?: emptyList()
                if (customList != null) {
                    _customListState.value = CustomListState.Detail(customList, items)
                } else {
                    _customListState.value = CustomListState.Error("List not found")
                }
            } catch (e: Exception) {
                _customListState.value = CustomListState.Error(e.message ?: "Failed to load list")
            }
        }
    }
} 