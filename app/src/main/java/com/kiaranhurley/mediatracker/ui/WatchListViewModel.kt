package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.repository.WatchListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WatchListState {
    object Idle : WatchListState()
    object Loading : WatchListState()
    data class Success(val items: List<WatchList>) : WatchListState()
    data class Error(val message: String) : WatchListState()
}

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val watchListRepository: WatchListRepository
) : ViewModel() {
    private val _state = MutableStateFlow<WatchListState>(WatchListState.Idle)
    val state: StateFlow<WatchListState> = _state.asStateFlow()

    fun loadWatchList(userId: Int) {
        _state.value = WatchListState.Loading
        viewModelScope.launch {
            try {
                val items = watchListRepository.getUserWatchList(userId).asStateFlow().value
                _state.value = WatchListState.Success(items)
            } catch (e: Exception) {
                _state.value = WatchListState.Error(e.message ?: "Failed to load watchlist")
            }
        }
    }

    fun addToWatchList(item: WatchList) {
        viewModelScope.launch {
            try {
                watchListRepository.addToWatchList(item)
            } catch (_: Exception) {}
        }
    }

    fun removeFromWatchList(item: WatchList) {
        viewModelScope.launch {
            try {
                watchListRepository.removeFromWatchList(item)
            } catch (_: Exception) {}
        }
    }

    fun toggleWatchListItem(userId: Int, itemId: Int, itemType: String) {
        viewModelScope.launch {
            try {
                watchListRepository.toggleWatchListItem(userId, itemId, itemType)
            } catch (_: Exception) {}
        }
    }
} 