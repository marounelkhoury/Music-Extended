package com.musicextended.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicextended.MusicExtendedApplication
import com.musicextended.data.local.entities.TrackEntity
import com.musicextended.data.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch // Import catch operator
import kotlinx.coroutines.flow.onStart // Import onStart operator
import kotlinx.coroutines.launch

class SavedTracksViewModel(private val application: MusicExtendedApplication) : ViewModel() {

    private val trackRepository: TrackRepository by lazy {
        application.trackRepository
    }

    private val _savedTracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val savedTracks: StateFlow<List<TrackEntity>> = _savedTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchSavedTracks()
    }

    fun fetchSavedTracks() {
        viewModelScope.launch {
            trackRepository.getCurrentUserSavedTracks()
                .onStart { // Emit loading state before starting collection
                    _isLoading.value = true
                    _error.value = null // Clear any previous error
                    Log.d("SavedTracksViewModel", "Starting track fetch...")
                }
                .catch { e -> // Catch any exceptions in the flow
                    Log.e("SavedTracksViewModel", "Error fetching saved tracks: ${e.message}", e)
                    _error.value = e.message
                    _isLoading.value = false // Stop loading on error
                }
                .collect { fetchedTracks -> // Collect the emissions from the Flow
                    _savedTracks.value = fetchedTracks
                    Log.d("SavedTracksViewModel", "Collected ${fetchedTracks.size} saved tracks.")
                    _isLoading.value = false // Stop loading after successful collection
                }
        }
    }

    class Factory(private val application: MusicExtendedApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SavedTracksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SavedTracksViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}