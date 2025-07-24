package com.kiaranhurley.mediatracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase
import com.kiaranhurley.mediatracker.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsState {
    object Loading : SettingsState()
    object Success : SettingsState()
    data class Error(val message: String) : SettingsState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val database: MediaTrackerDatabase
) : ViewModel() {
    
    private val _state = MutableStateFlow<SettingsState>(SettingsState.Success)
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            try {
                _state.value = SettingsState.Loading
                userRepository.updateDisplayName(newName)
                _state.value = SettingsState.Success
            } catch (e: Exception) {
                _state.value = SettingsState.Error("Failed to update display name: ${e.message}")
            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            try {
                _state.value = SettingsState.Loading
                database.clearAllTables()
                _state.value = SettingsState.Success
            } catch (e: Exception) {
                _state.value = SettingsState.Error("Failed to clear database: ${e.message}")
            }
        }
    }
} 