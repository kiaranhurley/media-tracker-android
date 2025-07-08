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
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CustomListState {
    object Idle : CustomListState()
    object Loading : CustomListState()
    data class Success(val lists: List<CustomList>, val items: List<ListItem>) : CustomListState()
    data class Error(val message: String) : CustomListState()
}

@HiltViewModel
class CustomListViewModel @Inject constructor(
    private val customListRepository: CustomListRepository
) : ViewModel() {
    private val _state = MutableStateFlow<CustomListState>(CustomListState.Idle)
    val state: StateFlow<CustomListState> = _state.asStateFlow()

    fun loadCustomLists(userId: Int) {
        _state.value = CustomListState.Loading
        viewModelScope.launch {
            try {
                val lists = customListRepository.getUserLists(userId).asStateFlow().value
                val items = lists.flatMap { customListRepository.getListItems(it.listId).asStateFlow().value }
                _state.value = CustomListState.Success(lists, items)
            } catch (e: Exception) {
                _state.value = CustomListState.Error(e.message ?: "Failed to load custom lists")
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

    fun toggleListItem(listId: Int, itemId: Int, itemType: String) {
        viewModelScope.launch {
            try {
                customListRepository.toggleListItem(listId, itemId, itemType)
            } catch (_: Exception) {}
        }
    }
} 